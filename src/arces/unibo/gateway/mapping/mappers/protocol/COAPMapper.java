package arces.unibo.gateway.mapping.mappers.protocol;

import arces.unibo.gateway.mapping.ResourceAction;

public class COAPMapper implements IProtocolMapper {

	@Override
	public ResourceAction mpRequestString2ResourceAction(String request, String requestPattern, ResourceAction resourceActionPattern) {
		if(request.equals(requestPattern)) return resourceActionPattern;
		return null;
	}

	@Override
	public String getMapperURI() {
		return "iot:COAP";
	}

	@Override
	public String resourceAction2MPResponseString(ResourceAction resourceAction, ResourceAction resourceActionPattern,
			String responsePattern) {
		if (resourceAction.equals(resourceActionPattern)) return responsePattern.replace("*", resourceAction.getValue());
		return null;
	}

}
