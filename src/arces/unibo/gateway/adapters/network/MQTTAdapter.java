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

	@Override
	public boolean start() {
		System.out.println("********************");
		System.out.println("* PINGPONG Adapter *");
		System.out.println("********************");
		
		return super.start();
	}
	
	@Override
	public void stop(){
		super.stop();
	}
	
	//TODO Send the response by calling "boolean mnResponse(String value)";
}
