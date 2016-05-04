package arces.unibo.gateway.mapping;

import java.util.ArrayList;

import arces.unibo.gateway.mapping.mappers.network.INetworkMapper;
import arces.unibo.gateway.mapping.mappers.protocol.IProtocolMapper;

public class Mappings {
	private ArrayList<Mapping> mappings;
	private IMapper mapper;
	
	public Mappings(IMapper mapper){
		this.mapper = mapper;
		mappings = new ArrayList<Mapping>();
	}
	
	public ResourceAction mpRequestString2ResourceAction(String request){
		if (!(this.mapper instanceof IProtocolMapper)) return null;
		for (Mapping mapping : mappings){				
			ResourceAction resourceAction = ((IProtocolMapper)mapper).mpRequestString2ResourceAction(request, mapping.requestPattern,mapping.resourceAction);
			
			if (resourceAction == null) continue;
			
			return resourceAction;
		}
		return null;
	}
	
	public String resourceAction2MPResponseString(ResourceAction resourceAction) {
		if (!(this.mapper instanceof IProtocolMapper)) return null;

		for (Mapping mapping : mappings ){
			String response = ((IProtocolMapper)mapper).resourceAction2MPResponseString(resourceAction,mapping.resourceAction, mapping.responsePattern);
			
			if (response == null) continue;
			
			return response;
		}
		return null;
	}
	
	public  String resourceAction2MNRequestString(ResourceAction resourceAction) {
		if (!(this.mapper instanceof INetworkMapper)) return null;

		for (Mapping mapping : mappings){
			if (mapping.resourceAction.equals(resourceAction)) return ((INetworkMapper)mapper).resourceAction2MNRequestString(mapping.requestPattern,resourceAction);
		}
		return null;
	}
	
	public ResourceAction mnResponseString2ResourceAction(String responseString) {
		if (!(this.mapper instanceof INetworkMapper)) return null;

		for (Mapping mapping : mappings){
			ResourceAction resourceAction = ((INetworkMapper)mapper).mnResponseString2ResourceAction(responseString, mapping.responsePattern,mapping.resourceAction);
			
			if (resourceAction == null) continue;
			
			return resourceAction;
		}
		return null;
	}
	
	public boolean addMapping(Mapping mapping){
		if (mappings.contains(mapping)) return false;
		return mappings.add(mapping);
	}
	
	public boolean removeMapping(Mapping mapping){
		return mappings.remove(mapping);
	}
}

