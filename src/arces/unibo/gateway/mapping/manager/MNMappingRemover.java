package arces.unibo.gateway.mapping.manager;

import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Producer;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;

public class MNMappingRemover extends Producer {
	public MNMappingRemover(ApplicationProfile appProfile) {super(appProfile,"DELETE_MN_MAPPING");}
	
	public boolean removeMapping(String mapping){
		Bindings bindings = new Bindings();
		bindings.addBinding("mapping", new RDFTermURI(mapping));
		
		return update(bindings);
	}
	
	public boolean removeAllMapping(){return update(null);}
}

