package arces.unibo.SEPA;

import java.util.HashMap;

import arces.unibo.KPI.KPICore;
import arces.unibo.KPI.SIBResponse;

public abstract class Client implements IClient{
	protected String PREFIXES = "";
	protected HashMap<String,String> URI2PrefixMap = new HashMap<String,String>();
	
	protected KPICore kp = null;
	protected SIBResponse ret;
	private boolean joined = false;
	
	private String defaultIP = "127.0.0.1";//"192.168.56.101"; //"127.0.0.1";
	private int defaultPort = 7701;//10010;//7701;
	private String defaultName ="SEPA Engine";
	
	private void addNamespace(String prefix,String uri){
		PREFIXES += "PREFIX " + prefix +":" + "<" + uri + "> ";
		URI2PrefixMap.put(uri, prefix);
	}
	
	public Client(String SIB_IP,int SIB_PORT,String SIB_NAME){
		System.out.println("SEPA Client Created IP:"+SIB_IP+" Port:"+SIB_PORT+" Name:"+SIB_NAME);
		kp = new KPICore(SIB_IP, SIB_PORT, SIB_NAME);	
	}
	
	public Client(){
		kp = new KPICore(defaultIP, defaultPort, defaultName);

		addNamespace("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		addNamespace("iot","http://www.arces.unibo.it/IoTGateway#");
		//kp.enable_debug_message();
		//kp.enable_error_message();
	}
	
	public boolean start() {
		ret = kp.join();
		joined = ret.isConfirmed();
		return joined;
	}
	
	protected boolean isJoined() {return joined;}
	
	public boolean stop() {
		if (!joined) return false;
		ret = kp.leave();
		joined = !ret.isConfirmed();
		return !joined;
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
