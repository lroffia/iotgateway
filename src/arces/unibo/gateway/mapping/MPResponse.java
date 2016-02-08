package arces.unibo.gateway.mapping;

import java.util.UUID;

public class MPResponse {
	String protocol = "";
	String responseString ="";
	String URI = "";
	
	public MPResponse(String protocol,String responseString){
		this.protocol = protocol;
		this.responseString = responseString;
		this.URI = "iot:MP-Response_"+UUID.randomUUID().toString();
	}
	
	public String getProtocol() {return protocol;}
	public String getResponseString() {return responseString;}
	public String getURI() {return URI;}
	
	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof MPResponse)) return false;
		MPResponse target = (MPResponse) obj;
		return (this.URI.equals(target.URI) || (this.protocol.equals(target.protocol) && this.responseString.equals(target.responseString)));
	}
	
	@Override
	public String toString(){
		return "MP-Response<"+URI+","+protocol+","+responseString+">";
	}
}
