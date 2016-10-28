package arces.unibo.gateway.adapters.protocol;

import java.util.ArrayList;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.Logger;
import arces.unibo.SEPA.Logger.VERBOSITY;
import arces.unibo.gateway.mapping.MPRequest;

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
			super("MP_RESPONSE","INSERT_MP_REQUEST");
		}
		
		@Override
		public void notify(BindingsResults notify) {
			Logger.log(VERBOSITY.DEBUG,adapterName(),"MP RESPONSE NOTIFICATION");
		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindings) {
			for(Bindings binding : bindings) {
				String requestURI = binding.getBindingValue("?request").getValue();
				String responseString = binding.getBindingValue("?value").getValue();
				
				Logger.log(VERBOSITY.INFO,adapterName(),"<< MP-Response<"+responseString+">");
				
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
			Logger.log(VERBOSITY.FATAL,adapterName(),"Subscription FAILED");
			return false;
		}
		
		Logger.log(VERBOSITY.DEBUG,adapterName(),"Subscription "+subID);
		
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
		Logger.log(VERBOSITY.INFO,adapterName(),">> " + request.toString());
		
		if(!dispatcher.update(bindings)) {
			Logger.log(VERBOSITY.ERROR,adapterName(),"***RDF STORE UPDATE FAILED***");
			return null;
		}
		
		return request.getURI();
	}
}
