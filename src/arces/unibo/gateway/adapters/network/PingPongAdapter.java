package arces.unibo.gateway.adapters.network;

import java.io.IOException;

import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.Logger;
import arces.unibo.SEPA.Producer;
import arces.unibo.SEPA.SPARQLApplicationProfile;
import arces.unibo.SEPA.Logger.VERBOSITY;

public class PingPongAdapter extends MNAdapter {
	static String status = "INIT";
	
	static Producer resourceCreator;
	
	static private void addResource(String uri,String value) {
		Bindings bindings = new Bindings();
		bindings.addBinding("?resource", new BindingURIValue(uri));
		bindings.addBinding("?value", new BindingLiteralValue(value));
		resourceCreator.update(bindings);	
	}
	
	public static void main(String[] args) throws IOException {
		
		PingPongAdapter adapter;
		adapter =new PingPongAdapter();
		
		if(adapter.start()) {
			Logger.log(VERBOSITY.INFO,adapter.adapterName(),"Connected to gateway "+
					SPARQLApplicationProfile.getParameters().getUrl()+":"+
					SPARQLApplicationProfile.getParameters().getPort()+"@"+
					SPARQLApplicationProfile.getParameters().getName());
			
			resourceCreator = new Producer("INSERT_RESOURCE");
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
	
	public PingPongAdapter(){
		super();
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
