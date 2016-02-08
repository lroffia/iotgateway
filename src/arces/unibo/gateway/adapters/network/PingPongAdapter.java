package arces.unibo.gateway.adapters.network;

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
		if (request.equals("PING")) {
			System.out.println("PINGPONG ADAPTER: PING-->PONG");
			super.mnResponse("PONG");
		}
		else {
			System.out.println("PINGPONG ADAPTER: PONG-->PING");
			super.mnResponse("PING");
		}
	}
	
	@Override
	public boolean start() {
		System.out.println("********************");
		System.out.println("* PINGPONG Adapter *");
		System.out.println("********************");
		
		return super.start();
	}
}
