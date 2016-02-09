package arces.unibo.gateway.adapters.protocol;

public class COAPAdapter extends MPAdapter{

	@Override
	public String protocolURI() {
		return "iot:COAP";
	}

	@Override
	public void mpResponse(String request, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean doStart() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doStop() {
		// TODO Auto-generated method stub
		
	}

	
	//TODO Call the method "String mpRequest(String value)" to send a request. The method returns the URI of the request

}
