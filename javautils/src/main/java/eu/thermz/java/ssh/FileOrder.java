package eu.thermz.java.ssh;

import java.io.File;
import java.util.Comparator;

import org.apache.commons.io.comparator.DefaultFileComparator;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.comparator.PathFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;

public enum FileOrder {
	DEFAULT (DefaultFileComparator.DEFAULT_COMPARATOR),
	DEFAULT_REVERSE (DefaultFileComparator.DEFAULT_REVERSE),
	NAME (NameFileComparator.NAME_COMPARATOR), 
	NAME_INSENSITIVE (NameFileComparator.NAME_INSENSITIVE_COMPARATOR), 
	NAME_REVERSE (NameFileComparator.NAME_REVERSE), 
	NAME_INSENSITIVE_REVERSE (NameFileComparator.NAME_INSENSITIVE_REVERSE), 
	TIMESTAMP (LastModifiedFileComparator.LASTMODIFIED_COMPARATOR), 
	TIMESTAMP_REVERSE (LastModifiedFileComparator.LASTMODIFIED_REVERSE),
	SIZE (SizeFileComparator.SIZE_COMPARATOR), 
	SIZE_REVERSE (SizeFileComparator.SIZE_REVERSE),
	PATH (PathFileComparator.PATH_COMPARATOR), 
	PATH_REVERSE (PathFileComparator.PATH_REVERSE),
	PATH_INSENSITIVE (PathFileComparator.PATH_INSENSITIVE_COMPARATOR), 
	PATH_INSENSITIVE_REVERSE (PathFileComparator.PATH_INSENSITIVE_REVERSE)
	;

	private Comparator<File> comparator;

	private FileOrder(Comparator<File> c) {
		this.comparator = c;
	}

	public Comparator<File> getComparator() {
		return comparator;
	}

}
