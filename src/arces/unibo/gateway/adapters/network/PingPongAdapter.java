package arces.unibo.gateway.adapters.network;

import java.io.IOException;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public class PingPongAdapter extends MNAdapter {
	
	public static void main(String[] args) throws IOException {
		PingPongAdapter adapter;
		
		if (args.length < 4) {
			adapter = new PingPongAdapter();
		}
		else {
			adapter =new PingPongAdapter(args[1],Integer.parseInt(args[2]),args[3]);
		}
		
		if(adapter.start()) {
			if (args.length == 4) 
				Logging.log(VERBOSITY.INFO,adapter.adapterName(),"Running @ "+args[1]+":"+args[2]+" Namespace: "+args[3]);
			else 
				Logging.log(VERBOSITY.INFO,adapter.adapterName(),"PingPong Adapter running on LOCAL gateway");
			Logging.log(VERBOSITY.INFO,adapter.adapterName(),"Press any key to exit...");
			System.in.read();
			if (adapter.stop()) Logging.log(VERBOSITY.INFO,adapter.adapterName(),"PingPong Adapter is stopped");
		}
		else {
			Logging.log(VERBOSITY.FATAL,adapter.adapterName(),"FAILED to start @ "+args[1]+":"+args[2]+" Namespace: "+args[3]);
			Logging.log(VERBOSITY.FATAL,adapter.adapterName(),"Press any key to exit...");
			System.in.read();
		}
			
	}
	
	public PingPongAdapter(String SIB_IP,int SIB_PORT,String SIB_NAME){
		super(SIB_IP, SIB_PORT,SIB_NAME);
	}
	
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
			Logging.log(VERBOSITY.INFO,adapterName(),"PING-->PONG");
			mnResponse("PONG");
		}
		else {
			Logging.log(VERBOSITY.INFO,adapterName(),"PONG-->PING");
			mnResponse("PING");
		}	
	}

	@Override
	protected boolean doStart() {
		Logging.log(VERBOSITY.INFO,adapterName(),"Started");
		return true;
	}

	@Override
	protected void doStop() {
	}

	@Override
	public String adapterName() {
		return "PINGPONG ADAPTER";
	}
}
