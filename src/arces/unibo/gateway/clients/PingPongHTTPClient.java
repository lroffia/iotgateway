package arces.unibo.gateway.clients;

import java.io.IOException;
//import java.util.concurrent.Semaphore;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.Logger.VERBOSITY;

public class PingPongHTTPClient {
	private static String server = "127.0.0.1";
	private static String tag = "HTTP PingPong Client";
	
	private static ResponseHandler<String> responseHandler;
	private static CloseableHttpClient httpclient = HttpClients.createDefault();

	private static long startTime = 0;
	private static long pingTime = 0;
	private static long nRequest = 0;
	private static long pingMax = 0;
	private static long pingMin = 0;
	private static long pingAvg = 0;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Logger.registerTag("HTTP PingPong Client");
		Logger.enableConsoleLog();
				
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
	            	Logger.log(VERBOSITY.ERROR, tag, "Unexpected response status: " + status);
	            	return "ERROR";
	            }
	        }
        };
        int period = 0;
       
        boolean auto = true;
        int ch;
        System.out.println("Select mode: \n"
				+ "1-Continuos without sleep\n"
				+ "2-Continuos with sleep\n"
				+ "3-Under request\n>>");
        ch = System.in.read();System.in.read();
        
        if(ch == '2') {
			System.out.println("Specify period (ms): ");		
			Scanner in = new Scanner(System.in);
			period = in.nextInt();
			in.close();
			
		} else if (ch == '3') auto = false;
		
        if (auto) ch = '4';
        
        while(true) {
			if(!auto) {
				System.out.println("Select the request to send:\n"
						+ "1-Set PINGPONG resource to PING\n"
						+ "3-Set PINGPONG resource to PONG\n"
						+ "Any other key-Get PINGPONG resource\n>>");
				ch = System.in.read();
				System.in.read();
			} 
			else {
				Thread.sleep(period);
				if (ch == '1') ch = '2';
				else if (ch == '2') ch = '3';
				else if (ch == '3') ch = '4';
				else if (ch == '4') ch = '1';
			}
			
			if(ch == '1') doRequest("resource=PINGPONG&action=SET&value=PING");
			else if (ch == '3') doRequest("resource=PINGPONG&action=SET&value=PONG");
			else doRequest("resource=PINGPONG&action=GET");
		}	
	}
	
	public static String doRequest(String request) throws InterruptedException, ClientProtocolException, IOException {        
        String responseBody ="ERROR";
         	
        HttpGet httpget = new HttpGet( "http://" + server + ":8000/iot?"+request);
        Logger.log(VERBOSITY.INFO, tag, httpget.toString());
        
        startTime = System.currentTimeMillis();
        
        try {
        responseBody = httpclient.execute(httpget, responseHandler);
        }
        catch(java.net.ConnectException e) {
        	 Logger.log(VERBOSITY.ERROR, tag, "Server " + server + " is down or cannot be reached");	
        }
        pingTime = System.currentTimeMillis() - startTime;
        
        if (nRequest == 0) {
        	pingMin = pingTime;
        	pingMax = pingTime;
        }
        else if (pingTime < pingMin) pingMin = pingTime;
        else if (pingTime > pingMax) pingMax = pingTime;
        nRequest++;
        pingAvg += pingTime;
        
        String message = String.format("Current: %d Min: %d Avg: %d Max: %d Response:%s", pingTime,pingMin,pingAvg/nRequest,pingMax,responseBody);
        Logger.log(VERBOSITY.INFO, tag,message);
        
        return responseBody;
	}
}

