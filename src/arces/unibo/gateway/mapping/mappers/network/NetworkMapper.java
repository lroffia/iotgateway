package arces.unibo.gateway.mapping.mappers.network;

import arces.unibo.gateway.mapping.ResourceAction;

public abstract class NetworkMapper implements INetworkMapper{

	public String resourceAction2MNRequestString(String pattern, ResourceAction resourceAction) {
		return pattern.replace("*", resourceAction.getValue());
	}
	
	public ResourceAction mnResponseString2ResourceAction(String response, String pattern, ResourceAction resourceActionPattern) {
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
		
		return new ResourceAction(resourceActionPattern.getResourceURI(),resourceActionPattern.getActionURI(),resourceActionPattern.getValue().replace("*", value));
	}
}
