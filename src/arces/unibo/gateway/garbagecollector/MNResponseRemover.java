package arces.unibo.gateway.garbagecollector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.Aggregator;
import arces.unibo.SEPA.client.pattern.ApplicationProfile;

import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;

public class MNResponseRemover extends Aggregator {		
	boolean monitor;
	GarbageCollectorListener listener;
	private static final Logger logger = LogManager.getLogger("MNResponseRemover");
	
	public MNResponseRemover(ApplicationProfile appProfile,boolean monitor,GarbageCollectorListener listener) {
		super(appProfile,"MN_RESPONSE", "DELETE_MN_RESPONSE");
		this.monitor = monitor;
		this.listener = listener;
	}
	
	public String subscribe() {
		return super.subscribe(null);
	}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (monitor) return;
		
		logger.debug( "DELETE MN RESPONSE "+bindingsResults.toString());
		for (Bindings garbage : bindingsResults.getBindings()) update(garbage);	
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if (!monitor || listener == null) return;
		
		for (Bindings garbage : bindingsResults.getBindings())
			listener.removedMNResponse(
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
