package arces.unibo.gateway.adapters.network;

public class PingPongAdapter extends MNAdapter {

	@Override
	public String networkURI() {
		return "iot:PINGPONG";
	}

	@Override
	public void mnRequest(String request) {
		if (request.equals("PING")) {
			System.out.println("PINGPONG ADAPTER: PING-->PONG");
			mnResponse("PONG");
		}
		else {
			System.out.println("PINGPONG ADAPTER: PONG-->PING");
			mnResponse("PING");
		}
		
	}

	@Override
	protected boolean doStart() {
		System.out.println("********************");
		System.out.println("* PINGPONG Adapter *");
		System.out.println("********************");
		return true;
	}

	@Override
	protected void doStop() {
		// TODO Auto-generated method stub
		
	}
}
