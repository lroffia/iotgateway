package arces.unibo.gateway.adapters.network;

import java.io.IOException;

import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.Producer;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.RDFTermLiteral;
import arces.unibo.SEPA.commons.RDFTermURI;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;

public class PingPongAdapter extends MNAdapter {
	static String status = "INIT";
	
	static Producer resourceCreator;
	
	static private void addResource(String uri,String value) {
		Bindings bindings = new Bindings();
		bindings.addBinding("resource", new RDFTermURI(uri));
		bindings.addBinding("value", new RDFTermLiteral(value));
		resourceCreator.update(bindings);	
	}
	
	public static void main(String[] args) throws IOException {
		
		ApplicationProfile appProfile = new ApplicationProfile();
		
		Logger.loadSettings();
		
		if(!appProfile.load("GatewayProfile.xml")) {
			Logger.log(VERBOSITY.FATAL, "DASH7", "Failed to load: "+ "GatewayProfile.xml");
			return;
		}
		else Logger.log(VERBOSITY.INFO, "DASH7", "Loaded application profile "+ "GatewayProfile.xml");
		
		PingPongAdapter adapter;
		adapter =new PingPongAdapter(appProfile);
		
		if(adapter.start()) {
			Logger.log(VERBOSITY.INFO,adapter.adapterName(),"Connected to gateway "+
					appProfile.getParameters().getUrl()+":"+
					appProfile.getParameters().getUpdatePort()+"@"+
					appProfile.getParameters().getPath());
			
			resourceCreator = new Producer(appProfile,"INSERT_RESOURCE");
			if (!resourceCreator.join()) return ;
			addResource("iot:Resource_PINGPONG",status);
			resourceCreator.leave();
		}
		else{
			Logger.log(VERBOSITY.FATAL,adapter.adapterName(),adapter.adapterName() + " is NOT running");	
		}
		Logger.log(VERBOSITY.INFO,adapter.adapterName(),"Press any key to exit...");
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
		Logger.log(VERBOSITY.INFO,adapterName(),"<< Request<"+request+">");
		String response = "GET&"+status;
		if(request.contains("SET")) {
			String[] values = request.split("=");
			status = values[1];
			response = "SET&"+status;
		}
	
		Logger.log(VERBOSITY.INFO,adapterName(),">> Response<"+response+">");
		mnResponse(response);
	}

	@Override
	protected boolean doStart() {
		Logger.log(VERBOSITY.INFO,adapterName(),"Started");
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
