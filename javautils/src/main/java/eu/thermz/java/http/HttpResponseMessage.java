package eu.thermz.java.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponseMessage {
	private Integer					responseCode					= 0;
	private String					responseString					= "";
	private String					responseContentType				= "";
	private String					responseContentDisposition		= "";
	Map<String, List<String>>		responseHeaders					= new HashMap<String, List<String>>();
	
	public HttpResponseMessage(Integer responseCode, String responseString, String responseContentType,
			String responseContentDisposition, Map<String, List<String>> responseHeaders) {
		super();
		this.responseCode = responseCode;
		this.responseString = responseString;
		this.responseContentType = responseContentType;
		this.responseContentDisposition = responseContentDisposition;
		this.responseHeaders = responseHeaders;
	}

	public Integer getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponseString() {
		return responseString;
	}
	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}
	public String getResponseContentType() {
		return responseContentType;
	}
	public void setResponseContentType(String responseContentType) {
		this.responseContentType = responseContentType;
	}
	public String getResponseContentDisposition() {
		return responseContentDisposition;
	}
	public void setResponseContentDisposition(String responseContentDisposition) {
		this.responseContentDisposition = responseContentDisposition;
	}
}