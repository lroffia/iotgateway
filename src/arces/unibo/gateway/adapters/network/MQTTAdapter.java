package arces.unibo.gateway.adapters.network;

public class MQTTAdapter extends MNAdapter {

	@Override
	public String networkURI() {
		return "iot:MQTT";
	}

	@Override
	public void mnRequest(String request) {
		// TODO Manage the MN-Request
		
	}

	//TODO Send the response by calling "boolean mnResponse(String value)";
}
