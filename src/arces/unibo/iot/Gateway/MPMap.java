package arces.unibo.iot.Gateway;

import arces.unibo.iot.mapping.Map;

import arces.unibo.iot.protocol.mappers.HTTPMapper;

public class MPMap extends Map {
	public MPMap(){
		//TODO: add all supported mappings here
		System.out.println("Protocol mappings:");
		
		System.out.println("- HTTP");
		this.addMapper("iot:HTTP", new HTTPMapper());
	}
}
