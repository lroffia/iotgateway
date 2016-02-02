package arces.unibo.iot.protocol.mappers;

import arces.unibo.iot.mapping.IMapper;
import arces.unibo.iot.mapping.IoTContextAction;

public interface IProtocolMapper extends IMapper {
	public IoTContextAction mpRequestString2IoT(String request,String pattern,IoTContextAction contextPattern);
	public String ioT2MPResponseString(String pattern, IoTContextAction contextAction);
}
