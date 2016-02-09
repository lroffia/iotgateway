package arces.unibo.gateway;

public class MappingManager {
		
	private MPMappingManager mpMappingManager;
	private MNMappingManager mnMappingManager;
	
	public MappingManager() {
		mpMappingManager = new MPMappingManager();
		mnMappingManager = new MNMappingManager();	
	}
	
	public boolean start(){
		System.out.println("*******************");
		System.out.println("* Mapping Manager *");
		System.out.println("*******************");
		
		if(!mnMappingManager.start()) return false;
		if(!mpMappingManager.start()) return false;
			
		System.out.println("Mapping Manager started");
		return true;
	}
	
	public void stop(){
		mnMappingManager.stop();
		mpMappingManager.stop();
	}
	
	public boolean addProtocolMapping(String protocol,String requestStringPattern,String responseStringPattern,String context,String action,String value){
		return mpMappingManager.addMapping(protocol, requestStringPattern, responseStringPattern, context, action, value);
	}

	public boolean addNetworkMapping(String network,String requestStringPattern,String responseStringPattern,String context,String action,String value){
		return mnMappingManager.addMapping(network, requestStringPattern, responseStringPattern, context, action, value);
	}
	
	public boolean removeProtocolMapping(String protocol,String requestStringPattern,String responseStringPattern,String context,String action,String value){
		return mpMappingManager.removeMapping(protocol, requestStringPattern, responseStringPattern, context, action, value);
	}
	
	public boolean removeNetworkMapping(String network,String requestStringPattern,String responseStringPattern,String context,String action,String value){
		return mnMappingManager.removeMapping(network, requestStringPattern, responseStringPattern, context, action, value);
	}
	
	public boolean removeAllMapping(){
		if(!mnMappingManager.removeAllMapping()) return false;	
		if(!mpMappingManager.removeAllMapping()) return false;
		return true;
	}
}
