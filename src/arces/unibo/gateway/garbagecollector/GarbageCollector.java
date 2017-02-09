package arces.unibo.gateway.garbagecollector;

import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;

public class GarbageCollector {
	static String tag = "GARBAGE COLLECTOR";

	private ApplicationProfile appProfile;
	private GarbageCollectorListener listener;
	
	private ResourcePendingRequestRemover resourcePendingRequest;
	private MPResponseRemover mpResponse;
	private ResourceResponseRemover resourceResponse;
	private ResourceRequestRemover resourceRequest;
	private MNResponseRemover mnResponse;
	private MNRequestRemover mnRequest;
	private Eraser eraser;
	private TriplesMonitor triplesMonitor;
	private MPRequestMonitor mpRequestMonitor;
		
	public GarbageCollector(ApplicationProfile appProfile,GarbageCollectorListener listener) {
		this.listener = listener;
		this.appProfile = appProfile;
	}
	public boolean start(boolean erase,boolean monitor) {	
		if (erase) {
			eraser = new Eraser(appProfile);
			if (!eraser.join()) return false;
			if (erase) if (!eraser.update()) return false;
		}
		
		if (monitor) {
			triplesMonitor = new TriplesMonitor(appProfile,listener);
			if (!triplesMonitor.join()) return false;	
			if(triplesMonitor.subscribe() == null) return false;
		}
			
		resourcePendingRequest = new ResourcePendingRequestRemover(appProfile,monitor,listener);
		if (!resourcePendingRequest.join()) return false;
		if(resourcePendingRequest.subscribe() == null) return false;
		
		mpResponse = new MPResponseRemover(appProfile,monitor,listener);
		if (!mpResponse.join()) return false;
		if(mpResponse.subscribe() == null) return false;
		
		resourceResponse = new ResourceResponseRemover(appProfile,monitor,listener);
		if (!resourceResponse.join()) return false;
		if(resourceResponse.subscribe() == null) return false;
		
		resourceRequest = new ResourceRequestRemover(appProfile,monitor,listener);
		if (!resourceRequest.join()) return false;
		if(resourceRequest.subscribe() == null) return false;
		
		mnRequest = new MNRequestRemover(appProfile,monitor,listener);
		if (!mnRequest.join()) return false;
		if(mnRequest.subscribe() == null) return false;
		
		mnResponse = new MNResponseRemover(appProfile,monitor,listener);
		if (!mnResponse.join()) return false;
		if(mnResponse.subscribe() == null) return false;
		
		mpRequestMonitor = new MPRequestMonitor(appProfile,monitor,listener);
		if (!mpRequestMonitor.join()) return false;
		if(mpRequestMonitor.subscribe() == null) return false;

		Logger.log(VERBOSITY.INFO, tag,"Started");
		return true;
	}
	
	public boolean stop(){
		return true;
	}
}