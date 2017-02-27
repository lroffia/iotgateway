package arces.unibo.gateway.clients;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;

public class PingPongCOAPClient {
	
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
				SEPALogger.log(VERBOSITY.INFO, "PINGPONG", pingPong +" --> " + responseString);
				if (!responseString.equals("TIMEOUT")) pingPong = response.getResponseText();
			} else {
				SEPALogger.log(VERBOSITY.INFO, "PINGPONG","No response received");
			}
		}
	}

}
