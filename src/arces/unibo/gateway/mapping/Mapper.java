package arces.unibo.gateway.mapping;

import arces.unibo.SEPA.application.Consumer;
import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;

public abstract class Mapper extends Consumer {

	protected Map map;
	
	public Mapper(ApplicationProfile appProfile,String SPARQL_SUBSCRIBE,Map map) {
		super(appProfile,SPARQL_SUBSCRIBE);
		this.map = map;
	}

	public abstract String name();
	
	public boolean join(){
		SEPALogger.log(VERBOSITY.DEBUG, name(), "Join");
		return super.join();
	}
	
	public String subscribe() {
		return super.subscribe(null);
	}
}
