package eu.thermz.java.ssh;

public interface SshSessionCallback<T> {
	public T execute(SshService sessionAwareSshService);
}
