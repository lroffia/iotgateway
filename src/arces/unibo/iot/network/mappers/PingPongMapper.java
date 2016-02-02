package arces.unibo.iot.network.mappers;

import arces.unibo.iot.mapping.IoTContextAction;

public class PingPongMapper implements INetworkMapper {
	
	public String ioT2MNRequestString(String pattern, IoTContextAction contextAction) {
		return pattern;
	}
	
	public IoTContextAction mnResponseString2IoT(String response, String pattern, IoTContextAction contextPattern) {		
		if (response.equals(pattern)) return new IoTContextAction(contextPattern.getContextURI(),contextPattern.getActionURI(),response);
		return null;
	}
}
