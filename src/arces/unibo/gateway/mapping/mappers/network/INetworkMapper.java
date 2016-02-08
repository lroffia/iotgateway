package arces.unibo.gateway.mapping.mappers.network;

import arces.unibo.gateway.mapping.IMapper;
import arces.unibo.gateway.mapping.ContextAction;

public interface INetworkMapper extends IMapper {
	public ContextAction mnResponseString2IoT(String response, String pattern,ContextAction contextPattern);
	public String ioT2MNRequestString(String pattern,ContextAction contextAction);
}
