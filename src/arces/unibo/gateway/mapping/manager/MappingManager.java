package arces.unibo.gateway.mapping.manager;

import java.util.ArrayList;

import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;
import arces.unibo.gateway.mapping.MNMapping;
import arces.unibo.gateway.mapping.MPMapping;

public class MappingManager implements MPMappingEventListener, MNMappingEventListener {
	static String tag = "MAPPING MANAGER";
	
	private MPMappingManager mpMappingManager;
	private MNMappingManager mnMappingManager;
	private MappingEventListener listener;
	
	public boolean addDefaultMapping() {
		return addDefaultProtocolMapping() && addDefaultNetworkMapping();
	}
	public boolean removeAllMapping(){
		if(!mnMappingManager.removeAllMapping()) return false;	
		if(!mpMappingManager.removeAllMapping()) return false;
		return true;
	}

	private boolean addDefaultProtocolMapping() {
		//Protocols default mappings
		SEPALogger.log(VERBOSITY.INFO, tag,"Adding default protocol mappings");
		
		// TODO add default mapping here
		//if(!addProtocolMapping("iot:HTTP", "action=GET&resource=PINGPONG", "*", "iot:Resource_PINGPONG", "iot:GET", "*")) return false;
		//if(!addProtocolMapping("iot:HTTP", "action=SET&resource=PINGPONG&value=*", "*", "iot:Resource_PINGPONG", "iot:SET", "*")) return false;

		/*
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=ROOM1&resource=TEMPERATURE", "Room1 temperature * Celsius degree", "iot:Resource_TEMPERATURE_1", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=ROOM2&resource=TEMPERATURE", "Room2 temperature * Celsius degree", "iot:Resource_TEMPERATURE_2", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=ROOM3&resource=TEMPERATURE", "Room3 temperature * Celsius degree", "iot:Resource_TEMPERATURE_3", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=ROOM0&resource=TEMPERATURE", "Room0 temperature * Celsius degree", "iot:Resource_TEMPERATURE_0", "iot:GET", "*")) return false;
		
		if(!addProtocolMapping("iot:HTTP", "action=SET&location=ROOM0&resource=VALVE&value=*", "Room0 valve * ", "iot:Resource_VALVE_0", "iot:SET", "*")) return false;
		
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=ROOM0&resource=BATTERY", "Room0 battery level * volts", "iot:Resource_BATTERY_0", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=ROOM1&resource=BATTERY", "Room1 battery level * volts", "iot:Resource_BATTERY_1", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=ROOM2&resource=BATTERY", "Room2 battery level * volts", "iot:Resource_BATTERY_2", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=ROOM3&resource=BATTERY", "Room3 battery level * volts", "iot:Resource_BATTERY_3", "iot:GET", "*")) return false;
*/
		
		/*if(!addProtocolMapping("iot:HTTP", "action=GET&location=SERVER_MML&core=0&resource=TEMPERATURE", "MML Server Core 0 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_0", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=SERVER_MML&core=1&resource=TEMPERATURE", "MML Server Core 1 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_1", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=SERVER_MML&core=2&resource=TEMPERATURE", "MML Server Core 2 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_2", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=SERVER_MML&core=3&resource=TEMPERATURE", "MML Server Core 3 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_3", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=SERVER_MML&core=4&resource=TEMPERATURE", "MML Server Core 4 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_4", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=GET&location=SERVER_MML&core=5&resource=TEMPERATURE", "MML Server Core 5 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_5", "iot:GET", "*")) return false;		
	
		if(!addProtocolMapping("iot:COAP", "PING", "PING->*", "iot:Resource_PING", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:COAP", "PONG", "PONG->*", "iot:Resource_PONG", "iot:GET", "*")) return false;
		
		if(!addProtocolMapping("iot:COAP", "ROOM1_TEMPERATURE", "Room1 temperature * ", "iot:Resource_TEMPERATURE_1", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:COAP", "ROOM2_TEMPERATURE", "Room1 temperature * ", "iot:Resource_TEMPERATURE_2", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:COAP", "ROOM3_TEMPERATURE", "Room1 temperature * ", "iot:Resource_TEMPERATURE_3", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:COAP", "ROOM4_TEMPERATURE", "Room1 temperature * ", "iot:Resource_TEMPERATURE_4", "iot:GET", "*")) return false;
		
		if(!addProtocolMapping("iot:COAP", "MML_CORE0_TEMPERATURE", "MML Server Core 0 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_0", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:COAP", "MML_CORE1_TEMPERATURE", "MML Server Core 1 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_1", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:COAP", "MML_CORE2_TEMPERATURE", "MML Server Core 2 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_2", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:COAP", "MML_CORE3_TEMPERATURE", "MML Server Core 3 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_3", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:COAP", "MML_CORE4_TEMPERATURE", "MML Server Core 4 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_4", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:COAP", "MML_CORE5_TEMPERATURE", "MML Server Core 5 Temperature *", "iot:Resource_SERVER_MML_TEMPERATURE_5", "iot:GET", "*")) return false;		
*/
		return true;
		
	}
	private boolean addDefaultNetworkMapping() {
				
		//Networks default mappings
		SEPALogger.log(VERBOSITY.INFO, tag,"Adding default network mappings");
		
		// TODO add default mapping here
		//if(!addNetworkMapping("iot:PINGPONG", "GET", "GET&*", "iot:Resource_PINGPONG", "iot:GET", "*")) return false;
		//if(!addNetworkMapping("iot:PINGPONG", "SET=*", "SET&*", "iot:Resource_PINGPONG", "iot:SET", "*")) return false;

		if(!addNetworkMapping("MQTT", "*", "arces/servers/mars/mml/cpu/core-1/temperature&*", "iot:Resource_MARS_MML_CPU_1", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/mars/mml/cpu/core-2/temperature&*", "iot:Resource_MARS_MML_CPU_2", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/mars/mml/cpu/core-3/temperature&*", "iot:Resource_MARS_MML_CPU_3", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/mars/mml/cpu/core-4/temperature&*", "iot:Resource_MARS_MML_CPU_4", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/mars/mml/cpu/core-5/temperature&*", "iot:Resource_MARS_MML_CPU_5", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/mars/mml/cpu/core-6/temperature&*", "iot:Resource_MARS_MML_CPU_6", "iot:SET", "*")) return false;
			
		if(!addNetworkMapping("MQTT", "*", "arces/servers/mars/mml/hd/sda/temperature&*", "iot:Resource_MARS_MML_HDD_SDA", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/mars/mml/hd/sdb/temperature&*", "iot:Resource_MARS_MML_HDD_SDB", "iot:SET", "*")) return false;
	
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-1/temperature&*", "iot:Resource_STAR_AVANA_CPU_1", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-2/temperature&*", "iot:Resource_STAR_AVANA_CPU_2", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-3/temperature&*", "iot:Resource_STAR_AVANA_CPU_3", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-4/temperature&*", "iot:Resource_STAR_AVANA_CPU_4", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-5/temperature&*", "iot:Resource_STAR_AVANA_CPU_5", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-6/temperature&*", "iot:Resource_STAR_AVANA_CPU_6", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-7/temperature&*", "iot:Resource_STAR_AVANA_CPU_7", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-8/temperature&*", "iot:Resource_STAR_AVANA_CPU_8", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-9/temperature&*", "iot:Resource_STAR_AVANA_CPU_9", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-10/temperature&*", "iot:Resource_STAR_AVANA_CPU_10", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-11/temperature&*", "iot:Resource_STAR_AVANA_CPU_11", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-12/temperature&*", "iot:Resource_STAR_AVANA_CPU_12", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-13/temperature&*", "iot:Resource_STAR_AVANA_CPU_13", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-14/temperature&*", "iot:Resource_STAR_AVANA_CPU_14", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-15/temperature&*", "iot:Resource_STAR_AVANA_CPU_15", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/cpu/core-16/temperature&*", "iot:Resource_STAR_AVANA_CPU_16", "iot:SET", "*")) return false;
		
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/hd/sda/temperature&*", "iot:Resource_STAR_AVANA_HDD_SDA", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/star/avana/hd/sdb/temperature&*", "iot:Resource_STAR_AVANA_HDD_SDB", "iot:SET", "*")) return false;
		
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-1/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_1", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-2/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_2", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-3/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_3", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-4/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_4", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-5/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_5", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-6/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_6", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-7/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_7", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-8/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_8", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-9/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_9", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-10/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_10", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-11/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_11", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-12/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_12", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-13/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_13", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-14/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_14", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-15/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_15", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-16/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_16", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-17/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_17", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-18/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_18", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-19/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_19", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/cpu/core-20/temperature&*", "iot:Resource_ARES_ERCOLE_CPU_20", "iot:SET", "*")) return false;

		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/hd/sda/temperature&*", "iot:Resource_ARES_ERCOLE_SDA", "iot:SET", "*")) return false;
		if(!addNetworkMapping("MQTT", "*", "arces/servers/ares/ercole/hd/sdb/temperature&*", "iot:Resource_ARES_ERCOLE_HDD_SDB", "iot:SET", "*")) return false;
		
		if(!addNetworkMapping("MQTT", "*", "arces/servers/mars/marsamba/hd/sda/temperature&*", "iot:Resource_MARS_SAMBA_HDD_SDA", "iot:SET", "*")) return false;
