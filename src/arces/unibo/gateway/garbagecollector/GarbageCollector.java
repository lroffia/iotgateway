package arces.unibo.gateway.garbagecollector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;

public class GarbageCollector {
	private static final Logger logger = LogManager.getLogger("GarbageCollector");

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

		logger.info("Started");
		return true;
	}
	
	public boolean stop(){
		return true;
	}
}