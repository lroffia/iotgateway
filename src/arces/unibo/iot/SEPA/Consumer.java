package arces.unibo.iot.SEPA;

import java.util.Vector;

import arces.unibo.iot.KPI.SSAP_sparql_response;

public abstract class Consumer extends Client implements IConsumer{
	private String SPARQL_SUBSCRIBE = "SELECT <VARIABLES> WHERE {<QUERY_PATTERN>}";
	private String subID ="";
	
	public Consumer(String query) {
		super();
		SPARQL_SUBSCRIBE = query;
	}

	public BindingsResults subscribe(Bindings forcedBindings) {
		String sparql = PREFIXES + replaceBindings(SPARQL_SUBSCRIBE,forcedBindings);
		
		if (debug) System.out.println(">> SUBSCRIBE "+sparql);
		ret = kp.subscribeSPARQL(sparql, this);
		subID = ret.subscription_id;
		
		if(!ret.Status.equals("m3:Success")) return null;
		
		return new BindingsResults(ret.sparqlquery_results,null,URI2PrefixMap);
	}
	
	public boolean unsubscribe() {
		if(debug) System.out.println("### UNSUBSCRIBE "+subID+" ###");
		ret = kp.unsubscribe(subID);
		return true;
	}
	
	public void kpic_RDFEventHandler(Vector<Vector<String>> newTriples, Vector<Vector<String>> oldTriples, String indSequence, String subID ){
		
	}
	
	public void kpic_SPARQLEventHandler(SSAP_sparql_response newResults, SSAP_sparql_response oldResults, String indSequence, String subID ){	
		BindingsResults results = new BindingsResults(newResults,oldResults,URI2PrefixMap);
		notify(results);
		
		if(debug) System.out.println("*** NOTIFICATION ***\n"+results.toString());
	}
	
	public void kpic_UnsubscribeEventHandler(String sub_ID ){
		
	}
	
	public void kpic_ExceptionEventHandler(Throwable SocketException ){
		
	}	
}
