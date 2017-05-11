package arces.unibo.gateway.mapping.manager;

import java.util.ArrayList;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;
import arces.unibo.SEPA.client.pattern.Consumer;
import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.Bindings;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;
import arces.unibo.SEPA.commons.response.ErrorResponse;
import arces.unibo.gateway.mapping.MNMapping;
import arces.unibo.gateway.mapping.ResourceAction;

public class MNMappingListener extends Consumer {
	MNMappingEventListener event;
	
	public MNMappingListener(ApplicationProfile appProfile,MNMappingEventListener event) {
		super(appProfile,"MN_MAPPING");
		this.event = event;
	}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		ArrayList<MNMapping> mappings = new ArrayList<MNMapping>();
		for (Bindings binding : bindingsResults.getBindings()) {
			mappings.add(new MNMapping(
					binding.getBindingValue("mapping"),
					binding.getBindingValue("network"),
					binding.getBindingValue("requestPattern"),
					binding.getBindingValue("responsePattern"),
					new ResourceAction(
							binding.getBindingValue("resource"),
							binding.getBindingValue("action"),
							binding.getBindingValue("value"))));
		}
		if(event != null) event.addedMNMappings(mappings);
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		ArrayList<MNMapping> mappings = new ArrayList<MNMapping>();
		for (Bindings binding : bindingsResults.getBindings()) {
			mappings.add(new MNMapping(
					binding.getBindingValue("mapping"),
					binding.getBindingValue("network"),
					binding.getBindingValue("requestPattern"),
					binding.getBindingValue("responsePattern"),
					new ResourceAction(
							binding.getBindingValue("resource"),
							binding.getBindingValue("action"),
							binding.getBindingValue("value"))));
		}
		if(event != null) event.removedMNMappings(mappings);	
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

