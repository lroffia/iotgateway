package arces.unibo.SEPA;

import java.util.Vector;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;
import sofia_kp.SSAP_sparql_response;
import sofia_kp.iKPIC_subscribeHandler2;

public abstract class Consumer extends Client implements IConsumer {
	private String SPARQL_SUBSCRIBE = "";
	private String subID ="";
	private iKPIC_subscribeHandler2 mHandler = null;

	private String tag = "SEPA CONSUMER";
	
	public Consumer(String subscribeQuery,String SIB_IP,int SIB_PORT,String SIB_NAME){
		super(SIB_IP,SIB_PORT,SIB_NAME);
		SPARQL_SUBSCRIBE = subscribeQuery.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", "").trim();;
	}
	
	public Consumer(String subscribeQuery) {
		this(subscribeQuery,defaultIP, defaultPort, defaultName);
	}
	
	public String subscribe(Bindings forcedBindings) {
		if (mHandler != null) return subID;
		
		mHandler = new NotificationHandler();
		
		String sparql = prefixes() + replaceBindings(SPARQL_SUBSCRIBE,forcedBindings);
		
		Logging.log(VERBOSITY.DEBUG,tag,"<SUBSCRIBE> ==> "+sparql);
		
		ret = kp.subscribeSPARQL(sparql, mHandler);

		if (ret == null) return null;		
		subID = ret.subscription_id;
		if(!ret.isConfirmed()) return null;
		
		Logging.log(VERBOSITY.DEBUG,tag,"SUBSCRIBE "+subID);
		
		BindingsResults queryResults = new BindingsResults(ret.sparqlquery_results,null,URI2PrefixMap);
		if (queryResults != null) {
			if (queryResults.getAddedBindings().isEmpty()) return subID;
			
			Consumer.this.notifyFirst(queryResults.getAddedBindings());
		}
		
		return subID;
	}
	 
	public final boolean unsubscribe() {
		Logging.log(VERBOSITY.DEBUG,tag,"UNSUBSCRIBE "+subID);
		ret = kp.unsubscribe(subID);
		if (ret == null) return false;		
		return ret.isConfirmed();
	}
	
	public class NotificationHandler implements iKPIC_subscribeHandler2{
		public void kpic_RDFEventHandler(Vector<Vector<String>> newTriples, Vector<Vector<String>> oldTriples, String indSequence, String subID ){}
		
		public void kpic_SPARQLEventHandler(SSAP_sparql_response newResults, SSAP_sparql_response oldResults, String indSequence, String subID ){				
			Logging.log(VERBOSITY.DEBUG, tag, "NOTIFY " + subID + " n. "+ indSequence);
			
			BindingsResults results = new BindingsResults(newResults,oldResults,URI2PrefixMap);
			
			Consumer.this.notify(results);

			if (results.getAddedBindings() != null) 
				if (results.getAddedBindings().size() > 0) Consumer.this.notifyAdded(results.getAddedBindings());
			if (results.getRemovedBindings() != null) 
				if (results.getRemovedBindings().size() > 0) Consumer.this.notifyRemoved(results.getRemovedBindings());
		}
		
		public void kpic_UnsubscribeEventHandler(String sub_ID ){}
		
		public void kpic_ExceptionEventHandler(Throwable SocketException ){}
	}
}
