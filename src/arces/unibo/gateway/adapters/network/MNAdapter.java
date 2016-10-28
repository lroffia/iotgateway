package arces.unibo.gateway.adapters.network;

import java.util.ArrayList;
import java.util.UUID;

import arces.unibo.SEPA.Aggregator;
import arces.unibo.SEPA.BindingLiteralValue;
import arces.unibo.SEPA.BindingURIValue;
import arces.unibo.SEPA.Bindings;
import arces.unibo.SEPA.BindingsResults;
import arces.unibo.SEPA.Logger;
import arces.unibo.SEPA.Logger.VERBOSITY;

public abstract class MNAdapter {
	private MNRequestResponseDispatcher dispatcher;
	
	//Abstract methods
	public abstract String networkURI();
	protected abstract void mnRequest(String request);
	protected abstract boolean doStart();
	protected abstract void doStop();
	public abstract String adapterName();
	
	public MNAdapter() {
		dispatcher = new MNRequestResponseDispatcher();	
	}
	
	class MNRequestResponseDispatcher extends Aggregator {
		
		public MNRequestResponseDispatcher(){
			super("MN_REQUEST","INSERT_MN_RESPONSE");
		}
		
		public String  subscribe(){
			//SPARQL SUBSCRIBE
			Bindings bindings = new Bindings();
			bindings.addBinding("?network", new BindingURIValue(networkURI()));
			
			return subscribe(bindings);
		}
		
		@Override
		public void notify(BindingsResults notify) {

		}

		@Override
		public void notifyAdded(ArrayList<Bindings> bindings) {
			for (Bindings binding : bindings){
				mnRequest(binding.getBindingValue("?value").getValue());
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
	
	protected final boolean mnResponse(String value) {
		String response = "iot:MN-Response_"+UUID.randomUUID().toString();
		
		Bindings bindings = new Bindings();
		bindings.addBinding("?value", new BindingLiteralValue(value));
		bindings.addBinding("?response", new BindingURIValue(response));
		bindings.addBinding("?network", new BindingURIValue(networkURI()));
		
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
