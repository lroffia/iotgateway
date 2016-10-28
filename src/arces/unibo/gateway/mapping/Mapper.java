package arces.unibo.gateway.mapping;

import arces.unibo.SEPA.Consumer;
import arces.unibo.SEPA.Logger;
import arces.unibo.SEPA.Logger.VERBOSITY;

public abstract class Mapper extends Consumer {

	protected Map map;
	
	public Mapper(String SPARQL_SUBSCRIBE,Map map) {
		super(SPARQL_SUBSCRIBE);
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
