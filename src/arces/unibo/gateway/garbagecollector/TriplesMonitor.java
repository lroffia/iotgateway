package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Consumer;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.Logger.VERBOSITY;
import arces.unibo.SEPA.commons.ARBindingsResults;
import arces.unibo.SEPA.commons.BindingsResults;

public class TriplesMonitor extends Consumer {
	long triplesNumber;
	GarbageCollectorListener listener;
	String tag = "TriplesMonitor";
	
	public TriplesMonitor(ApplicationProfile appProfile,GarbageCollectorListener listener) {
		super(appProfile,"ALL");
		triplesNumber = 0;
		this.listener = listener;
	}

	public String subscribe() {return subscribe(null);}

	@Override
	public void notify(ARBindingsResults notify, String spuid, Integer sequence) {

	}

	@Override
	public void notifyAdded(BindingsResults bindingsResults, String spuid, Integer sequence) {
		triplesNumber += bindingsResults.size();
		if (listener != null) listener.totalTriples(triplesNumber);
		Logger.log(VERBOSITY.DEBUG, tag, "Total triples: "+triplesNumber);	
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		triplesNumber -= bindingsResults.size();
		if (listener != null) listener.totalTriples(triplesNumber);
		Logger.log(VERBOSITY.DEBUG, tag, "Total triples: "+triplesNumber);
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
		triplesNumber = bindingsResults.size();
		if (listener != null) listener.totalTriples(triplesNumber);
		Logger.log(VERBOSITY.DEBUG, tag, "Initial triples: "+triplesNumber);
	}
}