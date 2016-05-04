package arces.unibo.gateway.mapping;

import java.util.UUID;

public class MNRequest {
	String network;
	String requestString;
	String URI;
	
	public MNRequest(String network,String requestString){
		this.network = network;
		this.requestString = requestString;
		this.URI = "iot:MN-Request_"+UUID.randomUUID().toString();
	}
	
	public String getNetwork(){return network;}
	public String getRequestString(){return requestString;}
	public String getURI() {return URI;}
	
	@Override
	public String toString(){
		return "MN-Request<"+network+","+requestString+">";
	}
}
