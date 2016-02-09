package arces.unibo.gateway.adapters.network;

public class MQTTAdapter extends MNAdapter {

	@Override
	protected String networkURI() {
		return "iot:MQTT";
	}

	@Override
	protected void mnRequest(String request) {
		// TODO Auto-generated method stub
		// TODO Send the response by calling "boolean mnResponse(String value)";
	}

	@Override
	protected boolean doStart() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void doStop() {
		// TODO Auto-generated method stub
		
	}
}
