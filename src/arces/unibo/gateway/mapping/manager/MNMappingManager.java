package arces.unibo.gateway.mapping.manager;

import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.Logger.VERBOSITY;

public class MNMappingManager {
	static final String tag = "MN MAPPING";
	
	private MNMappingCreator creator;
	private MNMappingRemover remover;
	private MNMappingListener listener;
	private MNMappingUpdater updater;
		
	public MNMappingManager(ApplicationProfile appProfile,MNMappingEventListener e) {
		creator = new MNMappingCreator(appProfile);
		remover = new MNMappingRemover(appProfile);
		listener = new MNMappingListener(appProfile,e);
		updater = new MNMappingUpdater(appProfile);
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
	
	public boolean updateMapping(String mapping,String protocol,String requestPattern,String responsePattern,String resource,String action,String value){
		return updater.updateMapping( mapping, protocol, requestPattern, responsePattern, resource, action, value);
	}
	
	public boolean start(){
		if(!creator.join()) return false;
		if(!remover.join()) return false;
		if(!listener.join()) return false;
		if(!updater.join()) return false;
		
		String subID = listener.subscribe(null);
		
		if (subID == null) {
			Logger.log(VERBOSITY.FATAL, tag, "Subscription FAILED");
			return false;
		}
		
		Logger.log(VERBOSITY.DEBUG, tag,"Subscription\t"+subID); 
		
		Logger.log(VERBOSITY.INFO, tag, "Started");
		
		return true;
	}

	public boolean stop() {
		boolean ret = creator.leave();
		ret = ret && remover.leave();
		ret = ret && listener.unsubscribe();
		ret = ret && listener.leave();
		return ret;
	}
}
