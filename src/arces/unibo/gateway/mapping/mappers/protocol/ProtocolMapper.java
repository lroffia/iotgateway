package arces.unibo.gateway.mapping.mappers.protocol;

import java.util.HashMap;
import java.util.Iterator;

import arces.unibo.gateway.mapping.ContextAction;

public abstract class ProtocolMapper implements IProtocolMapper {
	public String ioT2MPResponseString(String pattern, ContextAction contextAction) {
		return pattern.replace("*", contextAction.getValue());
	}
	
	public ContextAction mpRequestString2IoT(String request, String pattern,ContextAction contextPattern) {
		
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
		String value = contextPattern.getValue();
		if (contextPattern.getActionURI().equals("iot:SET")) 
			if (valueKey != null && value.equals("*")) value = requestValues.get(valueKey);		
		ContextAction ret = new ContextAction(contextPattern.getContextURI(), contextPattern.getActionURI(), value);
		
		return ret;
	}
}
