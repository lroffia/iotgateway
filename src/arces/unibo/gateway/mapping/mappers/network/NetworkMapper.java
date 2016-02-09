package arces.unibo.gateway.mapping.mappers.network;

import arces.unibo.gateway.mapping.ContextAction;

public abstract class NetworkMapper implements INetworkMapper{

	public String ioT2MNRequestString(String pattern, ContextAction contextAction) {
		return pattern.replace("*", contextAction.getValue());
	}
	
	public ContextAction mnResponseString2IoT(String response, String pattern, ContextAction contextPattern) {
		String[] patternValues = pattern.split("&");
		String[] responseValues = response.split("&");
		String value = null;
		
		for(int i=0; i < patternValues.length; i++){
			if (i == responseValues.length) return null;
			if (patternValues[i].equals("*")) {
				value = responseValues[i];
				continue;
			}
			if (!patternValues[i].equals(responseValues[i])) return null;
		}
		
		return new ContextAction(contextPattern.getContextURI(),contextPattern.getActionURI(),value);
	}
}
