package arces.unibo.gateway;

import java.io.IOException;
import java.util.ArrayList;

import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.gateway.adapters.protocol.HTTPAdapter;
import arces.unibo.gateway.adapters.protocol.MPAdapter;
import arces.unibo.gateway.adapters.protocol.WebSocketAdapter;
import arces.unibo.gateway.dispatching.MNDispatcher;
import arces.unibo.gateway.dispatching.MPDispatcher;
import arces.unibo.gateway.garbagecollector.GarbageCollector;
import arces.unibo.gateway.resources.ResourceManager;

class SemanticGateway {
	static String tag ="GATEWAY";
	static String APP_PROFILE = "GatewayProfile.sap";
	
	static ResourceManager resourceManager;
	static GarbageCollector gc;
	
	static MNDispatcher mnDispatcher;
	static MPDispatcher mpDispatcher;
	
	static ArrayList<MPAdapter> protocols;
 	
	static ApplicationProfile appProfile = new ApplicationProfile();
	
	public static void main(String[] args) throws IOException, InterruptedException {			
		Logger.loadSettings();
		
		if(!appProfile.load(APP_PROFILE)) {
			Logger.log(VERBOSITY.FATAL, "Gateway", "Failed to load: "+ APP_PROFILE);
			return;
		}
		else Logger.log(VERBOSITY.INFO, "Gateway", "Loaded application profile "+ APP_PROFILE);
	
		//Garbage collector
		gc = new GarbageCollector(appProfile,null);
		if (!gc.start(false,false)) {Logger.log(VERBOSITY.FATAL,tag,"Garbage collector FAILED to start!");return;}
		
		//Resource manager
		resourceManager = new ResourceManager(appProfile);
		if(!resourceManager.start()) {Logger.log(VERBOSITY.FATAL,tag,"Resource manager FAILED to start!");return;}
		
		//Messages dispatchers
		mnDispatcher = new MNDispatcher(appProfile);
		if(!mnDispatcher.start()) {Logger.log(VERBOSITY.FATAL,tag,"MN Dispatcher FAILED to start!");return;}
		
		mpDispatcher = new MPDispatcher(appProfile);
		if(!mpDispatcher.start()) {Logger.log(VERBOSITY.FATAL,tag,"MP Dispatcher FAILED to start!");return;}
		
		//Protocol adapters
		protocols= new ArrayList<MPAdapter>();
		
		//TODO: add all supported protocols here
		protocols.add(new HTTPAdapter(appProfile));
		protocols.add(new WebSocketAdapter(appProfile));		
		
		for (MPAdapter adapter : protocols) if(!adapter.start()) {Logger.log(VERBOSITY.FATAL,tag,adapter.adapterName()+ " FAILED to start!");return;}
		
		//Gateway running...
		Logger.log(VERBOSITY.INFO,tag,"Gateway up and running! Press any key to exit...");
		System.in.read();
		
		for (MPAdapter adapter : protocols) {if(adapter.stop()) Logger.log(VERBOSITY.INFO,tag,adapter.adapterName() + " stopped");}
		
		if(mnDispatcher.stop()) Logger.log(VERBOSITY.INFO,tag,"MN-Dispatcher stopped");
		if(mpDispatcher.stop()) Logger.log(VERBOSITY.INFO,tag,"MP-Dispatcher stopped");
		
		if(resourceManager.stop()) Logger.log(VERBOSITY.INFO,tag,"Resource Manager stopped");		
		if(gc.stop()) Logger.log(VERBOSITY.INFO,tag,"Garbage Collector stopped");

		System.exit(0);
	}
}
