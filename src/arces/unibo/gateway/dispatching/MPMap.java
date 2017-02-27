package arces.unibo.gateway.dispatching;

import java.util.ArrayList;

import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;
import arces.unibo.gateway.mapping.Map;
import arces.unibo.gateway.mapping.Mappings;
import arces.unibo.gateway.mapping.mappers.protocol.COAPMapper;
import arces.unibo.gateway.mapping.mappers.protocol.HTTPMapper;
import arces.unibo.gateway.mapping.mappers.protocol.IProtocolMapper;

public class MPMap extends Map {
	private ArrayList<IProtocolMapper> mappers = new ArrayList<IProtocolMapper>();
	
	public MPMap(){
		//TODO: add all supported mappings here
		mappers.add(new HTTPMapper());
		mappers.add(new COAPMapper());
		
		for(IProtocolMapper mapper : mappers) {
			addMapper(mapper.getMapperURI(), mapper);
			SEPALogger.log(VERBOSITY.INFO, "MP MAP",mapper.getMapperURI() + "\tmapper added");
		}
	}
	
	public void addMapper(String key,IProtocolMapper mapper){
		maps.put(key, new Mappings(mapper));	
	}
}

