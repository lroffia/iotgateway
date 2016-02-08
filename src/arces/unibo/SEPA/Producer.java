package arces.unibo.SEPA;

import java.util.UUID;

public class Producer extends Client implements IProducer {
	private String SPARQL_UPDATE = "INSERT { ?subject ?predicate ?object }";
	
	public Producer(String query) {
		super();
		SPARQL_UPDATE = query;
	}

	public Producer() {
		super();
	}
	
	 public boolean update(Bindings forcedBindings){
		 if (!isJoined()) return false;
		 
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
