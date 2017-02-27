package arces.unibo.gateway.adapters.network;

import java.io.IOException;

import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.Producer;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;

public class PingPongAdapter extends MNAdapter {
	static String status = "INIT";
	static String appProfileFile = "GatewayProfile.sap";
	static Producer resourceCreator;
	static ApplicationProfile appProfile;
	
	static private void addResource(String uri,String value) {
		Bindings bindings = new Bindings();
		bindings.addBinding("resource", new RDFTermURI(uri));
		bindings.addBinding("value", new RDFTermLiteral(value));
		resourceCreator.update(bindings);	
	}
	
	public static void main(String[] args) throws IOException {
		
		appProfile = new ApplicationProfile();
		
		if(!appProfile.load(appProfileFile)) {
			SEPALogger.log(VERBOSITY.FATAL,"PINGPONG ADAPTER", "Failed to load: "+ appProfileFile);
			return;
		}
		else SEPALogger.log(VERBOSITY.INFO, "PINGPONG ADAPTER", "Loaded application profile "+ appProfileFile);
		
		PingPongAdapter adapter;
		adapter =new PingPongAdapter(appProfile);
		
		if(adapter.start()) {
			SEPALogger.log(VERBOSITY.INFO,adapter.adapterName(),"Running...");
		}
		else{
			SEPALogger.log(VERBOSITY.FATAL,adapter.adapterName(),adapter.adapterName() + " is NOT running");	
		}
		SEPALogger.log(VERBOSITY.INFO,adapter.adapterName(),"Press any key to exit...");
		System.in.read();
		
		adapter.stop();		
	}
	
	public PingPongAdapter(ApplicationProfile appProfile){
		super(appProfile);
	}
	
	@Override
	public String networkURI() {
		return "iot:PINGPONG";
	}

	@Override
	public void mnRequest(String request) {
		SEPALogger.log(VERBOSITY.INFO,adapterName(),"<< Request<"+request+">");
		String response = "GET&"+status;
		if(request.contains("SET")) {
			String[] values = request.split("=");
			status = values[1];
			response = "SET&"+status;
		}
	
		SEPALogger.log(VERBOSITY.INFO,adapterName(),">> Response<"+response+">");
		mnResponse(response);
	}

	@Override
	protected boolean doStart() {
		resourceCreator = new Producer(appProfile,"INSERT_RESOURCE");
		if (!resourceCreator.join()) return false;
		addResource("iot:Resource_PINGPONG",status);
		resourceCreator.leave();
		SEPALogger.log(VERBOSITY.INFO,adapterName(),"Started");
		return true;
	}

	@Override
	protected void doStop() {
	}

	@Override
	public String adapterName() {
		return "PINGPONG ADAPTER";
	}
}
