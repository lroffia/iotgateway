package arces.unibo.iot.mapping;

public class MPRequest {
	String protocol = "";
	String requestString ="";
	
	public MPRequest(String protocol,String requestString){
		this.protocol = protocol;
		this.requestString = requestString;
	}
	
	public String getProtocol() {return protocol;}
	public String getRequestString() {return requestString;}
	
	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof MPRequest)) return false;
		MPRequest target = (MPRequest) obj;
		return (this.protocol.equals(target.protocol) && this.requestString.equals(target.requestString));
	}
	
	@Override
	public String toString(){
		return "MP-Request<"+protocol+","+requestString+">";
	}
	
}
