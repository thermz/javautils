package eu.thermz.java.http;

import static eu.thermz.java.Utils.areNotNull;
import static eu.thermz.java.Utils.isNotEmpty;
import static eu.thermz.java.Utils.urlEncode;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger;

public class HttpOutgoingRequest {

	static Logger log = Logger.getLogger(HttpOutgoingRequest.class);
	private String					urlString;
	private String					method;
	private boolean					https;
	private String					authentication;
	private String					body="";
	private String					accept;
	
	protected Map<String, String>	headers		= new HashMap<String, String>();
	protected Map<String, String>	formParams	= new LinkedHashMap<String, String>();
	
	private String					contentEncoding					= null;
	private String					charsetEncoding					= "UTF-8";
	private boolean					preserveLine					= false;
	private Integer					timeout							= 120000;
	
	private HttpOutgoingRequest (){ }
	private HttpOutgoingRequest (String method, String uri){
		this.method = method;
		this.urlString = uri;
	}
	
	public static HttpOutgoingRequest GET 	  (String uri) { return new HttpOutgoingRequest ( "GET" 	, uri); }
	public static HttpOutgoingRequest POST 	  (String uri) { return new HttpOutgoingRequest ( "POST" 	, uri); }
	public static HttpOutgoingRequest PUT 	  (String uri) { return new HttpOutgoingRequest ( "PUT" 	, uri); }
	public static HttpOutgoingRequest DELETE  (String uri) { return new HttpOutgoingRequest ( "DELETE" 	, uri); }
	public static HttpOutgoingRequest HEAD 	  (String uri) { return new HttpOutgoingRequest ( "HEAD" 	, uri); }

	private void refreshFormBody() {
		int len = formParams.size();
		int cont = 0;
		body="";
		for (Map.Entry<String, String> entry : formParams.entrySet()) {
			cont++;
			if(cont<len){
				body+=entry.getKey()+"="+entry.getValue()+"&";
			} else { //last case
				body+=entry.getKey()+"="+entry.getValue();
			}
		} 
	}
	
	public static class HeadersBuilder {
		HttpOutgoingRequest httpo;
		
		/**
		 * Call this method if you have another header to insert after this one.
		 * 
		 * @param k
		 * @param v
		 * @return
		 */
		public HeadersBuilder h(String k, String v){
			if( areNotNull(k,v) )
				httpo.headers.put(k, v);
			return this;
		}
		/**
		 * Call this method if this is the last header you're going to insert.
		 *  
		 * @param k
		 * @param v
		 * @return
		 */
		public HttpOutgoingRequest $(String k, String v){
			h(k,v);
			return httpo;
		}
		void setHTTPOutgoingRequest(HttpOutgoingRequest httpor){
			httpo = httpor;
		}
	}
	
	public HttpOutgoingRequest withContentType(String contentType){
		headers.put("Content-Type", contentType);
		return this;
	}
	public HttpOutgoingRequest withContentTypeApplicationJson(){
		headers.put("Content-Type", "application/json");
		return this;
	}
	public HttpOutgoingRequest withContentTypeApplicationJsonCharsetUTF8(){
		headers.put("Content-Type", "application/json; charset=utf-8");
		return this;
	}
	public HttpOutgoingRequest withContentTypeFormUrlEncoded(){
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		return this;
	}
	
	public HeadersBuilder withHeaders(){
		HeadersBuilder hb = new HeadersBuilder();
		hb.setHTTPOutgoingRequest(this);
		return hb;
	}
	
	public HttpOutgoingRequest withBody(String aBody){
		body = aBody;
		return this;
	}
	public String getBody(){
		return body;
	}
	
	public HttpOutgoingRequest withFormParam(String key, String value){
		formParams.put(urlEncode(key), urlEncode(value));
		refreshFormBody();
		return this;
//		String parameters = "parameter1=" + URLEncoder.encode("SOMETHING","UTF-8");
	}
	public HttpOutgoingRequest withFormParams(Map<String,String> params){
		formParams = params;
		refreshFormBody();
		return this;
	}
	
	
	private HttpResponseMessage getResponse() throws Exception {
		log.debug("getResponse() --> start");

		URL reqURL = new URL(urlString);
		if (isHttps(urlString))
			return httpsConnect(reqURL);
		else
			return httpConnect(reqURL);
	}
	
