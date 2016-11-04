package eu.thermz.java.ssh;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.jcraft.jsch.ChannelSftp.LsEntry;

public class LsEntryComparators {
	private LsEntryComparators() {
	};

	public static final Comparator<LsEntry> SIZE_COMPARATOR = new Comparator<LsEntry>() {
		@Override
		public int compare(LsEntry o1, LsEntry o2) {
			return o1.getAttrs().getSize() < o2.getAttrs().getSize() ? -1 : o1.getAttrs().getSize() == o2.getAttrs()
					.getSize() ? 0 : 1;
		}
	};

	public static final Comparator<LsEntry> SIZE_COMPARATOR_REVERSE = new Comparator<LsEntry>() {
		@Override
		public int compare(LsEntry o1, LsEntry o2) {
			return o1.getAttrs().getSize() < o2.getAttrs().getSize() ? 1 : o1.getAttrs().getSize() == o2.getAttrs()
					.getSize() ? 0 : -1;
		}
	};

	public static final Comparator<LsEntry> NAME_COMPARATOR = new Comparator<LsEntry>() {
		@Override
		public int compare(LsEntry o1, LsEntry o2) {
			return o1.getFilename().compareTo(o2.getFilename());
		}
	};

	public static final Comparator<LsEntry> NAME_COMPARATOR_REVERSE = new Comparator<LsEntry>() {
		@Override
		public int compare(LsEntry o1, LsEntry o2) {
			return -o1.getFilename().compareTo(o2.getFilename());
		}
	};

	public static final Comparator<LsEntry> TIMESTAMP_COMPARATOR = new Comparator<LsEntry>() {
		@Override
		public int compare(LsEntry o1, LsEntry o2) {
			return o1.getAttrs().getMTime() < o2.getAttrs().getMTime() ? -1 : o1.getAttrs().getMTime() == o2.getAttrs()
					.getMTime() ? 0 : 1;
		}
	};

	public static final Comparator<LsEntry> TIMESTAMP_COMPARATOR_REVERSE = new Comparator<LsEntry>() {
		@Override
		public int compare(LsEntry o1, LsEntry o2) {
			return -o1.getAttrs().getMTime() < o2.getAttrs().getMTime() ? -1 : o1.getAttrs().getMTime() == o2
					.getAttrs().getMTime() ? 0 : 1;
		}
	};

	public static void sort(List<LsEntry> files, FileOrder fileOrder) {
		if (fileOrder == null)
			fileOrder = FileOrder.DEFAULT;
		switch (fileOrder) {
		case SIZE:
			Collections.sort(files, LsEntryComparators.SIZE_COMPARATOR);
			break;
		case SIZE_REVERSE:
			Collections.sort(files, LsEntryComparators.SIZE_COMPARATOR_REVERSE);
			break;
		case NAME:
			Collections.sort(files, LsEntryComparators.NAME_COMPARATOR);
			break;
		case NAME_REVERSE:
			Collections.sort(files, LsEntryComparators.NAME_COMPARATOR_REVERSE);
			break;
		case TIMESTAMP:
			Collections.sort(files, LsEntryComparators.TIMESTAMP_COMPARATOR);
			break;
		case TIMESTAMP_REVERSE:
			Collections.sort(files, LsEntryComparators.TIMESTAMP_COMPARATOR_REVERSE);
			break;
		case DEFAULT_REVERSE:
			Collections.reverse(files);
			break;
		default:
		}
	}

	public static void sort(LsEntry[] files, FileOrder fileOrder) {
		if (fileOrder == null)
			fileOrder = FileOrder.DEFAULT;
		switch (fileOrder) {
		case SIZE:
			Arrays.sort(files, LsEntryComparators.SIZE_COMPARATOR);
			break;
		case SIZE_REVERSE:
			Arrays.sort(files, LsEntryComparators.SIZE_COMPARATOR_REVERSE);
			break;
		case NAME:
			Arrays.sort(files, LsEntryComparators.NAME_COMPARATOR);
			break;
		case NAME_REVERSE:
			Arrays.sort(files, LsEntryComparators.NAME_COMPARATOR_REVERSE);
			break;
		case TIMESTAMP:
			Arrays.sort(files, LsEntryComparators.TIMESTAMP_COMPARATOR);
			break;
		case TIMESTAMP_REVERSE:
			Arrays.sort(files, LsEntryComparators.TIMESTAMP_COMPARATOR_REVERSE);
			break;
		case DEFAULT_REVERSE:
			ArrayUtils.reverse(files);
			break;
		default:
		}
	}
}
