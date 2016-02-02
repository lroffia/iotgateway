package arces.unibo.iot.SEPA;

public abstract class Producer extends Client implements IProducer {
	private String SPARQL_UPDATE = "INSERT {<INSERT_PATTERN>} DELETE {<DELETE_PATTERN>}";
	
	public Producer(String query) {
		super();
		SPARQL_UPDATE = query;
	}

	 public boolean update(Bindings forcedBindings){
		 String sparql = PREFIXES + replaceBindings(SPARQL_UPDATE,forcedBindings);
		 
		 if(debug) System.out.println(">> "+sparql);
		 
		 ret = kp.update_sparql(sparql);
		 
		 return ret.Status.equals("m3:Success");
	 }
}
