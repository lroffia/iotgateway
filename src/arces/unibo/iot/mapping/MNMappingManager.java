package arces.unibo.iot.mapping;

import java.util.UUID;

import arces.unibo.iot.SEPA.BindingLiteralValue;
import arces.unibo.iot.SEPA.BindingURIValue;
import arces.unibo.iot.SEPA.Bindings;
import arces.unibo.iot.SEPA.Producer;

public class MNMappingManager extends Producer {

	private static String MN_MAPPING =
			" INSERT { "
			+ "?mapping rdf:type iot:MN-Mapping . "
			+ "?mapping iot:hasNetwork ?network . "
			+ "?mapping iot:hasContext ?context . "
			+ "?mapping iot:hasAction ?action . "
			+ "?mapping iot:hasValue ?value . "
			+ "?mapping iot:hasMNRequestPattern ?requestPattern . "
			+ "?mapping iot:hasMNResponsePattern ?responsePattern"
			+ " }";
	
	public MNMappingManager() {
		super(MN_MAPPING);
	}
	
	public boolean addMapping(String network,String requestPattern,String responsePattern,String context,String action,String value){
		Bindings bindings = new Bindings();
		String mapping = "iot:MN-Mapping_"+UUID.randomUUID().toString();
		bindings.addBinding("?mapping", new BindingURIValue(mapping));
		bindings.addBinding("?network", new BindingURIValue(network));
		bindings.addBinding("?context", new BindingURIValue(context));
		bindings.addBinding("?action", new BindingURIValue(action));
		bindings.addBinding("?value", new BindingLiteralValue(value));
		bindings.addBinding("?requestPattern", new BindingLiteralValue(requestPattern));
		bindings.addBinding("?responsePattern", new BindingLiteralValue(responsePattern));
		
		return update(bindings);	
	}

}
