package arces.unibo.gateway.mapping.manager;

import java.util.UUID;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Producer;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;

public class MPMappingCreator extends Producer {			
	public MPMappingCreator(ApplicationProfile appProfile) {super(appProfile,"INSERT_MP_MAPPING");}
	
	public boolean addMapping(String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
		Bindings bindings = new Bindings();
		String mapping = "iot:MP-Mapping_"+UUID.randomUUID().toString();
		bindings.addBinding("mapping", new RDFTermURI(mapping));
		bindings.addBinding("protocol", new RDFTermURI(protocol));
		bindings.addBinding("resource", new RDFTermURI(resource));
		bindings.addBinding("action", new RDFTermURI(action));
		bindings.addBinding("value", new RDFTermLiteral(value));
		bindings.addBinding("requestPattern", new RDFTermLiteral(requestPattern));
		bindings.addBinding("responsePattern", new RDFTermLiteral(responsePattern));
		
		return update(bindings);
	}
}
