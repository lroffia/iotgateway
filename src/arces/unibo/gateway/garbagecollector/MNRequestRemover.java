package arces.unibo.gateway.garbagecollector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.Aggregator;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;

public class MNRequestRemover extends Aggregator {		
	boolean monitor;
	GarbageCollectorListener listener;
	private static final Logger logger = LogManager.getLogger("MNRequestRemover");
	
	public MNRequestRemover(ApplicationProfile appProfile,boolean monitor,GarbageCollectorListener listener) {
		super(appProfile,"MN_REQUEST", "DELETE_MN_REQUEST");
		this.monitor = monitor;
		this.listener = listener;
	}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence)  {}
	
	public String subscribe() {
		return super.subscribe(null);
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (monitor) return;
		
		logger.debug("DELETE MN REQUEST "+bindingsResults.toString());
		for (Bindings garbage : bindingsResults.getBindings()) update(garbage);
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (!monitor || listener == null) return;
			
		for (Bindings garbage : bindingsResults.getBindings())
			listener.removedMNRequest(
					garbage.getBindingValue("network"), 
					garbage.getBindingValue("value"));	
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