package arces.unibo.gateway.mapping.manager;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Producer;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;

public class MPMappingUpdater extends Producer {			
	public MPMappingUpdater(ApplicationProfile appProfile) {super(appProfile,"UPDATE_MP_MAPPING");}
	
	public boolean updateMapping(String mapping, String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
		Bindings bindings = new Bindings();
		bindings.addBinding("mapping", new RDFTermURI(mapping));
		bindings.addBinding("protocol", new RDFTermURI(protocol));
		bindings.addBinding("resource", new RDFTermURI(resource));
		bindings.addBinding("action", new RDFTermURI(action));
		bindings.addBinding("value", new RDFTermURI(value));
		bindings.addBinding("requestPattern", new RDFTermLiteral(requestPattern));
		bindings.addBinding("responsePattern", new RDFTermLiteral(responsePattern));
		
		return update(bindings);
	}
}