/*		
		if(!addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO1", "TEMPERATURE!NODO1&*", "iot:Resource_TEMPERATURE_1", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO2", "TEMPERATURE!NODO2&*", "iot:Resource_TEMPERATURE_2", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO3", "TEMPERATURE!NODO3&*", "iot:Resource_TEMPERATURE_3", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:DASH7", "TEMPERATURE@NODO0", "TEMPERATURE!NODO0&*", "iot:Resource_TEMPERATURE_0", "iot:GET", "*")) return false;
		
		if(!addNetworkMapping("iot:DASH7", "VALVE@NODO0&*", "VALVE!NODO0&*", "iot:Resource_VALVE_0", "iot:SET", "*")) return false;

		if(!addNetworkMapping("iot:DASH7", "BATTERY@NODO1", "BATTERY!NODO1&*", "iot:Resource_BATTERY_1", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:DASH7", "BATTERY@NODO2", "BATTERY!NODO2&*", "iot:Resource_BATTERY_2", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:DASH7", "BATTERY@NODO3", "BATTERY!NODO3&*", "iot:Resource_BATTERY_3", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:DASH7", "BATTERY@NODO0", "BATTERY!NODO0&*", "iot:Resource_BATTERY_0", "iot:GET", "*")) return false;
		
			
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core0/temperature", "toffano/mml/Core0/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_0", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core1/temperature", "toffano/mml/Core1/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_1", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core2/temperature", "toffano/mml/Core2/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_2", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core3/temperature", "toffano/mml/Core3/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_3", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core4/temperature", "toffano/mml/Core4/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_4", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core5/temperature", "toffano/mml/Core5/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_5", "iot:GET", "*")) return false;
*/
		return true;
	}
	
	public MappingManager(ApplicationProfile appProfile,MappingEventListener listener) {
		mpMappingManager = new MPMappingManager(appProfile,this);
		mnMappingManager = new MNMappingManager(appProfile,this);	
		this.listener = listener;
	}
	
	public boolean start(){		
		if(!mnMappingManager.start()) return false;
		if(!mpMappingManager.start()) return false;
			
		SEPALogger.log(VERBOSITY.INFO, tag,"Started");
		
		removeAllMapping();
		addDefaultMapping();
		
		return true;
	}
	
	public boolean stop(){
		return true;
	}
	
	public boolean addProtocolMapping(String protocol,String requestStringPattern,String responseStringPattern,String resource,String action,String value){
		return mpMappingManager.addMapping(protocol, requestStringPattern, responseStringPattern, resource, action, value);
	}

	public boolean addNetworkMapping(String network,String requestStringPattern,String responseStringPattern,String resource,String action,String value){
		return mnMappingManager.addMapping(network, requestStringPattern, responseStringPattern, resource, action, value);
	}
	
	public boolean removeProtocolMapping(String mapping){
		return mpMappingManager.removeMapping(mapping);
	}
	
	public boolean removeNetworkMapping(String mapping){
		return mnMappingManager.removeMapping(mapping);
	}
	
	public boolean updateNetworkMapping(String mapping,String network,String requestStringPattern,String responseStringPattern,String resource,String action,String value) {
		//TODO Capire come mai non funziona la UPDATE
		//return mnMappingManager.updateMapping(mapping, network, requestStringPattern, responseStringPattern, resource, action, value);
		boolean ret = mnMappingManager.removeMapping(mapping);
		return ret && mnMappingManager.addMapping(network, requestStringPattern, responseStringPattern, resource, action, value);
		
	}
	
	public boolean updateProtocolMapping(String mapping,String protocol,String requestStringPattern,String responseStringPattern,String resource,String action,String value) {
		//TODO Capire come mai non funziona la UPDATE
		//return mpMappingManager.updateMapping(mapping, protocol, requestStringPattern, responseStringPattern, resource, action, value);
		boolean ret = mpMappingManager.removeMapping(mapping);
		return ret && mpMappingManager.addMapping(protocol, requestStringPattern, responseStringPattern, resource, action, value);
	}
	
	//Events
	@Override
	public void addedMNMappings(ArrayList<MNMapping> mappings) {
		if (listener != null) listener.addedMNMappings(mappings);
	}

	@Override
	public void removedMNMappings(ArrayList<MNMapping> mappings) {
		if (listener != null) listener.removedMNMappings(mappings);
	}

	@Override
	public void addedMPMappings(ArrayList<MPMapping> mappings) {
		if (listener != null) listener.addedMPMappings(mappings);
	}

	@Override
	public void removedMPMappings(ArrayList<MPMapping> mappings) {
		if (listener != null) listener.removedMPMappings(mappings);
	}
	
}
