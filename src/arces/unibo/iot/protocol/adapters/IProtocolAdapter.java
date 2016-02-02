package arces.unibo.iot.protocol.adapters;

public interface IProtocolAdapter {
	public String protocolURI();
	public void mpResponse(String request,String value);
	public boolean start();
}
