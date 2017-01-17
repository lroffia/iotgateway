package arces.unibo.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import arces.unibo.gateway.mapping.ResourceAction;
import arces.unibo.gateway.mapping.mappers.protocol.COAPMapper;
import arces.unibo.gateway.mapping.mappers.protocol.HTTPMapper;
import arces.unibo.gateway.mapping.mappers.protocol.IProtocolMapper;
import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.BindingsResults;
import arces.unibo.SEPA.commons.RDFTermLiteral;
import arces.unibo.SEPA.commons.RDFTermURI;
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
	
	private ApplicationProfile appProfile;
	
	public class MPMap extends Map {
		private ArrayList<IProtocolMapper> mappers = new ArrayList<IProtocolMapper>();
		
		public MPMap(){
			//TODO: add all supported mappings here
			mappers.add(new HTTPMapper());
			mappers.add(new COAPMapper());
			
			for(IProtocolMapper mapper : mappers) {
				addMapper(mapper.getMapperURI(), mapper);
				Logger.log(VERBOSITY.INFO, "MP MAP",mapper.getMapperURI() + "\tmapper added");
			}
		}
		
		public void addMapper(String key,IProtocolMapper mapper){
			maps.put(key, new Mappings(mapper));	
		}
	}
	
	public class MPMapper extends Mapper{
		public MPMapper(Map map) {super(appProfile,"MP_MAPPING", map);}

		@Override
		public String name() {return "MP MAPPER";}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			String protocol="";
			String resource="";
			String action="";
			String requestPattern="";
			String responsePattern="";
			String value = "";
			
			for (Bindings results : bindingsResults.getBindings()){
				for(String var : results.getVariables()){
					String bindingValue = results.getBindingValue(var);
					switch(var){
						case "protocol":
							protocol = bindingValue;
							break;
						case "resource":
							resource = bindingValue;
							break;
						case "action":
							action = bindingValue;
							break;
						case "requestPattern":
							requestPattern = bindingValue;
							break;
						case "responsePattern":
							responsePattern = bindingValue;
							break;
						case "value":
							value = bindingValue;
							break;
					}
				}
				MPMapping mapping = new MPMapping(protocol,requestPattern,responsePattern,new ResourceAction(resource,action,value));
				if(map.addMapping(mapping)) Logger.log(VERBOSITY.INFO, name() ,"ADDED MAPPING " + mapping.toString());
			}
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			String protocol="";
			String resource="";
			String action="";
			String requestPattern="";
			String responsePattern="";
			String value = "";
			
			for (Bindings results : bindingsResults.getBindings()){
				for(String var  : results.getVariables()){
					String bindingValue = results.getBindingValue(var);
					switch(var){
						case "protocol":
							protocol = bindingValue;
							break;
						case "resource":
							resource = bindingValue;
							break;
						case "action":
							action = bindingValue;
							break;
						case "requestPattern":
							requestPattern = bindingValue;
							break;
						case "responsePattern":
							responsePattern = bindingValue;
							break;
						case "value":
							value = bindingValue;
							break;
					}
				}
				MPMapping mapping = new MPMapping(protocol,requestPattern,responsePattern,new ResourceAction(resource,action,value));
				if(map.removeMapping(mapping)) Logger.log(VERBOSITY.INFO, name(),"REMOVED MAPPING " + mapping.toString());
			}
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
		}
	}
	
	class MPRequestDispatcher extends Aggregator {
		private static final String tag = "MP REQUEST DISPATCHER";
		
		public MPRequestDispatcher(){ 
			super(appProfile,"MP_REQUEST","INSERT_RESOURCE_PENDING_REQUEST");}
		
		public String subscribe() {return super.subscribe(null);}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for(Bindings bindings : bindingsResults.getBindings()){
				String protocolURI = bindings.getBindingValue("protocol");
				String requestString = bindings.getBindingValue("value");
				String mpRequest = bindings.getBindingValue("request");
				
				//Mapping MP-Request to Resource-Pending-Request
				MPRequest request = new MPRequest(protocolURI, requestString,mpRequest);
				
				Logger.log(VERBOSITY.INFO,tag,"<< " + request.toString());
				
				ResourceAction resourceAction = mpMap.mpRequest2ResourceAction(request);
				
				//MP-Mapping NOT FOUND
				if (resourceAction == null){
					MPResponse response = new MPResponse(protocolURI,"MP-MAPPING NOT FOUND FOR "+request.toString());
					
					bindings = new Bindings();
					bindings.addBinding("request", new RDFTermURI(mpRequest));
					bindings.addBinding("response", new RDFTermURI(response.getURI()));
					bindings.addBinding("value", new RDFTermLiteral(response.getResponseString()));
					bindings.addBinding("protocol", new RDFTermURI(response.getProtocol()));
					
					Logger.log(VERBOSITY.WARNING,tag,">> " + response.toString());
					
					mpResponseDispatcher.update(bindings);
						
					continue;
				}
				
				//Dispatch Resource-Pending-Request
				bindings = new Bindings();
				bindings.addBinding("request", new RDFTermURI("iot:Resource-Pending-Request_"+UUID.randomUUID().toString()));
				bindings.addBinding("resource", new RDFTermURI(resourceAction.getResourceURI()));
				bindings.addBinding("action", new RDFTermURI(resourceAction.getActionURI()));
				bindings.addBinding("value", new RDFTermLiteral(resourceAction.getValue()));
				
				Logger.log(VERBOSITY.INFO,tag, ">> Resource-Pending-Request " + resourceAction.toString());
				
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
			}
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
			
		}
	}
	
	class MPResponseDispatcher extends Aggregator {
		private static final String tag = "MP RESPONSE DISPATCHER";
		
		public MPResponseDispatcher(){super(appProfile,"RESOURCE_RESPONSE","INSERT_MP_RESPONSE");}
		
		public String subscribe() {return super.subscribe(null);}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for(Bindings bindings : bindingsResults.getBindings()){

				ResourceAction resource = new ResourceAction(bindings.getBindingValue("resource"), 
						bindings.getBindingValue("action"),  
						bindings.getBindingValue("value"));
				
				Logger.log(VERBOSITY.INFO,tag,"<< Resource-Response " + resource.toString());
				
				ArrayList<MPRequest> requests = getRequests(resource);
				
				if (requests == null) {
					Logger.log(VERBOSITY.WARNING,tag,"MP-REQUEST NOT FOUND FOR "+resource.toString());
					return;
				}
				
				for (MPRequest request : requests){					
					//Mapping Resource-Response to MP-Response
					String responseString = mpMap.resourceAction2MPResponseString(request.getProtocol(), resource);
					
					if (responseString == null) continue;
					
					MPResponse response = new MPResponse(request.getProtocol(), responseString);
					
					bindings = new Bindings();
					bindings.addBinding("request", new RDFTermURI(request.getURI()));
					bindings.addBinding("response", new RDFTermURI(response.getURI()));
					bindings.addBinding("value", new RDFTermLiteral(response.getResponseString()));
					bindings.addBinding("protocol", new RDFTermURI(response.getProtocol()));
					
					Logger.log(VERBOSITY.INFO,tag,">> " + response.toString());
					
					update(bindings);
				}
				
				removeResourceActionRequest(resource);
			}
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
			
		}
	}
	
	public MPDispatcher(ApplicationProfile appProfile) {
		this.appProfile = appProfile;
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
			Logger.log(VERBOSITY.FATAL,tag,"Mapper subscription FAILED");
			return false;
		}
		
		Logger.log(VERBOSITY.DEBUG,tag,"Mapper subscription\t"+subID);
		
		subID = mpRequestDispatcher.subscribe();
		
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL,tag,"Request dispatcher subscription FAILED");
			return false;
		}
		
		Logger.log(VERBOSITY.DEBUG,tag,"Request dispatcher subscription\t"+subID);
		
		subID = mpResponseDispatcher.subscribe();
		
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL,tag,"Response dispatcher subscription FAILED");
			return false;
		}
		
		Logger.log(VERBOSITY.DEBUG,tag,"Response dispatcher subscription\t"+subID);
		
		Logger.log(VERBOSITY.INFO,tag,"Started");
		
		return true;
	}
	
	public boolean stop(){				
		boolean ret1 = mpResponseDispatcher.unsubscribe();
		if (!ret1) Logger.log(VERBOSITY.ERROR,tag,"Response Dispatcher unsubscribe FAILED");
		boolean ret2 = mpRequestDispatcher.unsubscribe();
		if (!ret2) Logger.log(VERBOSITY.ERROR,tag,"Request Dispatcher unsubscribe FAILED");
		boolean ret3 = mpMapper.unsubscribe();
		if (!ret3) Logger.log(VERBOSITY.ERROR,tag,"Mapper unsubscribe FAILED");
		
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
