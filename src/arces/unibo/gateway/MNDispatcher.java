package arces.unibo.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.SPARQLApplicationProfile;
import arces.unibo.gateway.mapping.ResourceAction;
import arces.unibo.gateway.mapping.mappers.network.DASH7Mapper;
import arces.unibo.gateway.mapping.mappers.network.INetworkMapper;
import arces.unibo.gateway.mapping.mappers.network.MQTTMapper;
import arces.unibo.gateway.mapping.mappers.network.PingPongMapper;
import arces.unibo.gateway.mapping.MNMapping;
import arces.unibo.gateway.mapping.MNRequest;
import arces.unibo.gateway.mapping.MNResponse;
import arces.unibo.gateway.mapping.Map;
import arces.unibo.gateway.mapping.Mapper;
import arces.unibo.gateway.mapping.Mappings;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public class MNDispatcher {
	private static String tag = "MN DISPATCHER";
	
	private MNRequestDispatcher mnRequestDispatcher;
	private MNResponseDispatcher mnResponseDispatcher;
	private MNMapper mnMapper;
	private MNMap mnMap;
	private HashMap<ResourceAction,ArrayList<MNRequest>> requestMap = new HashMap<ResourceAction,ArrayList<MNRequest>>();
	
	public class MNMap extends Map {
		private ArrayList<INetworkMapper> mappers = new ArrayList<INetworkMapper>();
		
		public MNMap(){
			//TODO: add all supported mappings here
			mappers.add(new PingPongMapper());
			mappers.add(new DASH7Mapper());
			mappers.add(new MQTTMapper());
			
			for(INetworkMapper mapper : mappers) {
				addMapper(mapper.getMapperURI(), mapper);
				Logging.log(VERBOSITY.INFO, "MN MAP",mapper.getMapperURI()+ "\tmapper added");
			}
		}
		
		public void addMapper(String key,INetworkMapper mapper){
			maps.put(key, new Mappings(mapper));	
		}
	}
	
	public class MNRequestDispatcher extends Aggregator {	
		private static final String tag = "MN REQUEST DISPATCHER";
		
		public MNRequestDispatcher() {super(SPARQLApplicationProfile.subscribe("RESOURCE_REQUEST"),SPARQLApplicationProfile.insert("MN_REQUEST"));}
		
		public String subscribe() {return super.subscribe(null);}
		
		@Override
		public void notify(BindingsResults notify) {}

		public void dispatch(MNRequest request){
			Bindings bindings = new Bindings();
			bindings.addBinding("?request", new BindingURIValue("iot:MN-Request_"+UUID.randomUUID().toString()));
			bindings.addBinding("?network", new BindingURIValue(request.getNetwork()));
			bindings.addBinding("?value", new BindingLiteralValue(request.getRequestString()));
			
			update(bindings);
		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			for(Bindings bindings : bindingsResults){
				String value = bindings.getBindingValue("?value").getValue();
				String action = bindings.getBindingValue("?action").getValue();
				String resource = bindings.getBindingValue("?resource").getValue();
				ResourceAction resourceAction= new ResourceAction(resource, action, value);
				
				//Mapping Resource Request to MN Request List
				ArrayList<MNRequest> mnRequestList = mnMap.resourceAction2MNRequestList(resourceAction);
				
				if (mnRequestList.isEmpty()) {
					bindings = new Bindings();
					ResourceAction response = new ResourceAction(resource, action, "MN-MAPPING NOT FOUND");// FOR "+resourceAction.toString());
					bindings.addBinding("?response", new BindingURIValue("iot:Resource-Response_"+UUID.randomUUID().toString()));
					bindings.addBinding("?resource", new BindingURIValue(response.getResourceURI()));
					bindings.addBinding("?action", new BindingURIValue(response.getActionURI()));
					bindings.addBinding("?value", new BindingLiteralValue(response.getValue()));

					Logging.log(VERBOSITY.INFO, tag, resourceAction.toString()+ " --> "+response.toString());
					
					if(!mnResponseDispatcher.update(bindings)) 
						Logging.log(VERBOSITY.ERROR,tag,"***RDF STORE UPDATE FAILED***");
						
					continue;
				}
				
				//Matching request cache
				boolean cacheHit = false;
				for (ResourceAction actionCache : requestMap.keySet()){
					if (actionCache.equals(resourceAction)){
						requestMap.get(actionCache).addAll(mnRequestList);
						cacheHit = true;
						break;	
					}
				}
				
				//Request not cached: add to cache
				if (!cacheHit) requestMap.put(resourceAction, mnRequestList);
				
				//Dispatch MN Requests
				for(MNRequest request : mnRequestList){
					bindings = new Bindings();
					bindings.addBinding("?request", new BindingURIValue(request.getURI()));
					bindings.addBinding("?network", new BindingURIValue(request.getNetwork()));
					bindings.addBinding("?value", new BindingLiteralValue(request.getRequestString()));
					
					Logging.log(VERBOSITY.INFO, tag,resourceAction.toString()+" --> "+request.toString());
					
					if(!update(bindings)) 
						Logging.log(VERBOSITY.ERROR,tag,"***RDF STORE UPDATE FAILED***");
				}
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);			
		}
	}
	
	public class MNResponseDispatcher extends Aggregator {		
		private static final String tag = "MN RESPONSE DISPATCHER";
		
		public MNResponseDispatcher() {super(SPARQLApplicationProfile.subscribe("MN_RESPONSE"),SPARQLApplicationProfile.insert("RESOURCE_RESPONSE"));}

		public String subscribe() {return super.subscribe(null);}
		
		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			for (Bindings bindings : bindingsResults){

				MNResponse response = new MNResponse(bindings.getBindingValue("?network").getValue(), bindings.getBindingValue("?value").getValue());
				
				//Mapping MN Response to Resource Response
				ResourceAction resourceAction = mnMap.mnResponse2ResourceAction(response);
				
				if (resourceAction == null) resourceAction = new ResourceAction("iot:NULL","iot:NULL","MN-MAPPING NOT FOUND");// FOR "+response.toString());
				
				bindings = new Bindings();
				bindings.addBinding("?response", new BindingURIValue("iot:Resource-Response_"+UUID.randomUUID().toString()));						
				bindings.addBinding("?resource", new BindingURIValue(resourceAction.getResourceURI()));
				bindings.addBinding("?action", new BindingURIValue(resourceAction.getActionURI()));
				bindings.addBinding("?value", new BindingLiteralValue(resourceAction.getValue()));

				Logging.log(VERBOSITY.INFO, tag,response.toString()+ " --> "+resourceAction.toString());
				
				if(!update(bindings)) 
					Logging.log(VERBOSITY.ERROR,tag,"***RDF STORE UPDATE FAILED***");
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindingsResults) {}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);	
		}
	}

	public class MNMapper extends Mapper {	
		public MNMapper(Map map) {super(SPARQLApplicationProfile.subscribe("MN_MAPPING"), map);}

		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public String name() {return "MN MAPPER";}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindings) {
			String network="";
			String resource="";
			String action="";
			String requestPattern="";
			String responsePattern="";
			String value = "";
			
			Logging.log(VERBOSITY.INFO, name(), "ADDED MAPPINGS");
			for (Bindings results : bindings){
				for(String var : results.getVariables()){
					String bindingValue = results.getBindingValue(var).getValue();
					switch(var){
						case "?network":
							network = bindingValue;
							break;
						case "?resource":
							resource = bindingValue;
							break;
						case "?action":
							action = bindingValue;
							break;
						case "?requestPattern":
							requestPattern = bindingValue;
							break;
						case "?responsePattern":
							responsePattern = bindingValue;
							break;
						case "?value":
							value = bindingValue;
							break;
					}
				}
				MNMapping mapping = new MNMapping(network,requestPattern,responsePattern,new ResourceAction(resource,action,value));
				if (map.addMapping(mapping)) 
					Logging.log(VERBOSITY.INFO, name(), mapping.toString());
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindings) {
			String network="";
			String resource="";
			String action="";
			String requestPattern="";
			String responsePattern="";
			String value = "";
			
			Logging.log(VERBOSITY.INFO, name(), "REMOVED MAPPINGS");
			for (Bindings results : bindings){
				for(String var : results.getVariables()){
					String bindingValue = results.getBindingValue(var).getValue();
					switch(var){
						case "?network":
							network = bindingValue;
							break;
						case "?resource":
							resource = bindingValue;
							break;
						case "?action":
							action = bindingValue;
							break;
						case "?requestPattern":
							requestPattern = bindingValue;
							break;
						case "?responsePattern":
							responsePattern = bindingValue;
							break;
						case "?value":
							value = bindingValue;
							break;
					}
				}
				MNMapping mapping = new MNMapping(network,requestPattern,responsePattern,new ResourceAction(resource,action,value));
				if (map.removeMapping(mapping)) Logging.log(VERBOSITY.INFO, name(), mapping.toString());
			}
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
		}
	}
	
	public MNDispatcher() {
		mnMap = new MNMap();
		mnRequestDispatcher = new MNRequestDispatcher();
		mnResponseDispatcher = new MNResponseDispatcher();
		mnMapper = new MNMapper(mnMap);
	}
	
	public boolean start() {
		if(!mnMapper.join()) return false;
		if(!mnRequestDispatcher.join()) return false;
		if(!mnResponseDispatcher.join()) return false;
		
		String subID = mnMapper.subscribe();
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"Mapper subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG, tag,"Mapper subscription\t"+subID);
		
		subID = mnRequestDispatcher.subscribe();
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"Request dispatcher subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG, tag,"Request dispatcher subscription\t"+subID);
		
		subID = mnResponseDispatcher.subscribe();
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL, tag,"Response dispatcher subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG, tag,"Response dispatcher subscription\t"+subID);
		
		Logging.log(VERBOSITY.INFO, tag,"Started");
		
		return true;
	}
	
	public boolean stop(){		
		boolean ret1 = mnResponseDispatcher.unsubscribe();
		if (!ret1) Logging.log(VERBOSITY.ERROR, tag,"Response Dispatcher unsubscribe FAILED");
		boolean ret2 = mnRequestDispatcher.unsubscribe();
		if (!ret2) Logging.log(VERBOSITY.ERROR, tag,"Request Dispatcher unsubscribe FAILED");
		boolean ret3 = mnMapper.unsubscribe();
		if (!ret3) Logging.log(VERBOSITY.ERROR, tag,"Mapper unsubscribe FAILED");
		
		return (ret1 && ret2 && ret3);
	}
}
