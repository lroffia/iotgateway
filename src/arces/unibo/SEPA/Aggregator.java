package arces.unibo.SEPA;

import java.util.UUID;

public class Aggregator extends Consumer implements IAggregator {
	private String SPARQL_UPDATE = "INSERT { ?subject ?predicate ?object }";
	
	public Aggregator(String subscribeQuery,String updateQuery) {
		super(subscribeQuery);
		SPARQL_UPDATE = updateQuery;
	}

	public Aggregator() {
		super();
	}
	
	 public boolean update(Bindings forcedBindings){
		 String sparql = PREFIXES + replaceBindings(SPARQL_UPDATE,forcedBindings);
		 
		 System.out.println(">> UPDATE "+sparql);
		 
		 ret = kp.update_sparql(sparql);
		 
		 if (ret == null) return false;
		 return ret.Status.equals("m3:Success");
	 }
	 
	 public boolean update(){
		 Bindings forcedBindings = new Bindings();
		 forcedBindings.addBinding("?subject", new BindingURIValue("default:"+UUID.randomUUID().toString()));
		 forcedBindings.addBinding("?predicate", new BindingURIValue("default:"+UUID.randomUUID().toString()));
		 forcedBindings.addBinding("?object", new BindingURIValue("default:"+UUID.randomUUID().toString()));
		 
		 return update(forcedBindings);
	 }
}
