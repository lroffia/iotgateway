package arces.unibo.gateway.mapping;

import java.util.UUID;

public class MNMapping extends Mapping{
	String networkURI;
	
	public MNMapping(String networkURI,String requestPattern,String responsePattern,ResourceAction resourceAction){
		super( resourceAction, requestPattern, responsePattern);
		this.mappingURI = "iot:MN-Mapping_"+UUID.randomUUID().toString();
		this.networkURI = networkURI;
	}

	public MNMapping(String URI,String networkURI,String requestPattern,String responsePattern,ResourceAction resourceAction){
		super( resourceAction, requestPattern, responsePattern);
		this.mappingURI = URI;
		this.networkURI = networkURI;
	}
	
	public String getNetworkURI() {return networkURI;}
	
	@Override
	public String toString() {
		return "Network URI: " + networkURI + " " + super.toString();
	}
}
