package arces.unibo.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.SPARQLApplicationProfile;
import arces.unibo.gateway.mapping.ResourceAction;
import arces.unibo.gateway.mapping.mappers.protocol.COAPMapper;
import arces.unibo.gateway.mapping.mappers.protocol.HTTPMapper;
import arces.unibo.gateway.mapping.mappers.protocol.IProtocolMapper;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

import arces.unibo.gateway.mapping.MPMapping;
import arces.unibo.gateway.mapping.MPRequest;
import arces.unibo.gateway.mapping.MPResponse;
import arces.unibo.gateway.mapping.Map;
import arces.unibo.gateway.mapping.Mapper;
import arces.unibo.gateway.mapping.Mappings;

public class MPDispatcher {
	private static String tag = "MP DISPATCHER";
	
	private MPRequestDispatcher mpRequestDispatcher;
	private MPResponseDispatcher mpResponseDispatcher;
	private MPMapper mpMapper;
	private MPMap mpMap;
	private HashMap<ResourceAction,ArrayList<MPRequest>> requestMap = new HashMap<ResourceAction,ArrayList<MPRequest>>();
	
	public class MPMap extends Map {
		private ArrayList<IProtocolMapper> mappers = new ArrayList<IProtocolMapper>();
		
		public MPMap(){
			//TODO: add all supported mappings here
			mappers.add(new HTTPMapper());
			mappers.add(new COAPMapper());
			
			for(IProtocolMapper mapper : mappers) {
				addMapper(mapper.getMapperURI(), mapper);
				Logging.log(VERBOSITY.INFO, "MP MAP",mapper.getMapperURI() + "\tmapper added");
			}
		}
		
		public void addMapper(String key,IProtocolMapper mapper){
			maps.put(key, new Mappings(mapper));	
		}
	}
	
	public class MPMapper extends Mapper{
		public MPMapper(Map map) {super(SPARQLApplicationProfile.subscribe("MP_MAPPING"), map);}

