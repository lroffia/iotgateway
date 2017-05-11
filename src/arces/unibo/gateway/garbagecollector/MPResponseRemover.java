package arces.unibo.gateway.garbagecollector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.Aggregator;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.response.ErrorResponse;

public 	class MPResponseRemover extends Aggregator {
	boolean monitor;
	GarbageCollectorListener listener;
	private static final Logger logger = LogManager.getLogger("MPResponseRemover");
	
	public MPResponseRemover(ApplicationProfile appProfile,boolean monitor,GarbageCollectorListener listener) {
		super(appProfile,"MP_RESPONSE","DELETE_REQUEST_RESPONSE");
		this.monitor = monitor;
		this.listener = listener;
	}
		
	public String subscribe() {return super.subscribe(null);}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence)  {
		if (monitor) return;
		
		logger.debug("DELETE MP RESPONSE "+bindingsResults.toString());
		
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

	@Override
	public void onError(ErrorResponse errorResponse) {
		// TODO Auto-generated method stub
		
	}

}
