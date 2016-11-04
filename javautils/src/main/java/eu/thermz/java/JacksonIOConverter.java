package eu.thermz.java;

import static eu.thermz.java.Utils.unchecked;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;


public class JacksonIOConverter {
	
	private static ObjectMapper getDefaultObjectMapper(){
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
		om.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		return om;
	}
	
	public static String toJSON(Object o){
		return unchecked( () -> getDefaultObjectMapper().writeValueAsString(o), "{}" );
	}
	
	public static <T> T fromJSON(String s, final Class<T> clazz){
		return unchecked(() -> getDefaultObjectMapper().readValue(s, clazz));
	}
	
}
