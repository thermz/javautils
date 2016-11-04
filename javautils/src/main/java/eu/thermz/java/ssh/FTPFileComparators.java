package eu.thermz.java.ssh;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.net.ftp.FTPFile;

public class FTPFileComparators {
	private FTPFileComparators() {
	};

	public static final Comparator<FTPFile> SIZE_COMPARATOR = new Comparator<FTPFile>() {
		@Override
		public int compare(FTPFile o1, FTPFile o2) {
			return o1.getSize() < o2.getSize() ? -1 : o1.getSize() == o2.getSize() ? 0 : 1;
		}
	};

	public static final Comparator<FTPFile> SIZE_COMPARATOR_REVERSE = new Comparator<FTPFile>() {
		@Override
		public int compare(FTPFile o1, FTPFile o2) {
			return o1.getSize() < o2.getSize() ? 1 : o1.getSize() == o2.getSize() ? 0 : -1;
		}
	};

	public static final Comparator<FTPFile> NAME_COMPARATOR = new Comparator<FTPFile>() {
		@Override
		public int compare(FTPFile o1, FTPFile o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	public static final Comparator<FTPFile> NAME_COMPARATOR_REVERSE = new Comparator<FTPFile>() {
		@Override
		public int compare(FTPFile o1, FTPFile o2) {
			return -o1.getName().compareTo(o2.getName());
		}
	};

	public static final Comparator<FTPFile> TIMESTAMP_COMPARATOR = new Comparator<FTPFile>() {
		@Override
		public int compare(FTPFile o1, FTPFile o2) {
			return o1.getTimestamp().compareTo(o2.getTimestamp());
		}
	};

	public static final Comparator<FTPFile> TIMESTAMP_COMPARATOR_REVERSE = new Comparator<FTPFile>() {
		@Override
		public int compare(FTPFile o1, FTPFile o2) {
			return -o1.getTimestamp().compareTo(o2.getTimestamp());
		}
	};

	public static void sort(List<FTPFile> files, FileOrder fileOrder, boolean reverse) {
		if (fileOrder == null)
			fileOrder = FileOrder.DEFAULT;
		switch (fileOrder) {
		case SIZE:
			Collections.sort(files, FTPFileComparators.SIZE_COMPARATOR);
			break;
		case SIZE_REVERSE:
			Collections.sort(files, FTPFileComparators.SIZE_COMPARATOR_REVERSE);
			break;
		case NAME:
			Collections.sort(files, FTPFileComparators.NAME_COMPARATOR);
			break;
		case NAME_REVERSE:
			Collections.sort(files, FTPFileComparators.NAME_COMPARATOR_REVERSE);
			break;
		case TIMESTAMP:
			Collections.sort(files, FTPFileComparators.TIMESTAMP_COMPARATOR);
			break;
		case TIMESTAMP_REVERSE:
			Collections.sort(files, FTPFileComparators.TIMESTAMP_COMPARATOR_REVERSE);
			break;
		case DEFAULT_REVERSE:
			Collections.reverse(files);
		default:
		}
	}

	public static void sort(FTPFile[] files, FileOrder fileOrder) {
		if (fileOrder == null)
			fileOrder = FileOrder.DEFAULT;
		switch (fileOrder) {
		case SIZE:
			Arrays.sort(files, FTPFileComparators.SIZE_COMPARATOR);
			break;
		case SIZE_REVERSE:
			Arrays.sort(files, FTPFileComparators.SIZE_COMPARATOR_REVERSE);
			break;
		case NAME:
			Arrays.sort(files, FTPFileComparators.NAME_COMPARATOR);
			break;
		case NAME_REVERSE:
			Arrays.sort(files, FTPFileComparators.NAME_COMPARATOR_REVERSE);
			break;
		case TIMESTAMP:
			Arrays.sort(files, FTPFileComparators.TIMESTAMP_COMPARATOR);
			break;
		case TIMESTAMP_REVERSE:
			Arrays.sort(files, FTPFileComparators.TIMESTAMP_COMPARATOR_REVERSE);
			break;
		case DEFAULT_REVERSE:
			ArrayUtils.reverse(files);
		default:
		}
	}
}
