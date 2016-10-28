package arces.unibo.gateway.mapping.mappers.network;

//import arces.unibo.gateway.mapping.ResourceAction;

public class PingPongMapper extends NetworkMapper {

	/*
	public String resourceAction2MNRequestString(String pattern, ResourceAction resourceAction) {
		return pattern;
	}
	
	public ResourceAction mnResponseString2ResourceAction(String response, String pattern, ResourceAction resourceActionPattern) {		
		if (response.equals(pattern)) return new ResourceAction(resourceActionPattern.getResourceURI(),resourceActionPattern.getActionURI(),response);
		return null;
	}
*/
	@Override
	public String getMapperURI() {
		return "iot:PINGPONG";
	}
}
