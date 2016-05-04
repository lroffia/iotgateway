package arces.unibo.gateway.mapping;

import java.util.UUID;

public class MPMapping extends Mapping{
	String protocolURI;
	
	public MPMapping(String protocolURI,String requestPattern,String responsePattern,ResourceAction resourceAction){
		super( resourceAction, requestPattern, responsePattern);
		this.mappingURI = "iot:MP-Mapping_"+UUID.randomUUID().toString();
		this.protocolURI = protocolURI;
	}

	public MPMapping(String URI, String protocolURI,String requestPattern,String responsePattern,ResourceAction resourceAction){
		super( resourceAction, requestPattern, responsePattern);
		this.mappingURI = URI;
		this.protocolURI = protocolURI;
	}
	
	public String getProtocolURI() {return protocolURI;}
	
	@Override
	public String toString() {
		return "Protocol URI: " + protocolURI + " " + super.toString();
	}
}
