package arces.unibo.gateway.mapping.manager;

import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Producer;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.RDFTermURI;

public class MPMappingRemover extends Producer {			
	public MPMappingRemover(ApplicationProfile appProfile) {super(appProfile,"DELETE_MP_MAPPING");}
	
	public boolean removeMapping(String mapping){
		Bindings bindings = new Bindings();

		bindings.addBinding("mapping", new RDFTermURI(mapping));
		
		return update(bindings);
	}
	
	public boolean removeAllMapping(){return update(null);}
}
