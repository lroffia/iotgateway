package arces.unibo.iot.mapping;

public class MNRequest {
	String network;
	String requestString;
	
	public MNRequest(String network,String requestString){
		this.network = network;
		this.requestString = requestString;
	}
	
	public String getNetwork(){
		return network;
	}
	
	public String getRequestString(){
		return requestString;
	}
	
	@Override
	public String toString(){
		return "MN-Request<"+network+","+requestString+">";
	}
}
