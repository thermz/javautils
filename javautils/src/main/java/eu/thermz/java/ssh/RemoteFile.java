package eu.thermz.java.ssh;

import java.io.Serializable;

public class RemoteFile implements Serializable {
	private static final long serialVersionUID = 3739411687865985630L;

	private String name;
	private String path;
	private long mdate;
	private long size;

	public RemoteFile() {
	}

	public RemoteFile(String name, String path, long mdate, long size) {
		super();
		this.name = name;
		this.path = path;
		this.mdate = mdate;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getMdate() {
		return mdate;
	}

	public void setMdate(long mdate) {
		this.mdate = mdate;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("RemoteFile [name=").append(name).append(", path=").append(path)
				.append(", mdate=").append(mdate).append(", size=").append(size).append("]").toString();
	}

}
