package arces.unibo.gateway;

import java.util.ArrayList;

import arces.unibo.gateway.mapping.Map;
import arces.unibo.gateway.mapping.mappers.network.INetworkMapper;
import arces.unibo.gateway.mapping.mappers.network.PingPongMapper;

public class MNMap extends Map {
	private ArrayList<INetworkMapper> mappers = new ArrayList<INetworkMapper>();
	
	public MNMap(){
		System.out.println("Network mappings:");
		//TODO: add all supported mappings here
		mappers.add(new PingPongMapper());
		
		for(INetworkMapper mapper : mappers) {
			addMapper(mapper.getMapperURI(), mapper);
			System.out.println("- "+mapper.getMapperURI());
		}
		
	}
	
	public void addMapper(String key,INetworkMapper mapper){
		maps.put(key, new Mappings(mapper));	
	}
}
