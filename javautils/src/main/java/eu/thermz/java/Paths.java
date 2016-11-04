package eu.thermz.java;

import java.io.File;

/**
 * Paths helper class
 * @author riccardo
 */
public class Paths {
	
	/**
	 * Utility path helper that simulate almost the same behaviour of Java 7 <tt>Paths.get</tt> method
	 * But it returns the String path instead of the Path object.
	 * @param path
	 * @return the final path without any duplicate slash or backslash
	 */
	public static String get(String ... path){
		if(path.length == 0)
			return "";
		if(path.length == 1)
			return path[0];
		
		String current = "";
		for (String p : path)
			current = new File(current,p).getPath();
		
		return current;
	}
	
	/**
	 * Works like get, but the first argument is considered to be a relative path.
	 * @param path
	 * @return 
	 */
	public static String getRelative(String ... path){
		if(path.length == 0)
			return "";
		if(path.length == 1)
			return path[0];
		
		String current = path[0];
		
		for (int i = 1; i < path.length; i++)
			current = new File(current,path[i]).getPath();
		
		return current;
	}
	
}
