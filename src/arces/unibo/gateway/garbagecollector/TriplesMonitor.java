package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Consumer;
import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;
import arces.unibo.SEPA.commons.SPARQL.ARBindingsResults;
import arces.unibo.SEPA.commons.SPARQL.BindingsResults;

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
		SEPALogger.log(VERBOSITY.DEBUG, tag, "Total triples: "+triplesNumber);	
	}

	@Override
	public void notifyRemoved(BindingsResults bindingsResults, String spuid, Integer sequence) {
		triplesNumber -= bindingsResults.size();
		if (listener != null) listener.totalTriples(triplesNumber);
		SEPALogger.log(VERBOSITY.DEBUG, tag, "Total triples: "+triplesNumber);
	}

	@Override
	public void onSubscribe(BindingsResults bindingsResults, String spuid) {
		triplesNumber = bindingsResults.size();
		if (listener != null) listener.totalTriples(triplesNumber);
		SEPALogger.log(VERBOSITY.DEBUG, tag, "Initial triples: "+triplesNumber);
	}

	@Override
	public void brokenSubscription() {
		// TODO Auto-generated method stub
		
	}
}