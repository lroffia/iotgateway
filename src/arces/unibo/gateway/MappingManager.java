package arces.unibo.gateway;

import java.util.ArrayList;
import java.util.UUID;

import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.Consumer;
import arces.unibo.SEPA.Logger;
import arces.unibo.SEPA.Producer;
import arces.unibo.SEPA.Logger.VERBOSITY;
import arces.unibo.gateway.mapping.MNMapping;
import arces.unibo.gateway.mapping.MPMapping;
import arces.unibo.gateway.mapping.ResourceAction;

public class MappingManager implements MPMappingEventListener, MNMappingEventListener {
	static String tag = "MAPPING MANAGER";
	
	private MPMappingManager mpMappingManager;
	private MNMappingManager mnMappingManager;
	private MappingEventListener listener;
	
	public class MPMappingManager {
		static final String tag = "MP MAPPING";
		
		private MappingCreator creator;
		private MappingRemover remover;
		private MappingListener listener;
		private MappingUpdater updater;
		private MPMappingEventListener event = null;
		
		public void setEventListener(MPMappingEventListener e) {
			event = e;
		}
				
		private class MappingListener extends Consumer {			
			public MappingListener() {super("MP_MAPPING");}

			@Override
			public void notify(BindingsResults notify) {}

			@Override
			public void notifyAdded(ArrayList<Bindings> bindingsResults) {
				ArrayList<MPMapping> mappings = new ArrayList<MPMapping>();
				for (Bindings binding : bindingsResults) {
					mappings.add(new MPMapping(
							binding.getBindingValue("?mapping").getValue(),
							binding.getBindingValue("?protocol").getValue(),
							binding.getBindingValue("?requestPattern").getValue(),
							binding.getBindingValue("?responsePattern").getValue(),
							new ResourceAction(binding.getBindingValue("?resource").getValue(),binding.getBindingValue("?action").getValue(),binding.getBindingValue("?value").getValue())));
				}
				if(event != null) event.addedMPMappings(mappings);
			}

			@Override
			public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
				ArrayList<MPMapping> mappings = new ArrayList<MPMapping>();
				for (Bindings binding : bindingsResults) {
					mappings.add(new MPMapping(
							binding.getBindingValue("?mapping").getValue(),
							binding.getBindingValue("?protocol").getValue(),
							binding.getBindingValue("?requestPattern").getValue(),
							binding.getBindingValue("?responsePattern").getValue(),
							new ResourceAction(binding.getBindingValue("?resource").getValue(),binding.getBindingValue("?action").getValue(),binding.getBindingValue("?value").getValue())));
				}
				if(event != null) event.removedMPMappings(mappings);	
			}

			@Override
			public void notifyFirst(ArrayList<Bindings> bindingsResults) {notifyAdded(bindingsResults);}	
		}
		
		private class MappingCreator extends Producer {			
			public MappingCreator() {super("INSERT_MP_MAPPING");}
			
			public boolean addMapping(String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
				Bindings bindings = new Bindings();
				String mapping = "iot:MP-Mapping_"+UUID.randomUUID().toString();
				bindings.addBinding("?mapping", new BindingURIValue(mapping));
				bindings.addBinding("?protocol", new BindingURIValue(protocol));
				bindings.addBinding("?resource", new BindingURIValue(resource));
				bindings.addBinding("?action", new BindingURIValue(action));
				bindings.addBinding("?value", new BindingLiteralValue(value));
				bindings.addBinding("?requestPattern", new BindingLiteralValue(requestPattern));
				bindings.addBinding("?responsePattern", new BindingLiteralValue(responsePattern));
				
				return update(bindings);
			}
		}
		
		private class MappingUpdater extends Producer {			
			public MappingUpdater() {super("UPDATE_MP_MAPPING");}
			
			public boolean updateMapping(String mapping, String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
				Bindings bindings = new Bindings();
				bindings.addBinding("?mapping", new BindingURIValue(mapping));
				bindings.addBinding("?protocol", new BindingURIValue(protocol));
				bindings.addBinding("?resource", new BindingURIValue(resource));
				bindings.addBinding("?action", new BindingURIValue(action));
				bindings.addBinding("?value", new BindingLiteralValue(value));
				bindings.addBinding("?requestPattern", new BindingLiteralValue(requestPattern));
				bindings.addBinding("?responsePattern", new BindingLiteralValue(responsePattern));
				
				return update(bindings);
			}
		}
		
		private class MappingRemover extends Producer {			
			public MappingRemover() {super("DELETE_MP_MAPPING");}
			
			public boolean removeMapping(String mapping){
				Bindings bindings = new Bindings();

				bindings.addBinding("?mapping", new BindingURIValue(mapping));
				
				return update(bindings);
			}
			
			public boolean removeAllMapping(){return update(null);}
		}

