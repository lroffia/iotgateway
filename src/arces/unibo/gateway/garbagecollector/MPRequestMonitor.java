package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Consumer;
import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.response.ErrorResponse;

public class MPRequestMonitor extends Consumer {
	boolean monitor;
	GarbageCollectorListener listener;
	
	public MPRequestMonitor(ApplicationProfile appProfile,boolean monitor,GarbageCollectorListener listener) {
		super(appProfile,"MP_REQUEST");
		this.monitor = monitor;
		this.listener = listener;
	}

	public String subscribe() {return subscribe(null);}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if(!monitor || listener == null) return;
		
		for (Bindings garbage : bindingsResults.getBindings())
			listener.newMPRequest(
					garbage.getBindingValue("protocol"),
					garbage.getBindingValue("value"));	
		
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		if(!monitor || listener == null) return;
		
		for (Bindings garbage : bindingsResults.getBindings())
			listener.removedMPRequest(
					garbage.getBindingValue("protocol"),
					garbage.getBindingValue("value"));		
		
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
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
