package arces.unibo.SEPA;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public abstract class Aggregator extends Consumer implements IAggregator {
	private String SPARQL_UPDATE = "INSERT { ?subject ?predicate ?object }";
	
	private String tag = "SEPA AGGREGATOR";
	
	private boolean retry = true;
	private int maxRetries = 3;
	private int nRetry = 0;
	
	public Aggregator(String subscribeQuery,String updateQuery,String SIB_IP,int SIB_PORT,String SIB_NAME){
		super(subscribeQuery,SIB_IP,SIB_PORT,SIB_NAME);
		SPARQL_UPDATE = updateQuery.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", "").trim();
	}
	public Aggregator(String subscribeQuery,String updateQuery) {
		this(subscribeQuery,updateQuery,defaultIP, defaultPort, defaultName);
	}
	
	public synchronized boolean update(Bindings forcedBindings){
		 if (!isJoined()) {
			 Logging.log(VERBOSITY.ERROR,tag,"UPDATE FAILED because client is not joined");
			 return false;
		 }
		 
		 String sparql = prefixes() + replaceBindings(SPARQL_UPDATE,forcedBindings);
		 Logging.log(VERBOSITY.DEBUG,tag,"<UPDATE> ==> "+sparql);
		 
		 nRetry = 0;
		 retry = true;
		 ret = null;
		 
		 while (retry){

			ret = kp.update_sparql(sparql);
			
			if (!ret.isConfirmed()) {
				nRetry++;
				Logging.log(VERBOSITY.ERROR,tag,"UPDATE FAILED ("+nRetry+"/"+maxRetries+") "+sparql);
				if (nRetry == maxRetries) retry = false;
				continue;
			}
			retry = false;
		 }
		 
		 if (ret == null) {
			 Logging.log(VERBOSITY.ERROR,tag,"Update return value is NULL");
			 return false;
		 }
		 
		 Logging.log(VERBOSITY.DEBUG, tag, "Status = " + ret.Status);
		 return ret.isConfirmed();
	 }
}
