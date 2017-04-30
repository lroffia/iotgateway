package arces.unibo.gateway.adapters.network;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Producer;

import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;

public class PingPongAdapter extends MNAdapter {
	static String status = "INIT";
	static String appProfileFile = "GatewayProfile.sap";
	static Producer resourceCreator;
	static ApplicationProfile appProfile;
	
	private static final Logger logger = LogManager.getLogger("PingPongAdapter");
	
	static private void addResource(String uri,String value) {
		Bindings bindings = new Bindings();
		bindings.addBinding("resource", new RDFTermURI(uri));
		bindings.addBinding("value", new RDFTermLiteral(value));
		resourceCreator.update(bindings);	
	}
	
	public static void main(String[] args) throws IOException {
		
		appProfile = new ApplicationProfile();
		
		if(!appProfile.load(appProfileFile)) {
			logger.fatal("Failed to load: "+ appProfileFile);
			return;
		}
		else logger.info("Loaded application profile "+ appProfileFile);
		
		PingPongAdapter adapter;
		adapter =new PingPongAdapter(appProfile);
		
		if(adapter.start()) {
			logger.info("Running...");
		}
		else{
			logger.fatal(" is NOT running");	
		}
		logger.info("Press any key to exit...");
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
		logger.info("<< Request<"+request+">");
		String response = "GET&"+status;
		if(request.contains("SET")) {
			String[] values = request.split("=");
			status = values[1];
			response = "SET&"+status;
		}
	
		logger.info(">> Response<"+response+">");
		mnResponse(response);
	}

	@Override
	protected boolean doStart() {
		resourceCreator = new Producer(appProfile,"INSERT_RESOURCE");
		if (!resourceCreator.join()) return false;
		addResource("iot:Resource_PINGPONG",status);
		resourceCreator.leave();
		logger.info("Started");
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
