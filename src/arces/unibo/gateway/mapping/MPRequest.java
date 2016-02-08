package arces.unibo.gateway.mapping;

import java.util.UUID;

public class MPRequest {
	String protocol = "";
	String requestString ="";
	String URI = "";
	
	public MPRequest(String protocol,String requestString){
		this.protocol = protocol;
		this.requestString = requestString;
		this.URI = "iot:MP-Request_"+UUID.randomUUID().toString();
	}
	public MPRequest(String protocol,String requestString,String URI){
		this.protocol = protocol;
		this.requestString = requestString;
		this.URI = URI;
	}
	
	public String getProtocol() {return protocol;}
	public String getRequestString() {return requestString;}
	public String getURI() {return URI;}
	
	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof MPRequest)) return false;
		MPRequest target = (MPRequest) obj;
		return (this.URI.equals(target.URI) || (this.protocol.equals(target.protocol) && this.requestString.equals(target.requestString)));
	}
	
	@Override
	public String toString(){
		return "MP-Request<"+URI+","+protocol+","+requestString+">";
	}
	
}
