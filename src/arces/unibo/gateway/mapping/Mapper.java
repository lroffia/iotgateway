package arces.unibo.gateway.mapping;

import arces.unibo.SEPA.application.Consumer;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;

public abstract class Mapper extends Consumer {

	protected Map map;
	
	public Mapper(ApplicationProfile appProfile,String SPARQL_SUBSCRIBE,Map map) {
		super(appProfile,SPARQL_SUBSCRIBE);
		this.map = map;
	}

	public abstract String name();
	
	public boolean join(){
		Logger.log(VERBOSITY.DEBUG, name(), "Join");
		return super.join();
	}
	
	public String subscribe() {
		return super.subscribe(null);
	}
}
