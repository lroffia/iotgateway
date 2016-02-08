package arces.unibo.gateway.mapping.mappers.network;

import arces.unibo.gateway.mapping.ContextAction;

public class PingPongMapper implements INetworkMapper {
	
	public String ioT2MNRequestString(String pattern, ContextAction contextAction) {
		return pattern;
	}
	
	public ContextAction mnResponseString2IoT(String response, String pattern, ContextAction contextPattern) {		
		if (response.equals(pattern)) return new ContextAction(contextPattern.getContextURI(),contextPattern.getActionURI(),response);
		return null;
	}
}
