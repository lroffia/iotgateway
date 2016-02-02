package arces.unibo.iot.network.adapters;

public interface INetworkAdapter {
	public String networkURI();
	public void mnRequest(String request);
	public boolean mnResponse(String request);
	public boolean start();
}