		@Override
		public String name() {return "MP MAPPER";}
		
		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindings) {
			String protocol="";
			String resource="";
			String action="";
			String requestPattern="";
			String responsePattern="";
			String value = "";
			
			for (Bindings results : bindings){
				for(String var : results.getVariables()){
					String bindingValue = results.getBindingValue(var).getValue();
					switch(var){
						case "?protocol":
							protocol = bindingValue;
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
				MPMapping mapping = new MPMapping(protocol,requestPattern,responsePattern,new ResourceAction(resource,action,value));
				if(map.addMapping(mapping)) Logging.log(VERBOSITY.INFO, name() ,"ADDED MAPPING " + mapping.toString());
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindings) {
			String protocol="";
			String resource="";
			String action="";
			String requestPattern="";
			String responsePattern="";
			String value = "";
			
			for (Bindings results : bindings){
				for(String var  : results.getVariables()){
					String bindingValue = results.getBindingValue(var).getValue();
					switch(var){
						case "?protocol":
							protocol = bindingValue;
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
				MPMapping mapping = new MPMapping(protocol,requestPattern,responsePattern,new ResourceAction(resource,action,value));
				if(map.removeMapping(mapping)) Logging.log(VERBOSITY.INFO, name(),"REMOVED MAPPING " + mapping.toString());
			}
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
		}
	}
	
	class MPRequestDispatcher extends Aggregator {
		private static final String tag = "MP REQUEST DISPATCHER";
		
		public MPRequestDispatcher(){ 
			super(SPARQLApplicationProfile.subscribe("MP_REQUEST"),
					SPARQLApplicationProfile.insert("RESOURCE_PENDING_REQUEST"));}
		
		public String subscribe() {return super.subscribe(null);}
		
		@Override
		public void notify(BindingsResults results) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			for(Bindings bindings : bindingsResults){
				String protocolURI = bindings.getBindingValue("?protocol").getValue();
				String requestString = bindings.getBindingValue("?value").getValue();
				String mpRequest = bindings.getBindingValue("?request").getValue();
				
				//Mapping MP-Request to Resource-Pending-Request
				MPRequest request = new MPRequest(protocolURI, requestString,mpRequest);
				
				Logging.log(VERBOSITY.INFO,tag,"<< " + request.toString());
				
				ResourceAction resourceAction = mpMap.mpRequest2ResourceAction(request);
				
				//MP-Mapping NOT FOUND
				if (resourceAction == null){
					MPResponse response = new MPResponse(protocolURI,"MP-MAPPING NOT FOUND FOR "+request.toString());
					bindings = new Bindings();
					bindings.addBinding("?request", new BindingURIValue(mpRequest));
					bindings.addBinding("?response", new BindingURIValue(response.getURI()));
					bindings.addBinding("?value", new BindingLiteralValue(response.getResponseString()));
					bindings.addBinding("?protocol", new BindingURIValue(response.getProtocol()));
					
					Logging.log(VERBOSITY.INFO,tag,">> " + response.toString());
					
					if(!mpResponseDispatcher.update(bindings)) 
						Logging.log(VERBOSITY.ERROR,tag,"***RDF STORE UPDATE FAILED***");
						
					continue;
				}
				
				//Dispatch Resource-Pending-Request
				bindings = new Bindings();
				bindings.addBinding("?request", new BindingURIValue("iot:Resource-Pending-Request_"+UUID.randomUUID().toString()));
				bindings.addBinding("?resource", new BindingURIValue(resourceAction.getResourceURI()));
				bindings.addBinding("?action", new BindingURIValue(resourceAction.getActionURI()));
				bindings.addBinding("?value", new BindingLiteralValue(resourceAction.getValue()));
				
				Logging.log(VERBOSITY.INFO,tag,">> " + resourceAction.toString());
				
				if(update(bindings)) {		
					//MP-Request cache matching
					for(ResourceAction resource : requestMap.keySet()){
						if (resource.equals(resourceAction)) {
							requestMap.get(resource).add(request);
							return;
						}
					}
	
					//Add request to cache
					ArrayList<MPRequest> mpRequestsList = new ArrayList<MPRequest>();
					mpRequestsList.add(request);
					requestMap.put(resourceAction, mpRequestsList);
				}
				else Logging.log(VERBOSITY.ERROR,tag,"***RDF STORE UPDATE FAILED***");
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
	
	class MPResponseDispatcher extends Aggregator {
		private static final String tag = "MP RESPONSE DISPATCHER";
		
		public MPResponseDispatcher(){super(SPARQLApplicationProfile.subscribe("RESOURCE_RESPONSE"),SPARQLApplicationProfile.insert("MP_RESPONSE"));}
		
		public String subscribe() {return super.subscribe(null);}
		
		@Override
		public void notify(BindingsResults notify) {}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindingsResults) {
			for(Bindings bindings : bindingsResults){

				ResourceAction resource = new ResourceAction(bindings.getBindingValue("?resource").getValue(), 
						bindings.getBindingValue("?action").getValue(),  
						bindings.getBindingValue("?value").getValue());
				
				ArrayList<MPRequest> requests = getRequests(resource);
				
				if (requests == null) {
					Logging.log(VERBOSITY.WARNING,tag,"MP-REQUEST NOT FOUND");// FOR "+resource.toString());
					return;
				}
				
				for (MPRequest request : requests){					
					//Mapping Resource-Response to MP-Response
					String responseString = mpMap.resourceAction2MPResponseString(request.getProtocol(), resource);
					
					if (responseString == null) continue;
					
					MPResponse response = new MPResponse(request.getProtocol(), responseString);
					
					bindings = new Bindings();
					bindings.addBinding("?request", new BindingURIValue(request.getURI()));
					bindings.addBinding("?response", new BindingURIValue(response.getURI()));
					bindings.addBinding("?value", new BindingLiteralValue(response.getResponseString()));
					bindings.addBinding("?protocol", new BindingURIValue(response.getProtocol()));
					
					Logging.log(VERBOSITY.INFO,tag,resource.toString() + " --> " + response.toString());
					
					if(!update(bindings)) 
						Logging.log(VERBOSITY.ERROR,tag,"***RDF STORE UPDATE FAILED***");
				}
				
				removeResourceActionRequest(resource);
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindings) {
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
		}
	}
	
	public MPDispatcher() {
		mpMap = new MPMap();
		mpRequestDispatcher = new MPRequestDispatcher();
		mpResponseDispatcher = new MPResponseDispatcher();
		mpMapper = new MPMapper(mpMap);
	}
	
	public boolean start() {		
		if(!mpMapper.join()) return false;
		if(!mpRequestDispatcher.join()) return false;
		if(!mpResponseDispatcher.join()) return false;
		
		String subID = mpMapper.subscribe();
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL,tag,"Mapper subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG,tag,"Mapper subscription\t"+subID);
		
		subID = mpRequestDispatcher.subscribe();
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL,tag,"Request dispatcher subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG,tag,"Request dispatcher subscription\t"+subID);
		
		subID = mpResponseDispatcher.subscribe();
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL,tag,"Response dispatcher subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG,tag,"Response dispatcher subscription\t"+subID);
		
		Logging.log(VERBOSITY.INFO,tag,"Started");
		
		return true;
	}
	
	public boolean stop(){				
		boolean ret1 = mpResponseDispatcher.unsubscribe();
		if (!ret1) Logging.log(VERBOSITY.ERROR,tag,"Response Dispatcher unsubscribe FAILED");
		boolean ret2 = mpRequestDispatcher.unsubscribe();
		if (!ret2) Logging.log(VERBOSITY.ERROR,tag,"Request Dispatcher unsubscribe FAILED");
		boolean ret3 = mpMapper.unsubscribe();
		if (!ret3) Logging.log(VERBOSITY.ERROR,tag,"Mapper unsubscribe FAILED");
		
		return (ret1 && ret2 && ret3);
	}
	
	private ArrayList<MPRequest> getRequests(ResourceAction action) {
		Iterator<ResourceAction> resources = requestMap.keySet().iterator();
		while(resources.hasNext()) {
			ResourceAction resourceAction = resources.next();
			if (resourceAction.equals(action)) {
				return requestMap.get(resourceAction);
			}
		}
		return null;
	}
	
	private void removeResourceActionRequest(ResourceAction request){
		Iterator<ResourceAction> resources = requestMap.keySet().iterator();
		while(resources.hasNext()) {
			ResourceAction resource = resources.next();
			if (resource.equals(request)) {
				requestMap.remove(resource);
				return;
			}
		}	
	}
}
