package arces.unibo.iot.mapping;

public class MNResponse {
	String networkURI = "";
	String responseString = "";
	
	public MNResponse(String networkURI, String responseString) {
		this.networkURI = networkURI;
		this.responseString = responseString;
	}
	
	public String getNetwork(){return networkURI;}
	
	public String getResponseString(){return responseString;}
	
	@Override
	public String toString(){
		return "MN-Response<"+networkURI+","+responseString+">";
	}
}
