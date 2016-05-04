package arces.unibo.gateway.mapping.mappers.protocol;

import java.util.HashMap;
import java.util.Iterator;

import arces.unibo.gateway.mapping.ResourceAction;

public abstract class ProtocolMapper implements IProtocolMapper {
	
	@Override
	public String resourceAction2MPResponseString(ResourceAction resourceAction, ResourceAction resourceActionPattern,
			String responsePattern) {
		if (resourceAction.equals(resourceActionPattern)) return responsePattern.replace("*", resourceAction.getValue());
		return null;
	}
	
	public ResourceAction mpRequestString2ResourceAction(String request, String pattern,ResourceAction resourceActionPattern) {
		
		HashMap<String,String> patternValues = new HashMap<String,String>();
		HashMap<String,String> requestValues = new HashMap<String,String>();
		
		String[] requestTokens = request.split("&");
		String[] patternTokens = pattern.split("&");
		
		for(int i=0; i < patternTokens.length;i++) patternValues.put(patternTokens[i].split("=")[0],patternTokens[i].split("=")[1]);
		for(int i=0; i < requestTokens.length;i++) requestValues.put(requestTokens[i].split("=")[0], requestTokens[i].split("=")[1]);
		
		//Matching
		Iterator<String> keys = requestValues.keySet().iterator();
		String valueKey = null;
		int matching = 0;
		while(keys.hasNext()) {
			String key = keys.next();
			if (!patternValues.containsKey(key)) continue;
			if (patternValues.get(key).equals("*")) {
				valueKey = key;
				matching++;
			}
			else if (patternValues.get(key).equals(requestValues.get(key))) matching++;
		}
		
		if(matching != requestValues.keySet().size()) return null;
		
		//Retrieve value
		String value = resourceActionPattern.getValue();
		if (resourceActionPattern.getActionURI().equals("iot:SET")) 
			if (valueKey != null && value.equals("*")) value = requestValues.get(valueKey);		
		ResourceAction ret = new ResourceAction(resourceActionPattern.getResourceURI(), resourceActionPattern.getActionURI(), value);
		
		return ret;
	}
}
