package arces.unibo.iot.network.adapters;

public class PingPongAdapter extends MNAdapter {
	
	public PingPongAdapter(){
		super();
	}

	@Override
	public String networkURI() {
		return "iot:PINGPONG";
	}

	@Override
	public void mnRequest(String request) {
		if (request.equals("PING")) super.mnResponse("PONG");
		else super.mnResponse("PING");
	}
	
	@Override
	public boolean start() {
		System.out.println("********************");
		System.out.println("* PINGPONG Adapter *");
		System.out.println("********************");
		
		return super.start();
	}
}
