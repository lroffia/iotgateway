package arces.unibo.gateway.mapping.mappers.protocol;

import arces.unibo.gateway.mapping.IMapper;
import arces.unibo.gateway.mapping.ResourceAction;

public interface IProtocolMapper extends IMapper {
	public ResourceAction mpRequestString2ResourceAction(String request,String requestPattern,ResourceAction resourceActionPattern);
	public String resourceAction2MPResponseString(ResourceAction resourceAction,ResourceAction resourceActionPattern,String responsePattern);
}
