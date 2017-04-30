package arces.unibo.gateway.clients;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class PingPongCOAPClient {
	private static final Logger logger = LogManager.getLogger("PingPongCOAPClient");
	
	public static void main(String args[]) {

		String pingPong = "PING";
		
		URI uri;
		while(true){
			try {
				uri = new URI("coap://127.0.0.1/"+pingPong);
			} catch (URISyntaxException e1) {
				return;
			}
			
			CoapClient client = new CoapClient(uri);
			client.setTimeout(15000);
			
			CoapResponse response = client.get();
			
			if (response!=null) {
				String responseString = response.getResponseText();
				logger.info( pingPong +" --> " + responseString);
				if (!responseString.equals("TIMEOUT")) pingPong = response.getResponseText();
			} else {
				logger.info("No response received");
			}
		}
	}

}
