package arces.unibo.gateway.mapping;

import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;

public class MPMapper extends Mapper{
	private static String MP_MAPPER =
			"SELECT ?protocol ?requestPattern ?responsePattern ?context ?action ?value WHERE {"
			+ "?mapping rdf:type iot:MP-Mapping . "
			+ "?mapping iot:hasProtocol ?protocol . "
			+ "?mapping iot:hasContext ?context . "
			+ "?mapping iot:hasAction ?action . "
			+ "?mapping iot:hasValue ?value . "
			+ "?mapping iot:hasMPRequestPattern ?requestPattern . "
			+ "?mapping iot:hasMPResponsePattern ?responsePattern"
			+ " }";
	
	public MPMapper(Map map) {
		super(MP_MAPPER, map);
	}

	@Override
	public String name() {
		return "MP-Mapper";
	}
	
	@Override
	public void notify(BindingsResults notify) {
		System.out.println(notify.toString());
		
		String protocol="";
		String context="";
		String action="";
		String requestPattern="";
		String responsePattern="";
		String value = "";
		
		if (notify.getAddedBindings() != null){
			for (Bindings results : notify.getAddedBindings()){
				for(String var : results.getVariables()){
					String bindingValue = results.getBindingValue(var).getValue();
					switch(var){
						case "?protocol":
							protocol = bindingValue;
							break;
						case "?context":
							context = bindingValue;
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
				map.addMapping(protocol, new ContextAction(context,action,value),requestPattern, responsePattern);
			}
		}
		
		if (notify.getRemovedBindings() != null){
			for (Bindings results : notify.getRemovedBindings()){
				for(String var  : results.getVariables()){
					String bindingValue = results.getBindingValue(var).getValue();
					switch(var){
						case "?protocol":
							protocol = bindingValue;
							break;
						case "?context":
							context = bindingValue;
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
				map.removeMapping(protocol, new ContextAction(context,action,value),requestPattern, responsePattern);
			}
		}
	}
}
