package arces.unibo.gateway.garbagecollector;

public interface GarbageCollectorListener {
	public void newMPRequest(String protocol, String value);
	
	public void removedResourcePendingRequest(String resource,String action, String value);
	public void removedResourceRequest(String resource,String action, String value);
	public void removedResourceResponse(String resource,String action, String value);
	public void removedMPResponse(String protocol, String value);
	public void removedMNResponse(String network, String value);
	public void removedMNRequest(String network, String value);
	public void removedMPRequest(String protocol, String value);
	
	public void totalTriples(long triples);
}
