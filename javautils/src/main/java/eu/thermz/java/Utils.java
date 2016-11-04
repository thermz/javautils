package eu.thermz.java;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import static java.util.Collections.emptyList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import static java.util.Optional.ofNullable;
import java.util.Random;
import java.util.TimeZone;
import java.util.stream.Collectors;
import static java.util.stream.IntStream.rangeClosed;
import static java.util.stream.Stream.concat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.activation.DataSource;
import javax.naming.InitialContext;

/**
 * Class containing utility methods
 * @author riccardo
 */
public class Utils {
	
	public static int countOccourences(String line, char occChar){
		return line.length() - line.replace(String.valueOf(occChar), "").length();
	}
	
	public static boolean hasContent(Object o){
		return !(o == null || (o instanceof String && ((String)o).isEmpty() ) );
	}
	
	public static boolean hasNoContent(Object o){
		return !hasContent(o);
	}
	
	public static <T> List<T> join(List<? extends T> l1, List<? extends T> l2){
		List<T> joined = new ArrayList<T>();
		joined.addAll(l1);
		joined.addAll(l2);
		return joined;
	}

	public static InputStream getInputStreamFromFS(File f){
		return getInputStreamFromFS(f.getAbsolutePath());
	}
	
	public static InputStream getInputStreamFromFS(String path){
		InputStream is = null;
		try {
			is = new FileInputStream( path );
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		return new BufferedInputStream(is);
	}
	
	public static BufferedReader getBuffReaderFromFS(File f){
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return br;
	}
	
	public static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
	
	public static void writeFile(String filePath, String content){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filePath, "UTF-8");
			writer.print( content );
			writer.flush();
			String outcome = (writer.checkError())?"unsuccesful":"succesful";
		} catch (IOException ex) {
			throw new RuntimeException("Could not write file", ex);
		} finally {
			if(writer!=null) 
				writer.close();
		}
	}
	
	public static void writeFile(File file, String content){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file, "UTF-8");
			writer.print( content );
			writer.flush();
			String outcome = (writer.checkError())?"unsuccesful":"succesful";
		} catch (IOException ex) {
			throw new RuntimeException("Could not write file", ex);
		} finally {
			if(writer!=null) 
				writer.close();
		}
	}
	
	/**
	 * Unzip utility method
	 *
	 * @param zipFile input zip file
	 * @param outputFolder zip file output folder
	 */
	public static void unZipIt(String zipFile, String outputFolder) {

		byte[] buffer = new byte[1024];
		Collection<Closeable> closeables = new ArrayList<>();
		try {
			//create output directory is not exists

			//get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			closeables.add(zis);
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder , fileName);

				FileOutputStream fos = new FileOutputStream(newFile);
				closeables.add(fos);
				
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				
				ze = zis.getNextEntry();
			}

			zis.closeEntry();

		} catch (IOException ex) {
			throw new RuntimeException("could not unzip", ex);
		} finally {
			for (Closeable closeable : closeables){
				smartClose(closeable);
			}
		}
	}
	
	public static void smartClose(Closeable closeable){
		if(closeable != null)
			try{ closeable.close(); } catch (Exception ignored) {}
	}

	public static class Coordinates { double lat,lon;
		public Coordinates(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}
		public double getLat() {
			return lat;
		}
		public double getLon() {
			return lon;
		}
	}
	public static double getDistance(Coordinates hz, Coordinates cell) {
	
		double earthRadius = 6371000; //meters
		double dLat = Math.toRadians(cell.getLat() - hz.getLat());
		double dLng = Math.toRadians(cell.getLon() - hz.getLon());
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(hz.getLat())) * Math.cos(Math.toRadians(hz.getLon()))
				* Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		float dist = (float) (earthRadius * c);
		double distInKm = dist / 1000;
		return distInKm;
	}
	
	public static boolean isInt(String str){
		return str.matches("^-?\\d+$");
	}
	public static boolean isNotInt(String str){
		return !isInt(str);
	}
	
	public static boolean isEmpty(Collection<?> c){
		if(c == null)
			return true;
		return c.isEmpty();
	}
	
	public static boolean isNotEmpty(Collection<?> c){
		return !isEmpty(c);
	}

	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static Random rnd = new Random();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        rangeClosed(0, len).forEach(
                i -> sb.append(AB.charAt(rnd.nextInt(AB.length()))));
        return sb.toString();
    }

    private Utils() {
    }

    public static List<Field> getFields(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        List<Field> parentFields = ofNullable(getFields(clazz.getSuperclass())).orElse(emptyList());
        return concat(parentFields.stream(),
                asList(clazz.getDeclaredFields()).stream())
                .collect(Collectors.toList());
    }

    public static <T> T instanceEmpty(final Class<T> clazz) {
        return unchecked(() -> clazz.getConstructor().newInstance());
    }

    public static <T> T instanceByString(final Class<T> clazz, final String argument) {
        return unchecked(() -> clazz.getConstructor(String.class).newInstance(argument));
    }

    /**
     * Utility method for unchecking every java checked exception in a single
     * {@link RuntimeException}. <br>
     * This method overload needs a return value.
     *
     * @param u Behaviour closure
     * @param defaultValue
     * @return
     */
    public static <T> T unchecked(Uncheck<T> u, T defaultValue) {
        T t = null;
        try {
            t = u.uncheck();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (t == null) {
            t = defaultValue;
        }
        return t;
    }

    /**
     * Utility method for unchecking every java checked exception in a single
     * {@link RuntimeException} This method works for void return.
     *
     * @param u Behaviour closure
     */
    public static void unchecked(UncheckVoid u) {
        try {
            u.uncheck();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utility method for unchecking every java checked exception in a single
     * {@link RuntimeException}
     *
     * @param u Behaviour closure
     * @return
     */
    public static <T> T unchecked(Uncheck<T> u) {
        return unchecked(u, null);
    }

    private static DateFormat getUTCDateFormat() {
        return unchecked(() -> {
            //2014-03-11 16:38:24 UTC
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            return f;
        });
    }

    public static Date getUTCDate(final String dateString) {
        return unchecked(() -> getUTCDateFormat().parse(dateString));
    }

    public static String formatUTCDate(final Date date) {
        return getUTCDateFormat().format(date);
    }

	public static <T> List<T> iteratorToList(Iterator<T> iter) {
        List<T> copy = new ArrayList<>();
        while (iter.hasNext()) {
            copy.add(iter.next());
        }
        return copy;
    }

    public static Double percentage(Double num, Double total) {
        return num * 100 / total;
    }

    public static boolean areNotNull(String... args) {
        for (String string : args) {
            if (string == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotEmpty(String o) {
        return o != null && !o.isEmpty();
    }

    public static String urlEncode(String urlToEncode) {
        return unchecked(() -> URLEncoder.encode(urlToEncode, "UTF-8"));
    }

    public static Double round(double num, int places) {
        return new BigDecimal(num).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    public static <T> T getJNDIResource(Class<T> clazz, String jndiString) {
        return (T) unchecked(() -> new InitialContext().lookup(jndiString));
    }

    public static DataSource getDataSource(String dsSpecificName) {
        return getJNDIResource(DataSource.class, "java:jboss/datasources/" + dsSpecificName);
    }

    public static boolean ping(String ip) {
        return unchecked(() -> InetAddress.getByName(ip).isReachable(5));
    }

}
