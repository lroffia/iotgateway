package arces.unibo.gateway.adapters.protocol;

public class COAPAdapter extends MPAdapter{

	@Override
	public String protocolURI() {
		return "iot:COAP";
	}

	@Override
	public void mpResponse(String request, String value) {
		// TODO Manage the response "value" related to the request URI "request"

	}
	
	//TODO Call the method "String mpRequest(String value)" to send a request. The method returns the URI of the request

}
