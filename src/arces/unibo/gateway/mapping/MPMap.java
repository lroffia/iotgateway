package arces.unibo.gateway.mapping;

import arces.unibo.gateway.mapping.mappers.protocol.HTTPMapper;

public class MPMap extends Map {
	public MPMap(){
		//TODO: add all supported mappings here
		System.out.println("Protocol mappings:");
		
		System.out.println("- HTTP");
		this.addMapper("iot:HTTP", new HTTPMapper());
	}
}
