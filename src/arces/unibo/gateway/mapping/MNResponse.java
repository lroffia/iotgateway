package arces.unibo.gateway.mapping;

import java.util.UUID;

public class MNResponse {
	String networkURI = "";
	String responseString = "";
	String URI = "";
	
	public MNResponse(String networkURI, String responseString) {
		this.networkURI = networkURI;
		this.responseString = responseString;
		this.URI = "iot:MN-Response_"+UUID.randomUUID().toString();
	}
	
	public String getNetwork(){return networkURI;}
	public String getResponseString(){return responseString;}
	public String getURI() {return URI;}
	
	@Override
	public String toString(){
		return "MN-Response<"+networkURI+","+responseString+">";
	}
}
