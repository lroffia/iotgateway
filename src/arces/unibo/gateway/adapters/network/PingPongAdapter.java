package arces.unibo.gateway.adapters.network;

public class PingPongAdapter extends MNAdapter {

	@Override
	public String networkURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mnRequest(String request) {
		// TODO Auto-generated method stub
		
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
	
	/*
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
	

	public boolean start() {
		System.out.println("********************");
		System.out.println("* PINGPONG Adapter *");
		System.out.println("********************");
		
		return super.start();
	}
	
	public void stop(){
		super.stop();
	}
	*/
}
