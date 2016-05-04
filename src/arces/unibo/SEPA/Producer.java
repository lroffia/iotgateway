package arces.unibo.SEPA;

import java.io.IOException;

import org.jdom2.JDOMException;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public class Producer extends Client implements IProducer {
	private String SPARQL_UPDATE = "";
	
	private String tag = "SEPA PRODUCER";
	
	private boolean retry = true;
	private static final int maxRetries = 3;
	private int nRetry = 0;
	
	public Producer(String updateQuery,String SIB_IP,int SIB_PORT,String SIB_NAME){
		super(SIB_IP,SIB_PORT,SIB_NAME);
		SPARQL_UPDATE = updateQuery;
	}
	public Producer(String updateQuery) {
		this(updateQuery,defaultIP, defaultPort, defaultName);
	}
	
	public synchronized boolean update(Bindings forcedBindings){
		 if (!isJoined()) {
			 Logging.log(VERBOSITY.ERROR,tag,"UPDATE FAILED because client is not joined");
			 return false;
		 }
		 
		 String sparql = prefixes() + replaceBindings(SPARQL_UPDATE,forcedBindings).replace("\n", "").replace("\r", "");
		 
		 Logging.log(VERBOSITY.DEBUG,tag,"UPDATE "+sparql);
		 
		 nRetry = 0;
		 retry = true;
		 ret = null;
		 
		 while (retry){
			try 
			{
				ret = kp.update_sparql(sparql);
			} 
			catch (JDOMException | IOException e) {
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
