package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.application.Aggregator;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;
import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;

public 	class MPResponseRemover extends Aggregator {
	boolean monitor;
	GarbageCollectorListener listener;
	String tag = "MPResponseRemover";
	
	public MPResponseRemover(ApplicationProfile appProfile,boolean monitor,GarbageCollectorListener listener) {
		super(appProfile,"MP_RESPONSE","DELETE_REQUEST_RESPONSE");
		this.monitor = monitor;
		this.listener = listener;
	}
		
	public String subscribe() {return super.subscribe(null);}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence)  {
		if (monitor) return;
		
		SEPALogger.log(VERBOSITY.DEBUG, tag, "DELETE MP RESPONSE "+bindingsResults.toString());
		
		for (Bindings garbage : bindingsResults.getBindings()) update(garbage);
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (!monitor || listener == null) return;
		
		for (Bindings garbage : bindingsResults.getBindings())	
			listener.removedMPResponse(
					garbage.getBindingValue("protocol"), 
					garbage.getBindingValue("value"));	
	}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void brokenSubscription() {
		// TODO Auto-generated method stub
		
	}

}
