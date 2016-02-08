package arces.unibo.gateway.mapping;

import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;

public class MNMapper extends Mapper {
	private static String MN_MAPPER =
			" SELECT ?network ?requestPattern ?responsePattern ?context ?action ?value WHERE { "
			+ "?mapping rdf:type iot:MN-Mapping . "
			+ "?mapping iot:hasNetwork ?network . "
			+ "?mapping iot:hasContext ?context . "
			+ "?mapping iot:hasAction ?action . "
			+ "?mapping iot:hasValue ?value . "
			+ "?mapping iot:hasMNRequestPattern ?requestPattern . "
			+ "?mapping iot:hasMNResponsePattern ?responsePattern }";
	
	public MNMapper(Map map) {
		super(MN_MAPPER, map);
	}

	@Override
	public void notify(BindingsResults notify) {
		String network="";
		String context="";
		String action="";
		String requestPattern="";
		String responsePattern="";
		String value = "";
		
		System.out.println(this.name()+": new mappings");
		System.out.println("-------------------------------------------------------");
		if (notify.getAddedBindings() != null){
			for (Bindings results : notify.getAddedBindings()){
				for(String var : results.getVariables()){
					String bindingValue = results.getBindingValue(var).getValue();
					System.out.printf("-- %s=%s ", var,bindingValue);
					switch(var){
						case "?network":
							network = bindingValue;
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
				System.out.printf(" --\n");
				map.addMapping(network, new ContextAction(context,action,value),requestPattern, responsePattern);
			}
		}
		System.out.println("-------------------------------------------------------");
		
		System.out.println(this.name()+": removed mappings");
		System.out.println("-------------------------------------------------------");
		if (notify.getRemovedBindings() != null){
			for (Bindings results : notify.getRemovedBindings()){
				for(String var : results.getVariables()){
					String bindingValue = results.getBindingValue(var).getValue();
					System.out.printf("-- %s=%s ", var,bindingValue);
					switch(var){
						case "?network":
							network = bindingValue;
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
				System.out.printf(" --\n");
				map.removeMapping(network, new ContextAction(context,action,value),requestPattern, responsePattern);
			}
		}
		System.out.println("-------------------------------------------------------");
	}

	@Override
	public String name() {
		return "MN Mapper";
	}

}
