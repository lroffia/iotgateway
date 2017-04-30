package arces.unibo.gateway.mapping.manager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.SEPA.client.pattern.ApplicationProfile;										

public class MPMappingManager {
	private static final Logger logger = LogManager.getLogger("MPMappingManager");

	private MPMappingCreator creator;
	private MPMappingRemover remover;
	private MPMappingListener listener;
	private MPMappingUpdater updater;

	public MPMappingManager(ApplicationProfile appProfile,MPMappingEventListener e) {
		creator = new MPMappingCreator(appProfile);
		remover = new MPMappingRemover(appProfile);
		listener = new MPMappingListener(appProfile,e);
		updater = new MPMappingUpdater(appProfile);
	}
	
	public boolean removeAllMapping(){
		return remover.removeAllMapping();
	}
	
	public boolean removeMapping(String mapping){
		return remover.removeMapping(mapping);
	}
	
	public boolean addMapping(String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
		return creator.addMapping(protocol, requestPattern, responsePattern, resource, action, value);
	}
	
	public boolean updateMapping(String mapping, String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
		return updater.updateMapping( mapping,  protocol, requestPattern, responsePattern, resource, action, value);
	}
		
	public boolean start(){			
		if(!creator.join()) return false;
		if(!remover.join()) return false;
		if(!listener.join()) return false;
		if(!updater.join()) return false;
		
		String subID = listener.subscribe(null);
		
		if (subID == null) {
			logger.fatal("Subscription FAILED");
			return false;
		}
		
		logger.debug("Subscription\t"+subID);
		
		logger.info( "Started");
		
		return true;
	}

	public boolean stop() {
		return true;
	}
}

