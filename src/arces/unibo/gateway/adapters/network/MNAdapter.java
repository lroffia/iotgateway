package arces.unibo.gateway.adapters.network;

import java.util.UUID;

import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.BindingsResults;
import arces.unibo.SEPA.commons.RDFTermLiteral;
import arces.unibo.SEPA.commons.RDFTermURI;

public abstract class MNAdapter {
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
			Logger.log(VERBOSITY.FATAL,adapterName(),"Join FAILED");
			return false;
		}
		
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
}
