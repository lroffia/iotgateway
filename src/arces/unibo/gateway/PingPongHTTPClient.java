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
	private static String server = "127.0.0.1";

	private static ResponseHandler<String> responseHandler;
	private static CloseableHttpClient httpclient = HttpClients.createDefault();

	private static long startTime = 0;
	private static long pingTime = 0;
	private static long nRequest = 0;
	private static long pingMax = 0;
	private static long pingMin = 0;
	private static long pingAvg = 0;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String ret = "PING";
		responseHandler = new ResponseHandler<String>() {
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
		while(true) {
			ret = doRequest(ret);
			if (ret.equals("ERROR") || ret.equals("TIMEOUT")) ret = "PING";
			//Thread.sleep(1000);
		}	
	}
	
	public static String doRequest(String ping) throws InterruptedException, ClientProtocolException, IOException {        
        String responseBody ="ERROR";
         	
        HttpGet httpget = new HttpGet( "http://" + server + ":8000/iot?action="+ping);
        System.out.println("HTTP CLIENT REQUEST: "+httpget.toString());
        
        startTime = java.time.Instant.now().toEpochMilli();
        
        responseBody = httpclient.execute(httpget, responseHandler);
		
        pingTime = java.time.Instant.now().toEpochMilli() - startTime;
        
        if (nRequest == 0) {
        	pingMin = pingTime;
        	pingMax = pingTime;
        }
        else if (pingTime < pingMin) pingMin = pingTime;
        else if (pingTime > pingMax) pingMax = pingTime;
        nRequest++;
        pingAvg += pingTime;
        
        String message = String.format("Min: %d Avg: %d Max: %d", pingMin,pingAvg/nRequest,pingMax);
        System.out.println(message);
        
        return responseBody;
	}
}

