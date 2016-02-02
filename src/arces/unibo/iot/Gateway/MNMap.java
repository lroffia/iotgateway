package arces.unibo.iot.Gateway;

import arces.unibo.iot.mapping.Map;

import arces.unibo.iot.network.mappers.PingPongMapper;

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
