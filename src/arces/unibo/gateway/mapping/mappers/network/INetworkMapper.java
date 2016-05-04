package arces.unibo.gateway.mapping.mappers.network;

import arces.unibo.gateway.mapping.ResourceAction;
import arces.unibo.gateway.mapping.IMapper;

public interface INetworkMapper extends IMapper {
	public ResourceAction mnResponseString2ResourceAction(String response, String pattern,ResourceAction resourceActionPattern);
	public String resourceAction2MNRequestString(String pattern,ResourceAction resourceAction);
}
