package arces.unibo.iot.Gateway;

import arces.unibo.iot.mapping.MNMappingManager;
import arces.unibo.iot.mapping.MPMappingManager;

public class MappingManager {
		
	private MPMappingManager mpMappingManager;
	private MNMappingManager mnMappingManager;
	
	public MappingManager() {
		mpMappingManager = new MPMappingManager();
		mnMappingManager = new MNMappingManager();	
	}
	
	public boolean start(){
		System.out.println("*******************");
		System.out.println("* Mapping Manager *");
		System.out.println("*******************");
		
		//MN Mapping
		//if(!mnMappingManager.join()) return false;
		//addDefaultNetworkMapping();
		
		//MP Mapping
		if(!mpMappingManager.start()) return false;
		if(!mpMappingManager.removeAllMapping()) return false;
		if(!addDefaultProtocolMapping()) return false;
			
		System.out.println("Mapping Manager started");
		return true;
	}
	
	public boolean addProtocolMapping(String protocol,String requestStringPattern,String responseStringPattern,String context,String action,String value){
		return mpMappingManager.addMapping(protocol, requestStringPattern, responseStringPattern, context, action, value);
	}

	public boolean addNetworkMapping(String network,String requestStringPattern,String responseStringPattern,String context,String action,String value){
		return mnMappingManager.addMapping(network, requestStringPattern, responseStringPattern, context, action, value);
	}
	
	private boolean addDefaultProtocolMapping() {
		
		//Protocols default mappings
		System.out.println("Adding default protocol mappings");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=TEMPERATURE&location=ROOM1", "<*>", "iot:Context_1", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=TEMPERATURE&location=ROOM2", "<*>", "iot:Context_2", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=TEMPERATURE&location=ROOM3", "<*>", "iot:Context_3", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=TEMPERATURE&location=ROOM4", "<*>", "iot:Context_4", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=GET&type=VALVE&location=ROOM1", "<*>", "iot:Context_5", "iot:GET", "<*>");
		//this.addProtocolMapping("iot:HTTP", "action=SET&type=VALVE&location=ROOM1&value=<*>", "<*>", "iot:Context_5", "iot:SET", "<*>");
		if(!addProtocolMapping("iot:HTTP", "action=PING", "<*>", "iot:Context_PING", "iot:GET", "<*>")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=PONG", "<*>", "iot:Context_PONG", "iot:GET", "<*>")) return false;
		return true;
		
	}
	private void addDefaultNetworkMapping() {
				
		//Networks default mappings
		System.out.println("Adding default network mappings");
		//this.addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO1", "TEMPERATURE&NODO1&<*>", "iot:Context_1", "iot:GET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO2", "TEMPERATURE&NODO2&<*>", "iot:Context_2", "iot:GET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO3", "TEMPERATURE&NODO3&<*>", "iot:Context_3", "iot:GET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO4", "TEMPERATURE&NODO4&<*>", "iot:Context_4", "iot:GET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "VALVE@NODO1&<*>", "VALVE&NODO1&<*>", "iot:Context_5", "iot:SET", "<*>");
		//this.addNetworkMapping("iot:DASH7", "STATUS@NODO1", "STATUS&NODO1&<*>", "iot:Context_5", "iot:GET", "<*>");
		
		this.addNetworkMapping("iot:PINGPONG", "PING", "PONG", "iot:Context_PING", "iot:GET", "<*>");
		this.addNetworkMapping("iot:PINGPONG", "PONG", "PING", "iot:Context_PONG", "iot:GET", "<*>");
	}
}
