package arces.unibo.gateway.mapping;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Consumer;

public abstract class Mapper extends Consumer {
	private static final Logger logger = LogManager.getLogger("Mapper");
	protected Map map;
	
	public Mapper(ApplicationProfile appProfile,String SPARQL_SUBSCRIBE,Map map) {
		super(appProfile,SPARQL_SUBSCRIBE);
		this.map = map;
	}

	public abstract String name();
	
	public boolean join(){
		logger.info("Join: "+name());
		return super.join();
	}
	
	public String subscribe() {
		return super.subscribe(null);
	}
}
