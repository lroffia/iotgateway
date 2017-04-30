package arces.unibo.gateway.mapping.manager;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Producer;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;

public class MNMappingUpdater extends Producer {
	public MNMappingUpdater(ApplicationProfile appProfile) {super(appProfile,"UPDATE_MN_MAPPING");}
	
	public boolean updateMapping(String mapping,String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
		Bindings bindings = new Bindings();
		bindings.addBinding("mapping", new RDFTermURI(mapping));
		bindings.addBinding("network", new RDFTermURI(protocol));
		bindings.addBinding("resource", new RDFTermURI(resource));
		bindings.addBinding("action", new RDFTermURI(action));
		bindings.addBinding("value", new RDFTermLiteral(value));
		bindings.addBinding("requestPattern", new RDFTermLiteral(requestPattern));
		bindings.addBinding("responsePattern", new RDFTermLiteral(responsePattern));
		
		return update(bindings);
	}
}

