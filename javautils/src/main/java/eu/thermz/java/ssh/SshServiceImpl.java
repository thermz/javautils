package eu.thermz.java.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import org.apache.log4j.Logger;

/**
 * Utility wrapper around JSch classes
 * 
 * @author 
 */
public class SshServiceImpl implements SshService, FtpService {

	private static final Logger log = Logger.getLogger(SshServiceImpl.class);
	
	private JSch jsch = new JSch();

	private String host;
	private int port = 22;
	private int connectionTimeout = 15000;
	private int dataTimeout = 30000;

	// ssh credentials. Username and one between password and privkey are mandatory
	private String username;
	private String password;
	private String privkey;
	private String keyPassphrase;
	private String pubkey;
	private boolean identityChanged = true;

	// This class is an FtpService too
	@Override
	public <T> T executeInSession(FtpSessionCallback<T> callback) {
		Session session = null;
		try {
			session = createAndConnectSession();
			FtpService proxy = (FtpService) createSessionAwareProxy(session);
			return callback.execute(proxy);
		} catch(IOException e){
			throw new RuntimeException("Exception during SSH Connection", e);
		} finally {
			if (session != null && session.isConnected())
				session.disconnect();
			log.debug("SSH Session disconnected");
		}
	}

	@Override
	public <T> T executeInSession(SshSessionCallback<T> callback) throws RuntimeException {
		Session session = null;
		try {
			session = createAndConnectSession();
			SshService proxy = (SshService) createSessionAwareProxy(session);
			return callback.execute(proxy);
		} finally {
			if (session != null && session.isConnected())
				session.disconnect();
			log.debug("SSH Session disconnected");
		}
	}

	@Override
	public final CommandResult exec(final String command) throws RuntimeException {
		return executeInSession(new SshSessionCallback<CommandResult>() {
			@Override
			public CommandResult execute(SshService sessionAwareSshService) throws RuntimeException {
				return sessionAwareSshService.exec(command);
			}
		});
	}

	@Override
	public void put(final InputStream is, final String dest) throws RuntimeException {
		executeInSession(new SshSessionCallback<Void>() {
			@Override
			public Void execute(SshService sessionAwareSshService) throws RuntimeException {
				sessionAwareSshService.put(is, dest);
				return null;
			}
		});
	}
	
	@Override
	public void put(String content, final String dest) throws RuntimeException {
		try {
			put(new ByteArrayInputStream(content.getBytes("utf-8")), dest );
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Could not write byte in utf-8", ex);
		}
	}

	@Override
	public boolean get(final String localDir, final String remoteFile) throws RuntimeException {
		return executeInSession(new SshSessionCallback<Boolean>() {
			@Override
			public Boolean execute(SshService sessionAwareSshService) throws RuntimeException {
				return sessionAwareSshService.get(localDir, remoteFile);
			}
		});
	}

	@Override
	public boolean rm(final String remoteFile) throws RuntimeException {
		return executeInSession(new SshSessionCallback<Boolean>() {
			@Override
			public Boolean execute(SshService sessionAwareSshService) throws RuntimeException {
				return sessionAwareSshService.rm(remoteFile);
			}
		});
	}

	@Override
	public List<RemoteFile> listFiles(final String path, final String wildcardPattern, final boolean onlyFiles,
			final FileOrder fileOrder) throws RuntimeException {
		return executeInSession(new SshSessionCallback<List<RemoteFile>>() {
			@Override
			public List<RemoteFile> execute(SshService sessionAwareSshService) throws RuntimeException {
				return sessionAwareSshService.listFiles(path, wildcardPattern, onlyFiles, fileOrder);
			}
		});
	}

	protected List<RemoteFile> listFiles(final String path, final String wildcardPattern, final boolean onlyFiles,
			final FileOrder fileOrder, Session session) throws IOException {

		return execSftpCommand(session, new SftpCallback<List<RemoteFile>>() {
			@SuppressWarnings("unchecked")
			@Override
			public List<RemoteFile> exec(ChannelSftp sftpChan) throws SftpException {
				Vector<LsEntry> ls = sftpChan.ls(path);
				ArrayList<LsEntry> filtered = new ArrayList<LsEntry>();
				for (LsEntry lsEntry : ls) {
					if (onlyFiles && lsEntry.getAttrs().isDir())
						continue;
					if (wildcardPattern != null && !FilenameUtils.wildcardMatch(lsEntry.getFilename(), wildcardPattern))
						continue;
					filtered.add(lsEntry);
				}

				LsEntryComparators.sort(filtered, fileOrder);

				log.debug("File Listings for path: " + path + " and order " + fileOrder);
				for (int i = 0; i < filtered.size(); i++) {
					log.debug(filtered.get(i).toString());
				}

				ArrayList<RemoteFile> result = new ArrayList<RemoteFile>(filtered.size());
				for (LsEntry e : filtered)
					result.add(new RemoteFile(e.getFilename(), path, e.getAttrs().getMTime() * 1000L, e.getAttrs()
							.getSize()));

				return result;

			}
		});

	}

	protected CommandResult exec(String command, Session session) throws RuntimeException {
		log.debug("Executing ssh command:\n" + command);
		Channel channel = null;
		try {
			channel = session.openChannel("exec");
			log.debug("SSH Exec Channel opened");
			((ChannelExec) channel).setCommand(command);
			String out = pollChannelUntilClosed(channel, dataTimeout);
			return new CommandResult(channel.getExitStatus(), out);
		} catch (JSchException e) {
			throw new RuntimeException(e);
		} finally {
			if (channel != null && channel.isConnected())
				channel.disconnect();
			log.debug("SSH Exec Channel disconnected");
		}
	}

