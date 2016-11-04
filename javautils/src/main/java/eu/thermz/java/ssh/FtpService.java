package eu.thermz.java.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FtpService {

	/***
	 * Executes the operations called on the callback parameter in the same ftp Session
	 * 
	 */
	public <T> T executeInSession(FtpSessionCallback<T> callback);

	public void put(InputStream is, String dest) throws IOException;
	public void put(String content, String dest) throws IOException;

	public boolean get(String localDir, String remoteFile) throws IOException;
	
	public boolean rm(String remoteFile) throws IOException;

	public List<RemoteFile> listFiles(String path, String wildcardPattern, boolean onlyFiles, FileOrder fileOrder)
			throws IOException;
	
	public String getHost();

	public void setHost(String host);

	public void setPort(int port);

	public void setConnectionTimeout(int connectionTimeout);

	public void setUsername(String username);

	public void setPassword(String password);

	public void setDataTimeout(int dataTimeout);
}