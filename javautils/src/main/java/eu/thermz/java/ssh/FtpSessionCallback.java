package eu.thermz.java.ssh;

import java.io.IOException;

public interface FtpSessionCallback<T> {
	public T execute(FtpService sessionAwareFtpService) throws IOException;
}