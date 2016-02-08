package arces.unibo.gateway.adapters.protocol;

public interface IProtocolAdapter {
	public String protocolURI();
	public void mpResponse(String request,String value);
}