	private boolean isHttps(String urlString){
		return urlString.startsWith("https://");
	}

	private HttpResponseMessage httpsConnect(URL reqURL) throws Exception {
		log.debug("httpsConnect(" + reqURL + ") --> start");
		HttpsURLConnection httpsCon = null;
		// patch HTTPS per accettare qualsiasi certificato
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		SSLContext sc = null;
		sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			
		// Create all-trusting host name verifier
//		HostnameVerifier allHostsValid = new HostnameVerifier() {
//			public boolean verify(String hostname, SSLSession session) {
//				return true;
//			}
//		};
		
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

		httpsCon = (HttpsURLConnection) reqURL.openConnection();

		return connect(httpsCon);
	}

	private HttpResponseMessage httpConnect(URL reqURL) throws Exception {
		log.debug("httpConnect(" + reqURL + ") --> start");

		// create connection
		HttpURLConnection httpCon = null;
		httpCon = (HttpURLConnection) reqURL.openConnection();
		return connect(httpCon);
	}

	private HttpResponseMessage connect(HttpURLConnection httpCon) throws Exception {
		log.info("SENDING " + this.method + ": " + this.urlString);

		httpCon.setDoOutput(true);

		if ("POST".equalsIgnoreCase(method))
			httpCon.setDoInput(true);

		httpCon.setConnectTimeout(timeout);
		httpCon.setReadTimeout(timeout);

		httpCon.setRequestMethod(method.toUpperCase());
		if (authentication != null)
			httpCon.setRequestProperty("Authorization", authentication);
		if (contentEncoding != null)
			httpCon.setRequestProperty("Content-Encoding", contentEncoding);
		if (accept != null)
			httpCon.setRequestProperty("Accept", accept);
		
		Set<String> keys = headers.keySet();
		for (String key : keys)
			httpCon.setRequestProperty(key, headers.get(key));

		if (isNotEmpty(body)) {
			OutputStream outputStream;
			outputStream = httpCon.getOutputStream();
			outputStream.write(body.getBytes());
			outputStream.close();
		}

		if (https) {
			((HttpsURLConnection) httpCon).setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
		}

		// get the response code
		Integer responseCode 				= httpCon.getResponseCode();
		String responseContentType 			= httpCon.getContentType();
		String responseContentDisposition 	= httpCon.getHeaderField("Content-Disposition");
		String responseString 				= "";
		
		log.debug("HTTP code: "+responseCode);
		
		// get the response string
		BufferedReader response = null;
//		String contentEncoding = httpCon.getContentEncoding();
		log.debug("encoding content : "+charsetEncoding);
		if (responseCode.equals(200)) {
			log.debug("200 OK");
			responseString = "";
			String lineSeparator = "";
			if (this.preserveLine)
				lineSeparator = "\n";
			response = new BufferedReader(new InputStreamReader(httpCon.getInputStream(), charsetEncoding));
			String inputLine;
			while ((inputLine = response.readLine()) != null)
				responseString = responseString + inputLine + lineSeparator;
			response.close();

		} else {
			InputStream errorIs = httpCon.getErrorStream();
			if (errorIs == null)
				errorIs = httpCon.getInputStream();
			response = new BufferedReader(new InputStreamReader(errorIs, charsetEncoding));

			String inputLine;
			while ((inputLine = response.readLine()) != null)
				responseString = responseString + inputLine;

			response.close();
		}
		log.info("RESPONSE RECEIVED");
		
		return new HttpResponseMessage(	responseCode, 
										responseString, 
										responseContentType, 
										responseContentDisposition, 
										httpCon.getHeaderFields()  );
	}
	
	public HttpResponseMessage send(){
		try {
			return getResponse();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
