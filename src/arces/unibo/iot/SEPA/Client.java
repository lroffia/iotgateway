package arces.unibo.iot.SEPA;

import java.util.HashMap;

import arces.unibo.iot.KPI.KPICore;
import arces.unibo.iot.KPI.SIBResponse;

public abstract class Client implements Runnable, IClient {
	protected String PREFIXES = "";
	protected HashMap<String,String> URI2PrefixMap = new HashMap<String,String>();
	
	protected KPICore kp = null;
	protected SIBResponse ret;
	protected boolean joined = false;
	
	private String defaultIP ="127.0.0.1";
	private int defaultPort = 7701;
	private String defaultName ="IoTGateway";
	
	//private String defaultIP ="192.168.56.101";
	//private int defaultPort = 10010;
	
	protected void addNamespace(String prefix,String uri){
		PREFIXES += "PREFIX " + prefix +":" + "<" + uri + "> ";
		URI2PrefixMap.put(uri, prefix);
	}
	
	public Client(String SIB_IP,int SIB_PORT,String SIB_NAME){
		if(debug) System.out.println("SEPA Client Created IP:"+SIB_IP+" Port:"+SIB_PORT+" Name:"+SIB_NAME);
		kp = new KPICore(SIB_IP, SIB_PORT, SIB_NAME);	
	}
	
	public Client(){
		kp = new KPICore(defaultIP, defaultPort, defaultName);
		addNamespace("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		addNamespace("iot","http://www.arces.unibo.it/IoTGateway#");
		//kp.enable_debug_message();
		//kp.enable_error_message();
	}
	
	private boolean join() {
		ret = kp.join();
		return ret.Status.equals("m3:Success");
	}
	
	private boolean leave() {
		ret = kp.leave();
		return !ret.Status.equals("m3:Success");
	}
	
	public void run() {
		joined = join();
		while(joined) {
			try{
				Thread.sleep(10);
			}
			catch(InterruptedException e){
				leave();
				return;
			}
		}
		leave();
	}
	
	
	protected String replaceBindings(String sparql, Bindings bindings){
		if (bindings == null) return sparql;
		
		String replacedSparql = String.format("%s", sparql);
		String selectPattern = "";
		
		if (sparql.contains("SELECT")) {
			selectPattern = replacedSparql.substring(0, sparql.indexOf('{'));
			replacedSparql = replacedSparql.substring(sparql.indexOf('{'), replacedSparql.length());
		}
		for (String var : bindings.getVariables()) {
			if (bindings.getBindingValue(var).isLiteral()) 
				replacedSparql = replacedSparql.replace(var,"\""+bindings.getBindingValue(var).getValue()+"\"");
			else	
				replacedSparql = replacedSparql.replace(var,bindings.getBindingValue(var).getValue());
		}
		
		return selectPattern+replacedSparql;
	}
}
