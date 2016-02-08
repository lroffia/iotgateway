package arces.unibo.gateway.mapping.mappers.protocol;

import arces.unibo.gateway.mapping.IMapper;
import arces.unibo.gateway.mapping.ContextAction;

public interface IProtocolMapper extends IMapper {
	public ContextAction mpRequestString2IoT(String request,String pattern,ContextAction contextPattern);
	public String ioT2MPResponseString(String pattern, ContextAction contextAction);
}
