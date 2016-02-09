package arces.unibo.gateway;

import java.util.ArrayList;

import arces.unibo.gateway.mapping.Map;
import arces.unibo.gateway.mapping.mappers.protocol.HTTPMapper;
import arces.unibo.gateway.mapping.mappers.protocol.IProtocolMapper;

public class MPMap extends Map {
	private ArrayList<IProtocolMapper> mappers = new ArrayList<IProtocolMapper>();
	
	public MPMap(){
		System.out.println("Protocol mappings:");
		//TODO: add all supported mappings here
		mappers.add(new HTTPMapper());
		
		for(IProtocolMapper mapper : mappers) {
			addMapper(mapper.getMapperURI(), mapper);
			System.out.println("- "+mapper.getMapperURI());
		}
		
	}
	
	public void addMapper(String key,IProtocolMapper mapper){
		maps.put(key, new Mappings(mapper));	
	}
}
