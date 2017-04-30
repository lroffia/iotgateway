package arces.unibo.gateway.mapping.manager;

import java.util.ArrayList;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Consumer;
import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.gateway.mapping.MPMapping;
import arces.unibo.gateway.mapping.ResourceAction;

public class MPMappingListener extends Consumer {			
	MPMappingEventListener event;
	
	public MPMappingListener(ApplicationProfile appProfile,MPMappingEventListener event) {
		super(appProfile,"MP_MAPPING");
		this.event = event;
	}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		ArrayList<MPMapping> mappings = new ArrayList<MPMapping>();
		for (Bindings binding : bindingsResults.getBindings()) {
			mappings.add(new MPMapping(
					binding.getBindingValue("mapping"),
					binding.getBindingValue("protocol"),
					binding.getBindingValue("requestPattern"),
					binding.getBindingValue("responsePattern"),
					new ResourceAction(
							binding.getBindingValue("resource"),
							binding.getBindingValue("action"),
							binding.getBindingValue("value"))));
		}
		if(event != null) event.addedMPMappings(mappings);
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		ArrayList<MPMapping> mappings = new ArrayList<MPMapping>();
		for (Bindings binding : bindingsResults.getBindings()) {
			mappings.add(new MPMapping(
					binding.getBindingValue("mapping"),
					binding.getBindingValue("protocol"),
					binding.getBindingValue("requestPattern"),
					binding.getBindingValue("responsePattern"),
					new ResourceAction(
							binding.getBindingValue("resource"),
							binding.getBindingValue("action"),
							binding.getBindingValue("value"))));
		}
		if(event != null) event.removedMPMappings(mappings);	
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
