package arces.unibo.gateway.adapters.protocol;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.Aggregator;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;
import arces.unibo.gateway.mapping.MPRequest;

public abstract class MPAdapter {
	public abstract String adapterName();
	public abstract String protocolURI();
	protected abstract void mpResponse(String requestURI,String responseString);
	protected abstract boolean doStart();
	protected abstract void doStop();
	
	private static final Logger logger = LogManager.getLogger("MPAdapter");
	
	private MPRequestResponseDispatcher dispatcher;
	
	protected ApplicationProfile appProfile = new ApplicationProfile();
	
	public MPAdapter(ApplicationProfile appProfile){
		this.appProfile = appProfile;
	}
	
	class MPRequestResponseDispatcher extends Aggregator {
		private final Logger logger = LogManager.getLogger("MPRequestResponseDispatcher");
		
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
				
				logger.info("<< MP-Response<"+responseString+">");
				
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

		@Override
		public void brokenSubscription() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public boolean start(){
		dispatcher = new MPRequestResponseDispatcher();
		if (!dispatcher.join()) return false;
		
		String subID = dispatcher.subscribe();
		
		if (subID == null) {
			logger.fatal("Subscription FAILED");
			return false;
		}
		
		logger.debug("Subscription "+subID);
		
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
		logger.info(">> " + request.toString());
		
		if(!dispatcher.update(bindings)) {
			logger.error("***RDF STORE UPDATE FAILED***");
			return null;
		}
		
		return request.getURI();
	}
}
