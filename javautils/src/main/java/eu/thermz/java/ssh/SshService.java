package eu.thermz.java.ssh;

import java.io.InputStream;
import java.util.List;

public interface SshService {
	/***
	 * Executes the operations called on the callback parameter in the same ssh Session
	 * 
	 */
	public <T> T executeInSession(SshSessionCallback<T> callback);

	public CommandResult exec(String command);

	public void put(InputStream is, String dest);

	public boolean get(String localDir, String remoteFile);

	public boolean rm(String remoteFile);

	public List<RemoteFile> listFiles(String path, String wildcardPattern, boolean onlyFiles, FileOrder fileOrder);
	
	public String getHost();
}
