package arces.unibo.gateway.mapping;

import arces.unibo.SEPA.Consumer;

public abstract class Mapper extends Consumer implements IMapper {

	protected Map map;
	
	public Mapper(String SPARQL_SUBSCRIBE,Map map) {
		super(SPARQL_SUBSCRIBE);
		this.map = map;
	}

	public abstract String name();
	
	public boolean start(){
		System.out.println("*********************");
		System.out.println("Mapper: "+name());
		System.out.println("*********************");
		
		return super.start();
	}
}
