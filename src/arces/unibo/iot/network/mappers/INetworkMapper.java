package arces.unibo.iot.network.mappers;

import arces.unibo.iot.mapping.IMapper;
import arces.unibo.iot.mapping.IoTContextAction;

public interface INetworkMapper extends IMapper {
	public IoTContextAction mnResponseString2IoT(String response, String pattern,IoTContextAction contextPattern);
	public String ioT2MNRequestString(String pattern,IoTContextAction contextAction);
}
