package arces.unibo.gateway.dispatching;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.response.ErrorResponse;
import arces.unibo.gateway.mapping.MPMapping;
import arces.unibo.gateway.mapping.Map;
import arces.unibo.gateway.mapping.Mapper;
import arces.unibo.gateway.mapping.ResourceAction;

public class MPMapper extends Mapper{
	private static final Logger logger = LogManager.getLogger("MPMapper");
	
	public MPMapper(ApplicationProfile appProfile,Map map) {super(appProfile,"MP_MAPPING", map);}

	@Override
	public String name() {return "MP MAPPER";}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {

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
			if(map.addMapping(mapping)) logger.info("ADDED MAPPING " + mapping.toString());
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
			if(map.removeMapping(mapping)) logger.info("REMOVED MAPPING " + mapping.toString());
		}
		
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
		notifyAdded(bindingsResults,spuid,0);
	}

	@Override
	public void brokenSubscription() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(ErrorResponse errorResponse) {
		// TODO Auto-generated method stub
		
	}
}

