package arces.unibo.iot.mapping;

public class MPResponse {
	String protocol = "";
	String responseString ="";
	
	public MPResponse(String protocol,String responseString){
		this.protocol = protocol;
		this.responseString = responseString;
	}
	
	public String getProtocol() {return protocol;}
	public String getResponseString() {return responseString;}
	
	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof MPResponse)) return false;
		MPResponse target = (MPResponse) obj;
		return (this.protocol.equals(target.protocol) && this.responseString.equals(target.responseString));
	}
	
	@Override
	public String toString(){
		return "MP-Response<"+protocol+","+responseString+">";
	}
}
