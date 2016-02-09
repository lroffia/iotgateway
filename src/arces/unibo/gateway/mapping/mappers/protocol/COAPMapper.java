package arces.unibo.gateway.mapping.mappers.protocol;

import arces.unibo.gateway.mapping.ContextAction;

public class COAPMapper implements IProtocolMapper {

	@Override
	public ContextAction mpRequestString2IoT(String request, String pattern, ContextAction contextPattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String ioT2MPResponseString(String pattern, ContextAction contextAction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMapperURI() {
		return "iot:COAP";
	}

}
