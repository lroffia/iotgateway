package arces.unibo.gateway.dispatching;

import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import arces.unibo.gateway.mapping.Map;
import arces.unibo.gateway.mapping.Mappings;
import arces.unibo.gateway.mapping.mappers.network.DASH7Mapper;
import arces.unibo.gateway.mapping.mappers.network.INetworkMapper;
import arces.unibo.gateway.mapping.mappers.network.MQTTMapper;
import arces.unibo.gateway.mapping.mappers.network.PingPongMapper;

public class MNMap extends Map {
	private ArrayList<INetworkMapper> mappers = new ArrayList<INetworkMapper>();
	private static final Logger logger = LogManager.getLogger("MNMap");
	
	public MNMap(){
		//TODO: add all supported mappings here
		mappers.add(new PingPongMapper());
		mappers.add(new DASH7Mapper());
		mappers.add(new MQTTMapper());
		
		for(INetworkMapper mapper : mappers) {
			addMapper(mapper.getMapperURI(), mapper);
			logger.info(mapper.getMapperURI()+ "\tmapper added");
		}
	}
	
	public void addMapper(String key,INetworkMapper mapper){
		maps.put(key, new Mappings(mapper));	
	}
}

