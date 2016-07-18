package arces.unibo.gateway;

import java.io.IOException;
import java.util.ArrayList;

import arces.unibo.SEPA.SPARQLApplicationProfile;

import arces.unibo.gateway.adapters.network.MNAdapter;
import arces.unibo.gateway.adapters.protocol.HTTPAdapter;
import arces.unibo.gateway.adapters.protocol.MPAdapter;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

class SemanticGateway {
	static String tag ="GATEWAY";
	
	static ResourceManager resourceManager;
	static GarbageCollector gc;
	
	static MNDispatcher mnDispatcher;
	static MPDispatcher mpDispatcher;
	
	static ArrayList<MPAdapter> protocols;
	static ArrayList<MNAdapter> networks;
 	
	public static void main(String[] args) throws IOException, InterruptedException {	
		Logging.setVerbosityLevel(VERBOSITY.DEBUG);
		
		String path = SemanticGateway.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"GatewayProfile.xml";
		
		if(!SPARQLApplicationProfile.load(path)) {
			Logging.log(VERBOSITY.FATAL, "Gateway", "Failed to load: "+ path);
			return;
		}
	
		//Components
		gc = new GarbageCollector();
		resourceManager = new ResourceManager();
		
		mnDispatcher = new MNDispatcher();
		mpDispatcher = new MPDispatcher();
		
		protocols= new ArrayList<MPAdapter>();
		networks = new ArrayList<MNAdapter>();
		
		//TODO: add all supported protocols here
		protocols.add(new HTTPAdapter());
		//protocols.add(new COAPAdapter());
		//protocols.add(new WebSocketAdapter());
		
		//TODO: add all supported protocols here
		//networks.add(new PingPongAdapter());
		//networks.add(new MQTTAdapter());
		
		if (!gc.start(false,false)) {Logging.log(VERBOSITY.FATAL,tag,"Garbage collector FAILED to start!");return;}
		if(!resourceManager.start()) {Logging.log(VERBOSITY.FATAL,tag,"Resource manager FAILED to start!");return;}
		if(!mpDispatcher.start()) {Logging.log(VERBOSITY.FATAL,tag,"MP Dispatcher FAILED to start!");return;}
		if(!mnDispatcher.start()) {Logging.log(VERBOSITY.FATAL,tag,"MN Dispatcher FAILED to start!");return;}
			
		for (MPAdapter adapter : protocols) if(!adapter.start()) {Logging.log(VERBOSITY.FATAL,tag,adapter.adapterName()+ " FAILED to start!");return;}
		//for (MNAdapter adapter: networks) if(!adapter.start()) {Logging.log(VERBOSITY.FATAL,tag,adapter.adapterName()+ " FAILED to start!");return;}
		
		//Gateway running...
		Logging.log(VERBOSITY.INFO,tag,"Gateway up and running! Press any key to exit...");
		System.in.read();
		
		for (MPAdapter adapter : protocols) {if(adapter.stop()) Logging.log(VERBOSITY.INFO,tag,adapter.adapterName() + " stopped");}
		//for (MNAdapter adapter : networks) {if(adapter.stop()) Logging.log(VERBOSITY.INFO,tag,adapter.adapterName() + " stopped");}
		
		if(mnDispatcher.stop()) Logging.log(VERBOSITY.INFO,tag,"MN-Dispatcher stopped");
		if(mpDispatcher.stop()) Logging.log(VERBOSITY.INFO,tag,"MP-Dispatcher stopped");
		
		if(resourceManager.stop()) Logging.log(VERBOSITY.INFO,tag,"Resource Manager stopped");		
		if(gc.stop()) Logging.log(VERBOSITY.INFO,tag,"Garbage Collector stopped");

		System.exit(0);
	}
}
