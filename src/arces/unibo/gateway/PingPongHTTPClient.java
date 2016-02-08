package arces.unibo.gateway;

import java.io.IOException;
//import java.util.concurrent.Semaphore;

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
	private long startTime = 0;
	//private static Semaphore mutex = new Semaphore(1,true);
	
	public static void main(String[] args) throws IOException, InterruptedException {
		//Simulator
		PingPongHTTPClient client = new PingPongHTTPClient();
		String ret = "PING";
		while(true) {
			ret = client.doRequest(ret);
			if (ret.equals("ERROR") || ret.equals("TIMEOUT")) ret = "PING";
			Thread.sleep(1000);
		}	
	}
	
	public String doRequest(String ping) throws InterruptedException, ClientProtocolException, IOException {
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
        
        String responseBody ="ERROR";
        
        startTime = java.time.Instant.now().toEpochMilli();
    	
    	//mutex.acquire();
    	
		CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet( "http://" + server + ":8000/iot?action="+ping);
        System.out.println("HTTP CLIENT REQUEST: "+httpget.toString());
        responseBody = httpclient.execute(httpget, responseHandler);
		httpclient.close();

		//mutex.release();
		
        System.out.println("HTTP CLIENT RESPONSE ("+ (java.time.Instant.now().toEpochMilli() - startTime) +" ms): " + responseBody);
        
        return responseBody;
	}
}

