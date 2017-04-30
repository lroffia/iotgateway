package arces.unibo.gateway;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.gateway.adapters.protocol.HTTPAdapter;
import arces.unibo.gateway.adapters.protocol.MPAdapter;
import arces.unibo.gateway.adapters.protocol.WebSocketAdapter;
import arces.unibo.gateway.dispatching.MNDispatcher;
import arces.unibo.gateway.dispatching.MPDispatcher;
import arces.unibo.gateway.garbagecollector.GarbageCollector;
import arces.unibo.gateway.resources.ResourceManager;

class SemanticGateway {
	private static final Logger logger = LogManager.getLogger("SemanticGateway");
	static String APP_PROFILE = "GatewayProfile.sap";
	
	static ResourceManager resourceManager;
	static GarbageCollector gc;
	
	static MNDispatcher mnDispatcher;
	static MPDispatcher mpDispatcher;
	
	static ArrayList<MPAdapter> protocols;
 	
	static ApplicationProfile appProfile = new ApplicationProfile();
	
	public static void main(String[] args) throws IOException, InterruptedException {				
		if(!appProfile.load(APP_PROFILE)) {
			logger.fatal("Failed to load: "+ APP_PROFILE);
			return;
		}
		else logger.info("Loaded application profile "+ APP_PROFILE);
	
		//Garbage collector
		gc = new GarbageCollector(appProfile,null);
		if (!gc.start(false,false)) {
			logger.fatal("Garbage collector FAILED to start!");return;}
		
		//Resource manager
		resourceManager = new ResourceManager(appProfile);
		if(!resourceManager.start()) {
			logger.fatal("Resource manager FAILED to start!");return;}
		
		//Messages dispatchers
		mnDispatcher = new MNDispatcher(appProfile);
		if(!mnDispatcher.start()) {
			logger.fatal("MN Dispatcher FAILED to start!");return;}
		
		mpDispatcher = new MPDispatcher(appProfile);
		if(!mpDispatcher.start()) {
			logger.fatal("MP Dispatcher FAILED to start!");return;}
		
		//Protocol adapters
		protocols= new ArrayList<MPAdapter>();
		
		//TODO: add all supported protocols here
		protocols.add(new HTTPAdapter(appProfile));
		protocols.add(new WebSocketAdapter(appProfile));		
		
		for (MPAdapter adapter : protocols) if(!adapter.start()) {
			logger.info(adapter.adapterName()+ " FAILED to start!");return;}
		
		//Gateway running...
		logger.info("Gateway up and running! Press any key to exit...");
		System.in.read();
		
		for (MPAdapter adapter : protocols) {if(adapter.stop()) logger.info(adapter.adapterName() + " stopped");}
		
		if(mnDispatcher.stop()) logger.info("MN-Dispatcher stopped");
		if(mpDispatcher.stop()) logger.info("MP-Dispatcher stopped");
		
		if(resourceManager.stop()) logger.info("Resource Manager stopped");		
		if(gc.stop()) logger.info("Garbage Collector stopped");

		System.exit(0);
	}
}