	protected void put(final InputStream is, final String dest, Session session) throws RuntimeException {
		// if (log.isTraceEnabled())
		// log.debug("Uploaded file Dump:\n" + fileContent);
		log.info("Storing remote file in: " + dest);
		execSftpCommand(session, new SftpCallback<Void>() {
			@Override
			public Void exec(ChannelSftp sftpChan) throws SftpException {
				sftpChan.put(is, dest);
				return null;
			}
		});
	}

	protected boolean get(final String localDir, final String remoteFile, Session session) throws RuntimeException {
		return execSftpCommand(session, new SftpCallback<Boolean>() {
			@Override
			public Boolean exec(ChannelSftp sftpChan) throws SftpException {
				log.info("Getting remote file: " + remoteFile);
				sftpChan.get(remoteFile, localDir);
				return true;
			}
		});
	}

	protected boolean rm(final String remoteFile, Session session) throws IOException {
		return execSftpCommand(session, new SftpCallback<Boolean>() {
			@Override
			public Boolean exec(ChannelSftp sftpChan) throws SftpException {
				log.info("Deleting remote file: " + remoteFile);
				sftpChan.rm(remoteFile);
				return true;
			}
		});
	}

	private <T> T execSftpCommand(Session session, SftpCallback<T> cb) throws RuntimeException {
		Channel channel = null;
		try {
			// Upload the file
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect(connectionTimeout);
			log.debug("SSH Sftp Channel opened");
			return cb.exec((ChannelSftp) channel);
		} catch (JSchException e) {
			throw new RuntimeException(e);
		} catch (SftpException e) {
			throw new RuntimeException(e);
		} finally {
			if (channel != null && channel.isConnected())
				channel.disconnect();
			log.debug("SSH Sftp Channel disconnected");
		}
	}

	private Object createSessionAwareProxy(final Session finalSession) throws IllegalArgumentException {
		final SshServiceImpl sshServiceImpl = this;
		Object proxy = Proxy.newProxyInstance(SshServiceImpl.class.getClassLoader(), new Class[] { SshService.class,
				FtpService.class }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
				// Find a method with same name and a Session parameter
				Class<?>[] oldParameterTypes = m.getParameterTypes();
				Class<?>[] newParameterTypes = Arrays.copyOf(oldParameterTypes, oldParameterTypes.length + 1);
				newParameterTypes[oldParameterTypes.length] = Session.class;

				Object[] newArgs = Arrays.copyOf(args, args.length + 1);
				newArgs[args.length] = finalSession;

				// this MUST have an inner method with the same name and a Session parameter after the
				// interface method parameters for every interface method
				Method sessionAwareMethod = SshServiceImpl.class.getDeclaredMethod(m.getName(), newParameterTypes);

				try { // execute the method
					return sessionAwareMethod.invoke(sshServiceImpl, newArgs);
				} catch (InvocationTargetException e) {
					throw e.getTargetException();
				}
			}
		});
		return proxy;
	}

	private Session createAndConnectSession() throws RuntimeException {
		checkIdentityChanged();

		Session session = null;
		try {
			// Get the session
			session = jsch.getSession(username, host, port);
			if (password != null)
				session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setTimeout(dataTimeout);
			session.connect(connectionTimeout);
			log.debug("SSH Session Connected");
			return session;
		} catch (JSchException e) {
			if (session != null && session.isConnected())
				session.disconnect();
			throw new RuntimeException(e);
		}

	}

	private String pollChannelUntilClosed(Channel channel, long timeout) throws JSchException {
		long remainingTime = timeout;
		try {
			InputStream in = channel.getInputStream();
			channel.connect();

			StringBuilder out = new StringBuilder();

			byte[] tmp = new byte[1024];
			while (remainingTime > 0) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					out.append(new String(tmp, 0, i, "UTF8"));
				}
				if (channel.isClosed()) {
					log.debug(out.toString());
					log.debug("exit-status: " + channel.getExitStatus());

					return out.toString();
				}
				try {
					Thread.sleep(200);
					remainingTime -= 200;
				} catch (InterruptedException e) {
					log.warn("Channel polling interrupted! This shouldn't happen", e);
				}
			}
		} catch (IOException e) {
			throw new JSchException("IOException during ssh channel polling", e);
		}

		throw new JSchException("Channel hasn't been closed in " + timeout + " milliseconds");
	}

	private void checkIdentityChanged() throws RuntimeException {
		privkey = (privkey != null && privkey.length() > 0) ? privkey : null;
		pubkey = (pubkey != null && pubkey.length() > 0) ? pubkey : null;
		keyPassphrase = (keyPassphrase != null && keyPassphrase.length() > 0) ? keyPassphrase : null;

		if (!identityChanged || privkey == null)
			return;
		try {
			jsch.removeAllIdentity();
			jsch.addIdentity(privkey, pubkey, keyPassphrase == null ? null : keyPassphrase.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("impossible", e);
		} catch (JSchException e) {
			throw new RuntimeException(e);
		}
		identityChanged = false;
	}

	private static interface SftpCallback<T> {
		T exec(ChannelSftp sftpChan) throws SftpException;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
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

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getPrivkey() {
		return privkey;
	}

	public void setPrivkey(String privkey) {
		this.privkey = privkey;
		identityChanged = true;
	}

	public String getKeyPassphrase() {
		return keyPassphrase;
	}

	public void setKeyPassphrase(String keyPassphrase) {
		this.keyPassphrase = keyPassphrase;
		identityChanged = true;
	}

	public String getPubkey() {
		return pubkey;
	}

	public void setPubkey(String pubkey) {
		this.pubkey = pubkey;
		identityChanged = true;
	}

	public int getDataTimeout() {
		return dataTimeout;
	}

	public void setDataTimeout(int dataTimeout) {
		this.dataTimeout = dataTimeout;
	}

}