		public MPMappingManager() {
			creator = new MappingCreator();
			remover = new MappingRemover();
			listener = new MappingListener();
			updater = new MappingUpdater();
		}
		
		public boolean removeAllMapping(){
			return remover.removeAllMapping();
		}
		
		public boolean removeMapping(String mapping){
			return remover.removeMapping(mapping);
		}
		
		public boolean addMapping(String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
			return creator.addMapping(protocol, requestPattern, responsePattern, resource, action, value);
		}
		
		public boolean updateMapping(String mapping, String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
			return updater.updateMapping( mapping,  protocol, requestPattern, responsePattern, resource, action, value);
		}
			
		
		public boolean start(){			
			if(!creator.join()) return false;
			if(!remover.join()) return false;
			if(!listener.join()) return false;
			if(!updater.join()) return false;
			
			String subID = listener.subscribe(null);
			
			if (subID == null) {
				Logger.log(VERBOSITY.FATAL, tag,"Subscription FAILED");
				return false;
			}
			
			Logger.log(VERBOSITY.DEBUG, tag,"Subscription\t"+subID);
			
			Logger.log(VERBOSITY.INFO, tag, "Started");
			
			return true;
		}

		public boolean stop() {
			boolean ret = creator.leave();
			ret = ret && remover.leave();
			ret = ret && listener.unsubscribe();
			ret = ret && listener.leave();
					
			return ret;
		}
	}
	
	public class MNMappingManager {
		static final String tag = "MN MAPPING";
		
		private MappingCreator creator;
		private MappingRemover remover;
		private MappingListener listener;
		private MappingUpdater updater;
		private MNMappingEventListener event = null;
		
		public void setEventListener(MNMappingEventListener e) {
			event = e;
		}
			
		private class MappingListener extends Consumer {
			public MappingListener() {super("MN_MAPPING");}

			@Override
			public void notify(BindingsResults notify) {}

			@Override
			public void notifyAdded(ArrayList<Bindings> bindingsResults) {
				ArrayList<MNMapping> mappings = new ArrayList<MNMapping>();
				for (Bindings binding : bindingsResults) {
					mappings.add(new MNMapping(
							binding.getBindingValue("?mapping").getValue(),
							binding.getBindingValue("?network").getValue(),
							binding.getBindingValue("?requestPattern").getValue(),
							binding.getBindingValue("?responsePattern").getValue(),
							new ResourceAction(binding.getBindingValue("?resource").getValue(),binding.getBindingValue("?action").getValue(),binding.getBindingValue("?value").getValue())));
				}
				if(event != null) event.addedMNMappings(mappings);
			}

			@Override
			public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
				ArrayList<MNMapping> mappings = new ArrayList<MNMapping>();
				for (Bindings binding : bindingsResults) {
					mappings.add(new MNMapping(
							binding.getBindingValue("?mapping").getValue(),
							binding.getBindingValue("?network").getValue(),
							binding.getBindingValue("?requestPattern").getValue(),
							binding.getBindingValue("?responsePattern").getValue(),
							new ResourceAction(binding.getBindingValue("?resource").getValue(),binding.getBindingValue("?action").getValue(),binding.getBindingValue("?value").getValue())));
				}
				if(event != null) event.removedMNMappings(mappings);	
			}

			@Override
			public void notifyFirst(ArrayList<Bindings> bindingsResults) {
				notifyAdded(bindingsResults);
			}	
		}
		
		private class MappingCreator extends Producer {
			public MappingCreator() {super("INSERT_MN_MAPPING");}
			
			public boolean addMapping(String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
				Bindings bindings = new Bindings();
				String mapping = "iot:MN-Mapping_"+UUID.randomUUID().toString();
				bindings.addBinding("?mapping", new BindingURIValue(mapping));
				bindings.addBinding("?network", new BindingURIValue(protocol));
				bindings.addBinding("?resource", new BindingURIValue(resource));
				bindings.addBinding("?action", new BindingURIValue(action));
				bindings.addBinding("?value", new BindingLiteralValue(value));
				bindings.addBinding("?requestPattern", new BindingLiteralValue(requestPattern));
				bindings.addBinding("?responsePattern", new BindingLiteralValue(responsePattern));
				
				return update(bindings);
			}
		}
		
		private class MappingUpdater extends Producer {
			public MappingUpdater() {super("UPDATE_MN_MAPPING");}
			
			public boolean updateMapping(String mapping,String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
				Bindings bindings = new Bindings();
				bindings.addBinding("?mapping", new BindingURIValue(mapping));
				bindings.addBinding("?network", new BindingURIValue(protocol));
				bindings.addBinding("?resource", new BindingURIValue(resource));
				bindings.addBinding("?action", new BindingURIValue(action));
				bindings.addBinding("?value", new BindingLiteralValue(value));
				bindings.addBinding("?requestPattern", new BindingLiteralValue(requestPattern));
				bindings.addBinding("?responsePattern", new BindingLiteralValue(responsePattern));
				
				return update(bindings);
			}
		}
		
		private class MappingRemover extends Producer {
			public MappingRemover() {super("DELETE_MN_MAPPING");}
			
			public boolean removeMapping(String mapping){
				Bindings bindings = new Bindings();
				bindings.addBinding("?mapping", new BindingURIValue(mapping));
				
				return update(bindings);
			}
			
			public boolean removeAllMapping(){return update(null);}
		}

		public MNMappingManager() {
			creator = new MappingCreator();
			remover = new MappingRemover();
			listener = new MappingListener();
			updater = new MappingUpdater();
		}
		
		public boolean removeAllMapping(){
			return remover.removeAllMapping();
		}
		
		public boolean removeMapping(String mapping){
			return remover.removeMapping(mapping);
		}
		
		public boolean addMapping(String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
			return creator.addMapping(protocol, requestPattern, responsePattern, resource, action, value);
		}
		
		public boolean updateMapping(String mapping,String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
			return updater.updateMapping( mapping, protocol, requestPattern, responsePattern, resource, action, value);
		}
		
		public boolean start(){
			if(!creator.join()) return false;
			if(!remover.join()) return false;
			if(!listener.join()) return false;
			if(!updater.join()) return false;
			
			String subID = listener.subscribe(null);
			
			if (subID == null) {
				Logger.log(VERBOSITY.FATAL, tag, "Subscription FAILED");
				return false;
			}
			
			Logger.log(VERBOSITY.DEBUG, tag,"Subscription\t"+subID); 
			
			Logger.log(VERBOSITY.INFO, tag, "Started");
			
			return true;
		}

		public boolean stop() {
			boolean ret = creator.leave();
			ret = ret && remover.leave();
			ret = ret && listener.unsubscribe();
			ret = ret && listener.leave();
			return ret;
		}
	}
	
	public interface MappingEventListener {
		public void addedMNMappings(ArrayList<MNMapping> mappings);
		public void removedMNMappings(ArrayList<MNMapping> mappings);
		public void addedMPMappings(ArrayList<MPMapping> mappings);
		public void removedMPMappings(ArrayList<MPMapping> mappings);
	}
	
	public void setMappingEventListener(MappingEventListener e) {
		listener = e;
	}
	
	public boolean addDefaultMapping() {
		return addDefaultProtocolMapping() && addDefaultNetworkMapping();
	}
	private boolean addDefaultProtocolMapping() {
		//Protocols default mappings
		Logger.log(VERBOSITY.INFO, tag,"Adding default protocol mappings");
		
		// TODO add default mapping here
		if(!addProtocolMapping("iot:HTTP", "action=GET&resource=PINGPONG", "*", "iot:Resource_PINGPONG", "iot:GET", "*")) return false;
		if(!addProtocolMapping("iot:HTTP", "action=SET&resource=PINGPONG&value=*", "*", "iot:Resource_PINGPONG", "iot:SET", "*")) return false;

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
		Logger.log(VERBOSITY.INFO, tag,"Adding default network mappings");
		
		// TODO add default mapping here
		if(!addNetworkMapping("iot:PINGPONG", "GET", "GET&*", "iot:Resource_PINGPONG", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:PINGPONG", "SET=*", "SET&*", "iot:Resource_PINGPONG", "iot:SET", "*")) return false;

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
		*/
		/*		
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core0/temperature", "toffano/mml/Core0/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_0", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core1/temperature", "toffano/mml/Core1/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_1", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core2/temperature", "toffano/mml/Core2/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_2", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core3/temperature", "toffano/mml/Core3/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_3", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core4/temperature", "toffano/mml/Core4/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_4", "iot:GET", "*")) return false;
		if(!addNetworkMapping("iot:MQTT", "toffano/mml/Core5/temperature", "toffano/mml/Core5/temperature&*", "iot:Resource_SERVER_MML_TEMPERATURE_5", "iot:GET", "*")) return false;
*/	
		return true;
	}
	
	public MappingManager() {
		mpMappingManager = new MPMappingManager();
		mnMappingManager = new MNMappingManager();	
		mpMappingManager.setEventListener(this);
		mnMappingManager.setEventListener(this);
	}
	
	public boolean start(){		
		if(!mnMappingManager.start()) return false;
		if(!mpMappingManager.start()) return false;
			
		Logger.log(VERBOSITY.INFO, tag,"Started");
		
		removeAllMapping();
		addDefaultMapping();
		
		return true;
	}
	
	public boolean stop(){
		boolean ret = mnMappingManager.stop();
		ret = ret && mpMappingManager.stop();
		return ret;
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
	
	public boolean removeAllMapping(){
		if(!mnMappingManager.removeAllMapping()) return false;	
		if(!mpMappingManager.removeAllMapping()) return false;
		return true;
	}

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
