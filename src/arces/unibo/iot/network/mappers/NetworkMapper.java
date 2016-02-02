package arces.unibo.iot.network.mappers;

import arces.unibo.iot.mapping.IoTContextAction;

public abstract class NetworkMapper implements INetworkMapper{

	public String ioT2MNRequestString(String pattern, IoTContextAction contextAction) {
		return pattern.replace("<*>", contextAction.getValue());
	}
	
	public IoTContextAction mnResponseString2IoT(String response, String pattern, IoTContextAction contextPattern) {
		String[] patternValues = pattern.split("&");
		String[] responseValues = response.split("&");
		String value = null;
		
		for(int i=0; i < patternValues.length; i++){
			if (i == responseValues.length) return null;
			if (patternValues[i].equals("<*>")) {
				value = responseValues[i];
				continue;
			}
			if (!patternValues[i].equals(responseValues[i])) return null;
		}
		
		return new IoTContextAction(contextPattern.getContextURI(),contextPattern.getActionURI(),value);
	}
}
