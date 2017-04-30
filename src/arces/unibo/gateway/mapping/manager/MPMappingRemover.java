package arces.unibo.gateway.mapping.manager;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Producer;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;

public class MPMappingRemover extends Producer {			
	public MPMappingRemover(ApplicationProfile appProfile) {super(appProfile,"DELETE_MP_MAPPING");}
	
	public boolean removeMapping(String mapping){
		Bindings bindings = new Bindings();

		bindings.addBinding("mapping", new RDFTermURI(mapping));
		
		return update(bindings);
	}
	
	public boolean removeAllMapping(){return update(null);}
}
