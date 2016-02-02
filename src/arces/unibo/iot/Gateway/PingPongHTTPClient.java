package arces.unibo.iot.Gateway;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class PingPongHTTPClient {
	private String server = "127.0.0.1";
	
	public String doRequest(String ping) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
        HttpGet httpget = new HttpGet( "http://" + server + ":8000/iot?action="+ping);

        System.out.println(java.time.Instant.now().toEpochMilli() + " > Executing request " + httpget.getRequestLine());

        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
	        @Override
	        public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
	            int status = response.getStatusLine().getStatusCode();
	            if (status >= 200 && status < 300) 
	            {
	                HttpEntity entity = response.getEntity();
	                return entity != null ? EntityUtils.toString(entity) : "ERROR";
	            } 
	            else 
	            {
	            	System.out.println("  ***Unexpected response status: " + status + " ***");
	            	return "ERROR";
	            }
	        }
        };
        
        String responseBody ="";
        
        try 
        {
			responseBody = httpclient.execute(httpget, responseHandler);
		} 
        catch (ClientProtocolException e) {
        	System.out.println("  ***ClientProtocolException***");
			return "ERROR";
		} 
        catch (IOException e) {
        	System.out.println("  ***IOException (HTTP Server down?)***");
			return "ERROR";
		}
        
        try 
        {
			httpclient.close();
		} 
        catch (IOException e) {
        	return "ERROR";
		}
        
        System.out.println(java.time.Instant.now().toEpochMilli() + " < Response " + responseBody);
        
        return responseBody;
	}
}

