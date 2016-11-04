package eu.thermz.java.ssh;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FtpServiceImpl implements FtpService {

	private static final Logger log = Logger.getLogger(FtpServiceImpl.class);
	
	private String host;
	private int port = 21;
	private int connectionTimeout = 15000;
	private int dataTimeout = 30000;

	// ssh credentials. Username and one between password and privkey are mandatory
	private String username;
	private String password;

	@Override
	public <T> T executeInSession(FtpSessionCallback<T> callback) throws RuntimeException {
		final FTPClient ftp = new FTPClient();
		try {
			ftp.setConnectTimeout(connectionTimeout);
			ftp.setDataTimeout(dataTimeout);
			log.debug("connecting to "+host+":"+port);
			ftp.connect(host, port);	

			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
				throw new RuntimeException("FTP server refused connection. Reply Code: " + reply);
			log.debug("Ftp session connected");

			if (!ftp.login(username, password)) {
				// maybe a loginexception? for the time being, we throw only ioexceptions
				throw new RuntimeException("FTP Connection error: Wrong Username/Password");
			}

			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();

			final FtpServiceImpl ftpServiceImpl = this;

			Object proxy = Proxy.newProxyInstance(FtpServiceImpl.class.getClassLoader(),
					new Class[] { FtpService.class }, new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
							// Find a method with same name and a FTPClient parameter
							Class<?>[] oldParameterTypes = m.getParameterTypes();
							Class<?>[] newParameterTypes = Arrays.copyOf(oldParameterTypes,
									oldParameterTypes.length + 1);
							newParameterTypes[oldParameterTypes.length] = FTPClient.class;

							Object[] newArgs = Arrays.copyOf(args, args.length + 1);
							newArgs[args.length] = ftp;

							// this MUST have an inner method with the same name and a FtpClient parameter after the
							// interface method parameters for every interface method
							Method sessionAwareMethod = FtpServiceImpl.class.getDeclaredMethod(m.getName(),
									newParameterTypes);

							try { // execute the method
								return sessionAwareMethod.invoke(ftpServiceImpl, newArgs);
							} catch (InvocationTargetException e) {
								throw e.getTargetException();
							}
						}
					});

			return callback.execute((FtpService) proxy);

		} catch(IOException io){
			throw new RuntimeException("Error during FTP connection", io);
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.logout();
					ftp.disconnect();
				} catch (IOException e) {
					log.warn("Error closing ftp connection", e);
				}
				log.debug("Ftp session disconnected");
			}
		}
	}

	@Override
	public boolean get(final String localDir, final String remoteFile) throws RuntimeException {
		return executeInSession(new FtpSessionCallback<Boolean>() {
			@Override
			public Boolean execute(FtpService sessionAwareFtpService) throws IOException {
				return sessionAwareFtpService.get(localDir, remoteFile);
			}
		});
	}

	@Override
	public boolean rm(final String remoteFile) throws RuntimeException {
		return executeInSession(new FtpSessionCallback<Boolean>() {
			@Override
			public Boolean execute(FtpService sessionAwareFtpService) throws IOException {
				return sessionAwareFtpService.rm(remoteFile);
			}
		});
	}

	@Override
	public void put(final InputStream is, final String dest) throws RuntimeException {
		executeInSession(new FtpSessionCallback<Void>() {
			@Override
			public Void execute(FtpService sessionAwareFtpService) throws IOException {
				sessionAwareFtpService.put(is, dest);
				return null;
			}
		});
	}

	protected boolean get(String localDir, String remoteFile, FTPClient ftp) throws IOException {
		String fileName = FilenameUtils.getName(remoteFile);
		OutputStream output = new FileOutputStream(localDir + "/" + fileName);
		try {
			log.info("Getting remote file: " + remoteFile);
			return ftp.retrieveFile(remoteFile, output);
		} finally {
			try {
				output.close();
			} catch (RuntimeException e) {
				log.warn("Error closing output stream", e);
			}
		}
	}

	protected boolean rm(String remoteFile, FTPClient ftp) throws IOException {
		log.info("Deleting remote file: " + remoteFile);
		return ftp.deleteFile(remoteFile);
	}

	protected void put(InputStream is, String dest, FTPClient ftp) throws IOException {
		log.info("Storing remote file in: " + dest);
		ftp.storeFile(dest, is);
	}

	public List<RemoteFile> listFiles(final String path, final String wildcardPattern, final boolean onlyFiles,
			final FileOrder fileOrder) throws RuntimeException {
		return executeInSession(new FtpSessionCallback<List<RemoteFile>>() {
			@Override
			public List<RemoteFile> execute(FtpService sessionAwareFtpService) throws IOException {
				return sessionAwareFtpService.listFiles(path, wildcardPattern, onlyFiles, fileOrder);
			}
		});
	}
	
	@Override
	public void put(String content, final String dest) {
		try {
			put(new ByteArrayInputStream(content.getBytes("utf-8")), dest );
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Could not write byte in utf-8", ex);
		}
	}

	protected List<RemoteFile> listFiles(String path, final String wildcardPattern, final boolean onlyFiles,
			FileOrder fileOrder, FTPClient ftp) throws IOException {
		FTPFileFilter ftpFileFilter = new FTPFileFilter() {
			@Override
			public boolean accept(FTPFile file) {
				if (file == null)
					return false;
				if (onlyFiles && file.isDirectory())
					return false;
				if (wildcardPattern != null)
					return FilenameUtils.wildcardMatch(file.getName(), wildcardPattern);
				return true;
			}
		};
		FTPFile[] files = ftp.listFiles(path, ftpFileFilter);
		//Can't use mListDir because MLSD command may not be supported by the FTP server
//		FTPFile[] files = ftp.mlistDir(path, ftpFileFilter);
		FTPFileComparators.sort(files, fileOrder);

		List<RemoteFile> result = new ArrayList<RemoteFile>(files.length);
		for (FTPFile f : files)
			result.add(new RemoteFile(f.getName(), path, f.getTimestamp().getTimeInMillis(), f.getSize()));

		if(log.isTraceEnabled()){
			log.trace("File Listings for path: " + path + " and order " + fileOrder);
			for (int i = 0; i < files.length; i++) {
				log.trace(files[i].getRawListing());
			}
		}

		return result;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getDataTimeout() {
		return dataTimeout;
	}

	public void setDataTimeout(int dataTimeout) {
		this.dataTimeout = dataTimeout;
	}

}
