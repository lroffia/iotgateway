package arces.unibo.gateway.adapters.protocol;

import java.util.ArrayList;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.SPARQLApplicationProfile;
import arces.unibo.gateway.mapping.MPRequest;
import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public abstract class MPAdapter {
	public abstract String adapterName();
	public abstract String protocolURI();
	protected abstract void mpResponse(String requestURI,String responseString);
	protected abstract boolean doStart();
	protected abstract void doStop();
	
	private MPRequestResponseDispatcher dispatcher;
	
	class MPRequestResponseDispatcher extends Aggregator {
		
		public String subscribe() {
			Bindings bindings = new Bindings();
			bindings.addBinding("?protocol", new BindingURIValue(protocolURI()));
			return super.subscribe(bindings);	
		}
		
		public MPRequestResponseDispatcher(){
			super(SPARQLApplicationProfile.subscribe("MP_RESPONSE"),SPARQLApplicationProfile.insert("MP_REQUEST"));
		}
		
		@Override
		public void notify(BindingsResults notify) {
			Logging.log(VERBOSITY.DEBUG,adapterName(),"MP RESPONSE NOTIFICATION");
		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindings) {
			for(Bindings binding : bindings) {
				String requestURI = binding.getBindingValue("?request").getValue();
				String responseString = binding.getBindingValue("?value").getValue();
				
				Logging.log(VERBOSITY.INFO,adapterName(),"<< MP-Response<"+responseString+">");
				
				mpResponse(requestURI,responseString);
			}
		}

		@Override
		public void notifyRemoved(ArrayList<Bindings> bindings) {
			
		}

		@Override
		public void notifyFirst(ArrayList<Bindings> bindingsResults) {
			notifyAdded(bindingsResults);
		}
	}
	
	public boolean start(){
		dispatcher = new MPRequestResponseDispatcher();
		if (!dispatcher.join()) return false;
		
		String subID = dispatcher.subscribe();
		
		if (subID == null) {
			Logging.log(VERBOSITY.FATAL,adapterName(),"Subscription FAILED");
			return false;
		}
		
		Logging.log(VERBOSITY.DEBUG,adapterName(),"Subscription "+subID);
		
		return doStart();
	}
	
	public boolean stop(){
		doStop();
		
		return dispatcher.unsubscribe();
		
	}
	
	protected final String mpRequest(String value) {
		Bindings bindings = new Bindings();
		
		MPRequest request = new MPRequest(protocolURI(), value);
		
		bindings.addBinding("?request", new BindingURIValue(request.getURI()));
		bindings.addBinding("?value", new BindingLiteralValue(request.getRequestString()));
		bindings.addBinding("?protocol", new BindingURIValue(request.getProtocol()));
		
		//SPARQL UPDATE
		Logging.log(VERBOSITY.INFO,adapterName(),">> " + request.toString());
		
		if(!dispatcher.update(bindings)) {
			Logging.log(VERBOSITY.ERROR,adapterName(),"***RDF STORE UPDATE FAILED***");
			return null;
		}
		
		return request.getURI();
	}
}
