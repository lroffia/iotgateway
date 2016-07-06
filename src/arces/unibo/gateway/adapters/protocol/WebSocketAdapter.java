package arces.unibo.gateway.adapters.protocol;

/*
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
*/
public class WebSocketAdapter extends MPAdapter {
/*	
	@ServerEndpoint("/iot")
	public class iotGatewayEndpoint extends Endpoint {
		   public void onOpen(Session session, EndpointConfig config) {
			   final RemoteEndpoint.Basic remote = session.getBasicRemote();
			   try {
				remote.sendText("Ciao");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			   session.addMessageHandler (new MessageHandler.Whole<String>() {
			      public void onMessage(String text) {
			                 try {
			                	 remote.sendText(text.toUpperCase());
			                 } catch (IOException ioe) {
			                     // handle send failure here
			                 }
			             }

			   });
		   }
		   public void onClose(Session session, CloseReason closeReason) {}
		   public void onError (Session session, Throwable throwable) {}
	}

	iotGatewayEndpoint endpoint = new iotGatewayEndpoint();*/
	
	@Override
	public String adapterName() {
		return "WEBSOCKET ADAPTER";
	}

	@Override
	public String protocolURI() {
		return "iot:ws";
	}

	@Override
	protected void mpResponse(String requestURI, String responseString) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean doStart() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void doStop() {
		// TODO Auto-generated method stub
		
	}

}
