package arces.unibo.gateway;

import java.io.IOException;
import java.util.ArrayList;

import arces.unibo.SEPA.SPARQL;
import arces.unibo.gateway.adapters.network.MNAdapter;
import arces.unibo.gateway.adapters.network.MQTTAdapter;
import arces.unibo.gateway.adapters.network.PingPongAdapter;
import arces.unibo.gateway.adapters.protocol.COAPAdapter;
import arces.unibo.gateway.adapters.protocol.HTTPAdapter;
import arces.unibo.gateway.adapters.protocol.MPAdapter;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

class SemanticGateway {
	static String tag ="GATEWAY";
	
	static ResourceManager resourceManager;
	//static GarbageCollector gc;
	
	static MNDispatcher mnDispatcher;
	static MPDispatcher mpDispatcher;
	
	static ArrayList<MPAdapter> protocols;
	static ArrayList<MNAdapter> networks;
 	
	public static void main(String[] args) throws IOException, InterruptedException {	
		Logging.setVerbosityLevel(VERBOSITY.DEBUG);
		
		SPARQL.loadApplicationProfile(SemanticGateway.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"GatewayProfile.xml");
		
		//Garbage collector
		//gc = new GarbageCollector();
		/*if (!gc.start(false,false)) {
			Logging.log(VERBOSITY.FATAL,tag,"Garbage collector FAILED to start!");
			return;
		}*/
		
		//Resource Manager
		resourceManager = new ResourceManager();
		if(!resourceManager.start()) {
			Logging.log(VERBOSITY.FATAL,tag,"Resource manager FAILED to start!");
			return;	
		}
		
		//Dispatchers
		mnDispatcher = new MNDispatcher();
		mpDispatcher = new MPDispatcher();
		if(!mpDispatcher.start()) {
			Logging.log(VERBOSITY.FATAL,tag,"MP Dispatcher FAILED to start!");
			return;
		}
		if(!mnDispatcher.start()) {
			Logging.log(VERBOSITY.FATAL,tag,"MN Dispatcher FAILED to start!");
			return;
		}
			
		//LOCAL Protocol adapters
		protocols= new ArrayList<MPAdapter>();
		
		//TODO: add all supported protocols here
		protocols.add(new HTTPAdapter());
		protocols.add(new COAPAdapter());
		
		for (MPAdapter adapter : protocols) 
			if(!adapter.start()) {
				Logging.log(VERBOSITY.FATAL,tag,adapter.adapterName()+ " FAILED to start!");
				return;
			}
		
		//LOCAL Network adapters
		networks = new ArrayList<MNAdapter>();
		
		//TODO: add all supported protocols here
		networks.add(new PingPongAdapter());
		networks.add(new MQTTAdapter());
		
		for (MNAdapter adapter: networks) 
			if(!adapter.start()) {
				Logging.log(VERBOSITY.FATAL,tag,adapter.adapterName()+ " FAILED to start!");
				return;
			}
		
		Logging.log(VERBOSITY.INFO,tag,"Gateway up and running! Press any key to exit...");
		System.in.read();
		
		for (MPAdapter adapter : protocols) {
			if(adapter.stop()) Logging.log(VERBOSITY.INFO,tag,adapter.adapterName() + "\tstopped");
		}
		for (MNAdapter adapter : networks) {
			if(adapter.stop()) Logging.log(VERBOSITY.INFO,tag,adapter.adapterName() + "\tstopped");
		}
		
		if(mnDispatcher.stop()) Logging.log(VERBOSITY.INFO,tag,"MN-Dispatcher   \tstopped");
		if(mpDispatcher.stop()) Logging.log(VERBOSITY.INFO,tag,"MP-Dispatcher   \tstopped");
		
		if(resourceManager.stop()) Logging.log(VERBOSITY.INFO,tag,"Resource Manager\tstopped");		
		
		//if(gc.stop()) Logging.log(VERBOSITY.INFO,tag,"Garbage Collector\tstopped");

		System.exit(0);
	}
}
