package arces.unibo.gateway.adapters.network;

import java.io.IOException;

import arces.unibo.SEPA.SPARQLApplicationProfile;
import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public class PingPongAdapter extends MNAdapter {
	
	public static void main(String[] args) throws IOException {
		byte[] line = new byte[80];
		byte[] chars;
		int nBytes = 0;
		String IP = "127.0.0.1";
		int PORT = 10123;
		String namespace = "IoTGateway";
		
		String path = PingPongAdapter.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"GatewayProfile.xml";
		
		if(!SPARQLApplicationProfile.load(path)) {
			Logging.log(VERBOSITY.FATAL, "ADAPTER SETTINGS", "Failed to load: "+ path);
			return;
		}
		
		Logging.log(VERBOSITY.INFO,"ADAPTER SETTINGS","Gateway IP (press return for default, "+IP+" )");		
		nBytes = System.in.read(line);

		if (nBytes > 1) {
			chars = new byte[nBytes-1];
			for(int i=0 ; i < nBytes-1 ; i++) chars[i] = line[i];
			IP = new String(chars);
		}
		
		Logging.log(VERBOSITY.INFO,"ADAPTER SETTINGS","Gateway PORT (press return for default, " + PORT+ " )");
		nBytes = System.in.read(line);
		
		if (nBytes > 1) {
			chars = new byte[nBytes-1];
			for(int i=0 ; i < nBytes-1 ; i++) chars[i] = line[i];
			PORT = Integer.parseInt(new String(chars));
		}
		
		Logging.log(VERBOSITY.INFO,"ADAPTER SETTINGS","Gateway Namespace (press return for default, " +namespace +" )");
		nBytes = System.in.read(line);
		
		if (nBytes > 1) {
			chars = new byte[nBytes-1];
			for(int i=0 ; i < nBytes-1 ; i++) chars[i] = line[i];
			namespace = new String(chars);
		}
		
		PingPongAdapter adapter;
		adapter =new PingPongAdapter(IP,PORT,namespace);
		
		if(adapter.start()) {
			Logging.log(VERBOSITY.INFO,adapter.adapterName(), adapter.adapterName() + " is connected to gateway "+IP+":"+PORT+"@"+namespace);
			Logging.log(VERBOSITY.INFO,adapter.adapterName(),"Press any key to exit...");
			System.in.read();
			if(adapter.stop()) Logging.log(VERBOSITY.INFO,adapter.adapterName(),adapter.adapterName() +" stopped");
		}
		else {
			Logging.log(VERBOSITY.FATAL,adapter.adapterName(),adapter.adapterName() +" is NOT running");
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
