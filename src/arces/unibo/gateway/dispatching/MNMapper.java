package arces.unibo.gateway.dispatching;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.gateway.mapping.MNMapping;
import arces.unibo.gateway.mapping.Map;
import arces.unibo.gateway.mapping.Mapper;
import arces.unibo.gateway.mapping.ResourceAction;

public class MNMapper extends Mapper {	
	public MNMapper(ApplicationProfile appProfile,Map map) {super(appProfile,"MN_MAPPING", map);}
	private static final Logger logger = LogManager.getLogger("MNMapper");
	
	@Override
	public String name() {return "MN MAPPER";}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		String network="";
		String resource="";
		String action="";
		String requestPattern="";
		String responsePattern="";
		String value = "";
		
		logger.info("ADDED MAPPINGS");
		for (Bindings results : bindingsResults.getBindings()){
			for(String var : results.getVariables()){
				String bindingValue = results.getBindingValue(var);
				switch(var){
					case "network":
						network = bindingValue;
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
			MNMapping mapping = new MNMapping(network,requestPattern,responsePattern,new ResourceAction(resource,action,value));
			if (map.addMapping(mapping)) 
				logger.info( mapping.toString());
		}
		
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		String network="";
		String resource="";
		String action="";
		String requestPattern="";
		String responsePattern="";
		String value = "";
		
		logger.info("REMOVED MAPPINGS");
		for (Bindings results : bindingsResults.getBindings()){
			for(String var : results.getVariables()){
				String bindingValue = results.getBindingValue(var);
				switch(var){
					case "network":
						network = bindingValue;
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
			MNMapping mapping = new MNMapping(network,requestPattern,responsePattern,new ResourceAction(resource,action,value));
			if (map.removeMapping(mapping)) logger.info(mapping.toString());
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
}

