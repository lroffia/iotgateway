package arces.unibo.SEPA;

import java.util.Vector;

import arces.unibo.KPI.SSAP_sparql_response;
import arces.unibo.KPI.iKPIC_subscribeHandler2;

public class Consumer extends Client implements IConsumer {
	private String SPARQL_SUBSCRIBE = "SELECT ?subject ?predicate ?object WHERE { ?subject ?predicate ?object }";
	private String subID ="";
	private NotificationHandler mHandler;
	protected BindingsResults queryResults = null;
	
	public Consumer(String query) {
		super();
		SPARQL_SUBSCRIBE = query;
		mHandler = new NotificationHandler();
	}
	
	public Consumer() {
		super();
		mHandler = new NotificationHandler();
	}
	
	public void notify(BindingsResults notify) {
		System.out.println("\n<---- NOTIFY\n"+notify.toString());
	}

	public boolean subscribe(Bindings forcedBindings) {
		if (!subID.equals("")) return false;
		
		String sparql = PREFIXES + replaceBindings(SPARQL_SUBSCRIBE,forcedBindings);
		
		System.out.println(">> SUBSCRIBE "+sparql);
		
		ret = kp.subscribeSPARQL(sparql, mHandler);
		
		if (ret == null) return false;		
		subID = ret.subscription_id;
		if(!ret.isConfirmed()) return false;
		
		System.out.println("<< SUB ID: "+subID);
		
		queryResults = new BindingsResults(ret.sparqlquery_results,null,URI2PrefixMap);
		
		return true;
	}
	
	public boolean subscribe() {
		return subscribe(null);
	}
	
	public boolean unsubscribe() {
		System.out.println(">> UNSUBSCRIBE "+subID);
		ret = kp.unsubscribe(subID);
		if (ret == null) return false;		
		if(!ret.Status.equals("m3:Success")) return false;
		return true;
	}
	
	public class NotificationHandler implements iKPIC_subscribeHandler2{
		public void kpic_RDFEventHandler(Vector<Vector<String>> newTriples, Vector<Vector<String>> oldTriples, String indSequence, String subID ){
			
		}
		
		public void kpic_SPARQLEventHandler(SSAP_sparql_response newResults, SSAP_sparql_response oldResults, String indSequence, String subID ){	
			BindingsResults results = new BindingsResults(newResults,oldResults,URI2PrefixMap);
			Consumer.this.notify(results);
		}
		
		public void kpic_UnsubscribeEventHandler(String sub_ID ){
			
		}
		
		public void kpic_ExceptionEventHandler(Throwable SocketException ){
			
		}
	}

	@Override
	public BindingsResults getQueryResults() {
		return queryResults;
	}
}
