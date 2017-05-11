package arces.unibo.gateway.adapters.network;

import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.Aggregator;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.SPARQL.RDFTermLiteral;
import arces.unibo.SEPA.commons.SPARQL.RDFTermURI;
import arces.unibo.SEPA.commons.response.ErrorResponse;

public abstract class MNAdapter {
	private static final Logger logger = LogManager.getLogger("MNAdapter");
	
	private MNRequestResponseDispatcher dispatcher;
	
	//Abstract methods
	public abstract String networkURI();
	protected abstract void mnRequest(String request);
	protected abstract boolean doStart();
	protected abstract void doStop();
	public abstract String adapterName();
	
	
	public MNAdapter(ApplicationProfile appProfile) {
		dispatcher = new MNRequestResponseDispatcher(appProfile);	
	}
	
	class MNRequestResponseDispatcher extends Aggregator {
		
		public MNRequestResponseDispatcher(ApplicationProfile appProfile){
			super(appProfile,"MN_REQUEST","INSERT_MN_RESPONSE");
		}
		
		public String  subscribe(){
			//SPARQL SUBSCRIBE
			Bindings bindings = new Bindings();
			bindings.addBinding("network", new RDFTermURI(networkURI()));
			
			return subscribe(bindings);
		}

		@Override
		public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
			for (Bindings binding : bindingsResults.getBindings()){
				mnRequest(binding.getBindingValue("value"));
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

		@Override
		public void onError(ErrorResponse errorResponse) {
			// TODO Auto-generated method stub
			
		}
	}
	
	protected final boolean mnResponse(String value) {
		String response = "iot:MN-Response_"+UUID.randomUUID().toString();
		
		Bindings bindings = new Bindings();
		bindings.addBinding("value", new RDFTermLiteral(value));
		bindings.addBinding("response", new RDFTermURI(response));
		bindings.addBinding("network", new RDFTermURI(networkURI()));
		
		//SPARQL UPDATE
		return dispatcher.update(bindings);
	}
	
	public boolean start(){
		if (!dispatcher.join()) {
			logger.fatal("Join FAILED");
			return false;
		}
		
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
}
