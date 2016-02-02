package arces.unibo.iot.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import arces.unibo.iot.network.mappers.INetworkMapper;
import arces.unibo.iot.protocol.mappers.IProtocolMapper;

public abstract class Map {
	HashMap<String,Mappings> maps = new HashMap<String,Mappings>();
	
	public void addMapper(String uri,IMapper mapper){
		maps.put(uri, new Mappings(mapper));	
	}

	public IoTContextAction mpRequest2IoT(MPRequest request) {
		if (!maps.containsKey(request.getProtocol())) return null;
		return maps.get(request.getProtocol()).mpRequestString2IoT(request.getRequestString());
	}

	public String iot2MPResponseString(String protocol, IoTContextAction contextAction) {
		if (!maps.containsKey(protocol)) return null;
		return maps.get(protocol).iot2MPResponseString(contextAction);
	}

	public ArrayList<MNRequest> ioT2MNRequestList(IoTContextAction ioTContextAction) {
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
	
	public IoTContextAction mnResponse2IoT(MNResponse response) {
		if (!maps.containsKey(response.getNetwork())) return null;
		return maps.get(response.getNetwork()).mnResponseString2IoT(response.getResponseString());
	}
	
	public boolean addMapping(String protocol, IoTContextAction contextAction,String requestPattern,String responsePattern) {
		if (!maps.containsKey(protocol)) return false;
		return maps.get(protocol).addMapping(new Mapping(contextAction,requestPattern,responsePattern));
	}

	public boolean removeMapping(String protocol, IoTContextAction contextAction,String requestPattern,String responsePattern) {
		if (!maps.containsKey(protocol)) return false;
		return maps.get(protocol).removeMapping(new Mapping(contextAction,requestPattern,responsePattern));
	}
	
	public class Mapping {
		IoTContextAction contextAction;
		String requestPattern;
		String reponsePattern;
		
		public Mapping(IoTContextAction contextAction,String requestPattern,String responsePattern){
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
		
		public IoTContextAction mpRequestString2IoT(String request){
			if (!(this.mapper instanceof IProtocolMapper)) return null;
			Iterator<Mapping> mappingIt = mappings.iterator();
			while (mappingIt.hasNext()){
				Mapping mapping = mappingIt.next();
				
				IoTContextAction context = ((IProtocolMapper)mapper).mpRequestString2IoT(request, mapping.requestPattern,mapping.contextAction);
				
				if (context == null) continue;
				
				return context;
			}
			return null;
		}
		
		public String iot2MPResponseString(IoTContextAction contextAction) {
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
		
		public  String ioT2MNRequestString(IoTContextAction ioTContextAction) {
			if (!(this.mapper instanceof INetworkMapper)) return null;
			Iterator<Mapping> mappingIt = mappings.iterator();
			while (mappingIt.hasNext()){
				Mapping mapping = mappingIt.next();
				
				if (mapping.contextAction.equals(ioTContextAction)) return ((INetworkMapper)mapper).ioT2MNRequestString(mapping.requestPattern,ioTContextAction);
			}
			return null;
		}
		
		public IoTContextAction mnResponseString2IoT(String responseString) {
			if (!(this.mapper instanceof INetworkMapper)) return null;
			Iterator<Mapping> mappingIt = mappings.iterator();
			while (mappingIt.hasNext()){
				Mapping mapping = mappingIt.next();
				
				IoTContextAction context = ((INetworkMapper)mapper).mnResponseString2IoT(responseString, mapping.reponsePattern,mapping.contextAction);
				
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
