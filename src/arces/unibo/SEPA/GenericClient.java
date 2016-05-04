package arces.unibo.SEPA;

import java.io.IOException;
import java.util.Vector;

import org.jdom2.JDOMException;

import arces.unibo.KPI.SIBResponse;
import arces.unibo.KPI.SSAP_sparql_response;
import arces.unibo.KPI.iKPIC_subscribeHandler2;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public class GenericClient extends Client {
	private String subID ="";
	private NotificationHandler mHandler = null;
	private Notification listener = null;
	
	public interface Notification {
		public void notify(BindingsResults notify);
		public void notifyFirst(BindingsResults notify);
	}
	
	public GenericClient(String SIB_IP,int SIB_PORT,String SIB_NAME, Notification listener){
		super(SIB_IP,SIB_PORT,SIB_NAME);
		this.listener = listener; 
	}
	
	public boolean update(String SPARQL_UPDATE,Bindings forced) {
		
		if (!isJoined()) return false;
		
		String sparql = prefixes() + super.replaceBindings(SPARQL_UPDATE,forced).replace("\n", "").replace("\r", "");
		
		try 
		{
			ret = kp.update_sparql(sparql);
		} 
		catch (JDOMException | IOException e) {
			Logging.log(VERBOSITY.FATAL,"SEPA","Update FAILED "+sparql);
			return false;
		}
		
		if (ret == null) return false;
		 
		 return ret.isConfirmed();
	 }
	
	public BindingsResults query(String SPARQL_QUERY,Bindings forced) {
		if (!isJoined()) return null;
		
		String sparql = prefixes() + super.replaceBindings(SPARQL_QUERY,forced).replace("\n", "").replace("\r", "");
		
		SIBResponse ret;
		try 
		{
			ret = kp.querySPARQL(sparql);
		} 
		catch (JDOMException | IOException e) {
			Logging.log(VERBOSITY.FATAL,"SEPA","Query FAILED "+sparql);
			return null;
		}
		
		BindingsResults queryResults = new BindingsResults(ret.sparqlquery_results,null,URI2PrefixMap);
		
		return queryResults;
	}
	
	public String subscribe(String SPARQL_SUBSCRIBE,Bindings forced) {
		if (mHandler != null) return subID;
		
		mHandler = new NotificationHandler();
		
		String sparql = prefixes() + super.replaceBindings(SPARQL_SUBSCRIBE,forced).replace("\n", "").replace("\r", "");
		
		Logging.log(VERBOSITY.DEBUG,"SEPA","Subscribe "+sparql);
		
		try 
		{
			ret = kp.subscribeSPARQL(sparql, mHandler);
		} 
		catch (JDOMException | IOException e) 
		{
			e.printStackTrace();
			return null;
		}
		
		if (ret == null) return null;		
		
		subID = ret.subscription_id;
		
		if(!ret.isConfirmed()) return null;
		
		Logging.log(VERBOSITY.DEBUG,"SEPA","Subscribe "+subID);
		
		BindingsResults queryResults = new BindingsResults(ret.sparqlquery_results,null,URI2PrefixMap);
		if (queryResults != null) {
			if (queryResults.getAddedBindings().isEmpty()) return subID;
			
			listener.notifyFirst(queryResults);
		}
		
		return subID;
	}
	 
	public final boolean unsubscribe() {
		Logging.log(VERBOSITY.DEBUG,"SEPA","Unsubscribe "+subID);
		
		ret = kp.unsubscribe(subID);
		
		mHandler = null;
		subID = "";
		
		if (ret == null) return false;		
		
		return ret.isConfirmed();
	}
	
	public class NotificationHandler implements iKPIC_subscribeHandler2{
		public void kpic_RDFEventHandler(Vector<Vector<String>> newTriples, Vector<Vector<String>> oldTriples, String indSequence, String subID ){}
		
		public void kpic_SPARQLEventHandler(SSAP_sparql_response newResults, SSAP_sparql_response oldResults, String indSequence, String subID ){	
			BindingsResults results = new BindingsResults(newResults,oldResults,URI2PrefixMap);
			listener.notify(results);
		}
		
		public void kpic_UnsubscribeEventHandler(String sub_ID ){}
		
		public void kpic_ExceptionEventHandler(Throwable SocketException ){}
	}
}
