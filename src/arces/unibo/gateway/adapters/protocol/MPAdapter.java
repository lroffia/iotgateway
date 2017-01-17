package arces.unibo.gateway.adapters.protocol;

import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.BindingsResults;
import arces.unibo.SEPA.commons.RDFTermLiteral;
import arces.unibo.SEPA.commons.RDFTermURI;
import arces.unibo.gateway.mapping.MPRequest;

public abstract class MPAdapter {
	public abstract String adapterName();
	public abstract String protocolURI();
	protected abstract void mpResponse(String requestURI,String responseString);
	protected abstract boolean doStart();
	protected abstract void doStop();
	
	private MPRequestResponseDispatcher dispatcher;
	
	protected ApplicationProfile appProfile = new ApplicationProfile();
	
	public MPAdapter(ApplicationProfile appProfile){
		this.appProfile = appProfile;
	}
	
	class MPRequestResponseDispatcher extends Aggregator {
		
		public String subscribe() {
			Bindings bindings = new Bindings();
			bindings.addBinding("protocol", new RDFTermURI(protocolURI()));
			return super.subscribe(bindings);	
		}
		
		public MPRequestResponseDispatcher(){
			super(appProfile,"MP_RESPONSE","INSERT_MP_REQUEST");
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for(Bindings binding : bindingsResults.getBindings()) {
				String requestURI = binding.getBindingValue("request");
				String responseString = binding.getBindingValue("value");
				
				Logger.log(VERBOSITY.INFO,adapterName(),"<< MP-Response<"+responseString+">");
				
				mpResponse(requestURI,responseString);
			}
			
		}

		@Override
		public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSubscribe(BindingsResults bindingsResults, String spuid) {
			notifyAdded(bindingsResults,spuid,0);
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
		
		bindings.addBinding("request", new RDFTermURI(request.getURI()));
		bindings.addBinding("value", new RDFTermLiteral(request.getRequestString()));
		bindings.addBinding("protocol", new RDFTermURI(request.getProtocol()));
		
		//SPARQL UPDATE
		Logger.log(VERBOSITY.INFO,adapterName(),">> " + request.toString());
		
		if(!dispatcher.update(bindings)) {
			Logger.log(VERBOSITY.ERROR,adapterName(),"***RDF STORE UPDATE FAILED***");
			return null;
		}
		
		return request.getURI();
	}
}
