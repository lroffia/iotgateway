package arces.unibo.gateway.mapping;

import arces.unibo.gateway.mapping.mappers.network.PingPongMapper;

public class MNMap extends Map {
	public MNMap(){
		//TODO: add all supported mappings here
		System.out.println("Protocol mappings:");
		
		//System.out.println("- DASH7");
		//this.addMapper("iot:DASH7", new DASH7Mapper());
		
		System.out.println("- PINGPONG");
		this.addMapper("iot:PINGPONG", new PingPongMapper());
		
	}
}
