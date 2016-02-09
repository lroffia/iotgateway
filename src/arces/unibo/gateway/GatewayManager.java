package arces.unibo.gateway;

import java.io.IOException;
import java.util.ArrayList;

import arces.unibo.gateway.adapters.network.MNAdapter;
import arces.unibo.gateway.adapters.network.PingPongAdapter;
import arces.unibo.gateway.adapters.protocol.HTTPAdapter;
import arces.unibo.gateway.adapters.protocol.MPAdapter;

public class GatewayManager {
	
	static MappingManager mappingManager;
	static MNDispatcher mnDispatcher;
	static MPDispatcher mpDispatcher;
	static ArrayList<MNAdapter> networks;
	static ArrayList<MPAdapter> protocols;
	static GarbageCollector gc;
	
	private static boolean addDefaultProtocolMapping() {
		
		//Protocols default mappings
		System.out.println("Adding default protocol mappings");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=TEMPERATURE&location=ROOM1", "<*>", "iot:Context_1", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=TEMPERATURE&location=ROOM2", "<*>", "iot:Context_2", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=TEMPERATURE&location=ROOM3", "<*>", "iot:Context_3", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=TEMPERATURE&location=ROOM4", "<*>", "iot:Context_4", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=VALVE&location=ROOM1", "<*>", "iot:Context_5", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=SET&type=VALVE&location=ROOM1&value=<*>", "<*>", "iot:Context_5", "iot:SET", "<*>");
		if(!mappingManager.addProtocolMapping("iot:HTTP", "action=PING", "*", "iot:Context_PING", "iot:GET", "*")) return false;
		if(!mappingManager.addProtocolMapping("iot:HTTP", "action=PONG", "*", "iot:Context_PONG", "iot:GET", "*")) return false;
		return true;
		
	}
	private static boolean addDefaultNetworkMapping() {
				
		//Networks default mappings
		System.out.println("Adding default network mappings");
		//this.addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO1", "TEMPERATURE&NODO1&<*>", "iot:Context_1", "iot:GET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO2", "TEMPERATURE&NODO2&<*>", "iot:Context_2", "iot:GET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO3", "TEMPERATURE&NODO3&<*>", "iot:Context_3", "iot:GET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO4", "TEMPERATURE&NODO4&<*>", "iot:Context_4", "iot:GET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "VALVE@NODO1&<*>", "VALVE&NODO1&<*>", "iot:Context_5", "iot:SET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "STATUS@NODO1", "STATUS&NODO1&<*>", "iot:Context_5", "iot:GET", "<*>");
		
		if(!mappingManager.addNetworkMapping("iot:PINGPONG", "PING", "PONG", "iot:Context_PING", "iot:GET", "*")) return false;
		if(!mappingManager.addNetworkMapping("iot:PINGPONG", "PONG", "PING", "iot:Context_PONG", "iot:GET", "*")) return false;
		return true;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		//Garbage collector
		gc = new GarbageCollector();
		if (!gc.start(true)) return;
		
		//Mapping manager
		mappingManager = new MappingManager();
		if(!mappingManager.start()) return;
		mappingManager.removeAllMapping();
		addDefaultNetworkMapping();
		addDefaultProtocolMapping();
		
		//Dispatchers
		mnDispatcher = new MNDispatcher();
		mpDispatcher = new MPDispatcher();
		if(!mpDispatcher.start()) return;
		if(!mnDispatcher.start()) return;
		
		//Network adapters
		networks = new ArrayList<MNAdapter>();
		//TODO: add all supported networks here
		networks.add(new PingPongAdapter());		
		for (MNAdapter adapter : networks)  adapter.start();
		
		//Protocol adapters
		protocols= new ArrayList<MPAdapter>();
		//TODO: add all supported protocols here
		protocols.add(new HTTPAdapter());
		for (MPAdapter adapter : protocols) adapter.start();

		System.in.read();
		
		for (MPAdapter adapter : protocols) adapter.stop();
		for (MNAdapter adapter : networks) adapter.stop();
		mnDispatcher.stop();
		mpDispatcher.stop();
		mappingManager.stop();
		gc.stop();
	}
}
