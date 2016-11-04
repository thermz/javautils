package eu.thermz.java;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Class that has the responsibility to deal with CSV files.
 * 
 * @author riccardo
 */
public class CSVUtils {
	static final String NEWLINE = System.getProperty("line.separator");
	
	/**
	 * Create the output String csv using the list of objects in input.
	 * 
	 * @param <T> The type that maps a CSV row
	 * @param objects The list of objects of type T
	 * @param clazz The class of type T (mandatory for reflection API).
	 * @param header A boolean flag that tells whether there is or not the header on top of the CSV
	 * @param endDelimeter Flag that indicates whether the CSV should end with a delimeter or not
	 * @param delimeter The delimeter character in the CSV. Usually it's comma ',' or semicolon ';'.
	 * @return The String output CSV
	 */
	public static <T> String toCSV(List<T> objects, Class<T> clazz, boolean header, boolean endDelimeter, final char delimeter) {
		StringBuilder csv = new StringBuilder("");

		//CSV header first line
		if(header)
			csv.append(forEachGetterPrint(clazz, new GetterBehaviour() {
				public String behaviour(Method m) {
					return getFieldName(m.getName()) + delimeter;
				}
			})).append(NEWLINE);

		//CSV records
		for (final T object : objects) {
			String record = forEachGetterPrint(clazz, new GetterBehaviour() {
				public String behaviour(Method m) throws Exception {
					return m.invoke(object) + Character.toString(delimeter);
				}
			});
			record = (endDelimeter)?record:truncateLastChar(record);
			csv.append(record).append(NEWLINE);
		}

		return csv.toString();

	}
	
	/**
	 * Create the output String csv using the list of objects in input. 
	 * As expected the CSV doesn't end with delimeter.
	 * 
	 * @param <T> The type that maps a CSV row
	 * @param objects The list of objects of type T
	 * @param clazz The class of type T (mandatory for reflection API).
	 * @param header A boolean flag that tells whether there is or not the header on top of the CSV
	 * @param delimeter The delimeter character in the CSV. Usually it's comma ',' or semicolon ';'.
	 * @return The String output CSV
	 */
	public static <T> String toCSV(List<T> objects, Class<T> clazz, boolean header, final char delimeter) {
		return toCSV(objects, clazz, header, false, delimeter);
	}

	static String forEachGetterPrint(Class<?> clazz, GetterBehaviour behaviour) {
		StringBuilder sb = new StringBuilder("");
		try {
			PropertyDescriptor[] pd = Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors();
			Arrays.sort(pd, new Comparator<PropertyDescriptor>() {
				public int compare(PropertyDescriptor pd1, PropertyDescriptor pd2) {
					CSVOrder order1 = pd1.getReadMethod().getAnnotation(CSVOrder.class);
					CSVOrder order2 = pd2.getReadMethod().getAnnotation(CSVOrder.class);
					Integer w1 = (order1 == null)?Integer.MAX_VALUE:order1.value();
					Integer w2 = (order2 == null)?Integer.MAX_VALUE:order2.value();
					return w1.compareTo(w2);
				}
			} );
			for (PropertyDescriptor pd1 : pd) {
				sb.append(behaviour.behaviour(pd1.getReadMethod()));
			}

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return sb.toString();
	}

	static String truncateLastChar(String s){
		return s.substring(0, s.length() - 1);
	}
	
	static interface GetterBehaviour {
		public String behaviour(Method m) throws Exception;
	}

	static String getFieldName(String getterName) {
		return Introspector.decapitalize(getterName.substring(getterName.startsWith("is") ? 2 : 3));
	}
}
