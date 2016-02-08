package arces.unibo.gateway.adapters.network;

public interface INetworkAdapter {
	public String networkURI();
	public void mnRequest(String request);
	public boolean mnResponse(String request);
	public boolean start();
}
