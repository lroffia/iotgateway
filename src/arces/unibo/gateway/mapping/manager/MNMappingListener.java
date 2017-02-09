package arces.unibo.gateway.mapping.manager;

import java.util.ArrayList;

import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Consumer;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.Bindings;
import arces.unibo.SEPA.commons.BindingsResults;
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
}

