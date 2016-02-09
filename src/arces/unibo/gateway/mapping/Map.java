package arces.unibo.gateway.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import arces.unibo.gateway.mapping.mappers.network.INetworkMapper;
import arces.unibo.gateway.mapping.mappers.protocol.IProtocolMapper;

public abstract class Map {
	protected HashMap<String,Mappings> maps = new HashMap<String,Mappings>();
	
	/*
	public void addMapper(String key,Mapper mapper){
		maps.put(key, new Mappings(mapper));	
	}
*/
	public ContextAction mpRequest2IoT(MPRequest request) {
		if (!maps.containsKey(request.getProtocol())) return null;
		return maps.get(request.getProtocol()).mpRequestString2IoT(request.getRequestString());
	}

	public String iot2MPResponseString(String protocol, ContextAction contextAction) {
		if (!maps.containsKey(protocol)) return null;
		return maps.get(protocol).iot2MPResponseString(contextAction);
	}

	public ArrayList<MNRequest> ioT2MNRequestList(ContextAction ioTContextAction) {
		ArrayList<MNRequest> ret = new ArrayList<MNRequest>();
		Iterator<String> mapsIt = maps.keySet().iterator();
		while (mapsIt.hasNext()){
			String protocol = mapsIt.next();
			Mappings mappings = maps.get(protocol);
			String requestString = mappings.ioT2MNRequestString(ioTContextAction);
			if (requestString == null) continue;
			ret.add(new MNRequest(protocol,requestString));
		}
		return ret;
	}
	
	public ContextAction mnResponse2IoT(MNResponse response) {
		if (!maps.containsKey(response.getNetwork())) return null;
		return maps.get(response.getNetwork()).mnResponseString2IoT(response.getResponseString());
	}
	
	public boolean addMapping(String protocol, ContextAction contextAction,String requestPattern,String responsePattern) {
		if (!maps.containsKey(protocol)) return false;
		return maps.get(protocol).addMapping(new Mapping(contextAction,requestPattern,responsePattern));
	}

	public boolean removeMapping(String protocol, ContextAction contextAction,String requestPattern,String responsePattern) {
		if (!maps.containsKey(protocol)) return false;
		return maps.get(protocol).removeMapping(new Mapping(contextAction,requestPattern,responsePattern));
	}
	
	public class Mapping {
		ContextAction contextAction;
		String requestPattern;
		String reponsePattern;
		
		public Mapping(ContextAction contextAction,String requestPattern,String responsePattern){
			this.contextAction = contextAction;
			this.reponsePattern = responsePattern;
			this.requestPattern = requestPattern;
		}
		
		@Override
		public boolean equals(Object obj){
			if (!obj.getClass().equals(this.getClass())) return false;
			Mapping target = (Mapping) obj;
			return (this.contextAction.equals(target.contextAction));
		}
	}
	
	public class Mappings {
		private ArrayList<Mapping> mappings;
		private IMapper mapper;
		
		public Mappings(IMapper mapper){
			this.mapper = mapper;
			mappings = new ArrayList<Mapping>();
		}
		
		public ContextAction mpRequestString2IoT(String request){
			if (!(this.mapper instanceof IProtocolMapper)) return null;
			Iterator<Mapping> mappingIt = mappings.iterator();
			while (mappingIt.hasNext()){
				Mapping mapping = mappingIt.next();
				
				ContextAction context = ((IProtocolMapper)mapper).mpRequestString2IoT(request, mapping.requestPattern,mapping.contextAction);
				
				if (context == null) continue;
				
				return context;
			}
			return null;
		}
		
		public String iot2MPResponseString(ContextAction contextAction) {
			if (!(this.mapper instanceof IProtocolMapper)) return null;
			Iterator<Mapping> mappingIt = mappings.iterator();
			while (mappingIt.hasNext()){
				Mapping mapping = mappingIt.next();
				
				String response = ((IProtocolMapper)mapper).ioT2MPResponseString(mapping.reponsePattern,contextAction);
				
				if (response == null) continue;
				
				return response;
			}
			return null;
		}
		
		public  String ioT2MNRequestString(ContextAction ioTContextAction) {
			if (!(this.mapper instanceof INetworkMapper)) return null;
			Iterator<Mapping> mappingIt = mappings.iterator();
			while (mappingIt.hasNext()){
				Mapping mapping = mappingIt.next();
				
				if (mapping.contextAction.equals(ioTContextAction)) return ((INetworkMapper)mapper).ioT2MNRequestString(mapping.requestPattern,ioTContextAction);
			}
			return null;
		}
		
		public ContextAction mnResponseString2IoT(String responseString) {
			if (!(this.mapper instanceof INetworkMapper)) return null;
			Iterator<Mapping> mappingIt = mappings.iterator();
			while (mappingIt.hasNext()){
				Mapping mapping = mappingIt.next();
				
				ContextAction context = ((INetworkMapper)mapper).mnResponseString2IoT(responseString, mapping.reponsePattern,mapping.contextAction);
				
				if (context == null) continue;
				
				return context;
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
}
