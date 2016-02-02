package arces.unibo.iot.mapping;

import arces.unibo.iot.SEPA.BindingsResults;
import arces.unibo.iot.SEPA.Consumer;

public abstract class Mapper extends Consumer {

	protected Map map;
	
	public Mapper(String SPARQL_SUBSCRIBE,Map map) {
		super(SPARQL_SUBSCRIBE);
		this.map = map;
	}

	public abstract String name();
	
	public boolean start(){
		//SPARQL SUBSCRIBE
		BindingsResults ret = subscribe(null);
		if (ret == null) return false;
				
		System.out.println(ret.toString());
		
		notify(ret);
		
		return true;
	}
}
