package arces.unibo.iot.SEPA;

public abstract class Aggregator extends Consumer implements IAggregator {
	private String SPARQL_UPDATE = "INSERT {<INSERT_PATTERN>} DELETE {<DELETE_PATTERN>}";
	
	public Aggregator(String subscribeQuery,String updateQuery) {
		super(subscribeQuery);
		SPARQL_UPDATE = updateQuery;
	}

	 public boolean update(Bindings forcedBindings){
		 String sparql = PREFIXES + replaceBindings(SPARQL_UPDATE,forcedBindings);
		 
		 if(debug) System.out.println(">> "+sparql);
		 
		 ret = kp.update_sparql(sparql);
		 
		 return ret.Status.equals("m3:Success");
	 }
}
