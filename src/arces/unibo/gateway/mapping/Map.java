package arces.unibo.gateway.mapping;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Map {
	protected HashMap<String,Mappings> maps = new HashMap<String,Mappings>();
	
	public ResourceAction mpRequest2ResourceAction(MPRequest request) {
		if (!maps.containsKey(request.getProtocol())) return null;
		return maps.get(request.getProtocol()).mpRequestString2ResourceAction(request.getRequestString());
	}

	public String resourceAction2MPResponseString(String protocol, ResourceAction resourceAction) {
		if (!maps.containsKey(protocol)) return null;
		return maps.get(protocol).resourceAction2MPResponseString(resourceAction);
	}

	public ArrayList<MNRequest> resourceAction2MNRequestList(ResourceAction resourceAction) {
		ArrayList<MNRequest> ret = new ArrayList<MNRequest>();
		for (String protocol : maps.keySet()){
			Mappings mappings = maps.get(protocol);
			String requestString = mappings.resourceAction2MNRequestString(resourceAction);
			if (requestString == null) continue;
			ret.add(new MNRequest(protocol,requestString));
		}
		return ret;
	}
	
	public ResourceAction mnResponse2ResourceAction(MNResponse response) {
		if (!maps.containsKey(response.getNetwork())) return null;
		return maps.get(response.getNetwork()).mnResponseString2ResourceAction(response.getResponseString());
	}
	
	public boolean addMapping(Mapping mapping) {
		String key = "";
		if (mapping instanceof MNMapping) key = ((MNMapping) mapping).getNetworkURI();
		if (mapping instanceof MPMapping) key = ((MPMapping) mapping).getProtocolURI();
		if (!maps.containsKey(key)) return false;
		
		return maps.get(key).addMapping(mapping);
	}

	public boolean removeMapping(Mapping mapping) {
		String key = "";
		if (mapping instanceof MNMapping) key = ((MNMapping) mapping).getNetworkURI();
		if (mapping instanceof MPMapping) key = ((MPMapping) mapping).getProtocolURI();
		
		if (!maps.containsKey(key)) return false;
		return maps.get(key).removeMapping(mapping);
	}
}
