package arces.unibo.gateway.mapping.mappers.network;

public class MQTTMapper extends NetworkMapper {

	@Override
	public String getMapperURI() {
		return "iot:MQTT";
	}

}
