package arces.unibo.gateway.adapters.network;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import arces.unibo.SEPA.SPARQLApplicationProfile;
import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

import dash7Adapter.STDash7Coordinator;
import dash7Adapter.STDash7Coordinator.BatteryNotify;
import dash7Adapter.STDash7Coordinator.StatusNotify;
import dash7Adapter.STDash7Coordinator.TemperatureNotify;
import dash7Adapter.STDash7Coordinator.ValveNotify;

/*
 * MN-Request Strings
 * 1) STATUS@<NODE_ID>
 * 2) TEMPERATURE@<NODE_ID>
 * 3) VALVE@<NODE_ID>&<VALUE>
 * 4) BATTERY@<NODE_ID>
 * 
 * MN-Response Strings
 * 1) STATUS!<NODE_ID>&<VALUE>
 * 2) TEMPERATURE!<NODE_ID>&<VALUE>
 * 3) VALVE!<NODE_ID>&<VALUE>
 * 4) BATTERY!<NODE_ID>&<VALUE>
 *  
 * */

public class DASH7Adapter extends MNAdapter implements Observer{

	STDash7Coordinator dash7Coordinator = null;
	private RequestThread running = null;
	private String response = "";
	private static Semaphore busy = new Semaphore(1,true);
	
	@Override
	public void update(Observable o, Object arg) {		
		if(arg instanceof StatusNotify) 
			response = String.format("STATUS!%s&%s",((StatusNotify) arg).id,((StatusNotify) arg).status);
		if(arg instanceof TemperatureNotify) 
			response = String.format("TEMPERATURE!%s&%.2f",((TemperatureNotify) arg).id,((TemperatureNotify) arg).temperature);
		if(arg instanceof ValveNotify) 
			response = String.format("VALVE!%s&%s",((ValveNotify) arg).id,((ValveNotify) arg).ack);
		if(arg instanceof BatteryNotify) 
			response = String.format("BATTERY!%s&%s",((BatteryNotify) arg).id,((BatteryNotify) arg).vBatt);

		if (running != null) running.interrupt();
	}
	
	class RequestThread extends Thread {
		
		private String req;
		private byte[] id;
		private byte value;
		private long timeout = 10000;
		
		public RequestThread(String req,byte[] id,byte value) {
			this.req = req;
			this.id = id;
			this.value = value;
		}
		
		public void run() {
			try 
			{
				switch(req){
					case "STATUS": dash7Coordinator.SendGetStatusCommand(id);			
						break;
					case "TEMPERATURE": dash7Coordinator.SendReadTempCommand(id);
						break;
					case "VALVE": dash7Coordinator.SendSetValveCommand(id,value);
						break;
					case "BATTERY": dash7Coordinator.SendGetBatteryStatusCommand(id);
						break;
				}
			} 
			catch (IOException e) 
			{
				mnResponse(req+"!"+id+"&ERROR");
				e.printStackTrace();
				busy.release();
				return;
			}
			
			try 
			{
				sleep(timeout);
			} 
			catch (InterruptedException e) 
			{
				mnResponse(response);
				busy.release();
				return;
			}
			
			mnResponse(req+"!"+id+"&TIMEOUT");
			busy.release();
		}
	}
	
	public static void main(String[] args) throws IOException {		
		byte[] line = new byte[80];
		byte[] chars;
		int nBytes = 0;
		String IP = "127.0.0.1";
		int PORT = 10123;
		String namespace = "IoTGateway";
		
		String path = DASH7Adapter.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"GatewayProfile.xml";
		
		if(!SPARQLApplicationProfile.load(path)) {
			Logging.log(VERBOSITY.FATAL, "ADAPTER SETTINGS", "Failed to load: "+ path);
			return;
		}
		
		Logging.log(VERBOSITY.INFO,"ADAPTER SETTINGS","Gateway IP (press return for default, "+IP+" )");		
		nBytes = System.in.read(line);

		if (nBytes > 1) {
			chars = new byte[nBytes-1];
			for(int i=0 ; i < nBytes-1 ; i++) chars[i] = line[i];
			IP = new String(chars);
		}
		
		Logging.log(VERBOSITY.INFO,"ADAPTER SETTINGS","Gateway PORT (press return for default, " + PORT+ " )");
		nBytes = System.in.read(line);
		
		if (nBytes > 1) {
			chars = new byte[nBytes-1];
			for(int i=0 ; i < nBytes-1 ; i++) chars[i] = line[i];
			PORT = Integer.parseInt(new String(chars));
		}
		
		Logging.log(VERBOSITY.INFO,"ADAPTER SETTINGS","Gateway Namespace (press return for default, " +namespace +" )");
		nBytes = System.in.read(line);
		
		if (nBytes > 1) {
			chars = new byte[nBytes-1];
			for(int i=0 ; i < nBytes-1 ; i++) chars[i] = line[i];
			namespace = new String(chars);
		}
		
		DASH7Adapter adapter;
		adapter =new DASH7Adapter(IP,PORT,namespace);
		
		if(adapter.start()) {
			Logging.log(VERBOSITY.INFO,adapter.adapterName(),adapter.adapterName() + " is connected to gateway "+IP+":"+PORT+"@"+namespace);
			Logging.log(VERBOSITY.INFO,adapter.adapterName(),"Press any key to exit...");
			System.in.read();
			if(adapter.stop()) Logging.log(VERBOSITY.INFO,adapter.adapterName(),adapter.adapterName() + " stopped");
		}
		else {
			Logging.log(VERBOSITY.FATAL,adapter.adapterName(),adapter.adapterName() + " is NOT running");
			Logging.log(VERBOSITY.FATAL,adapter.adapterName(),"Press any key to exit...");
			System.in.read();
		}	
	}
	
	public DASH7Adapter(){
		super();
	}
	
	public DASH7Adapter(String SIB_IP,int SIB_PORT,String SIB_NAME){
		super(SIB_IP, SIB_PORT,SIB_NAME);
	}
	
	@Override
	public boolean doStart() {
		Logging.log(VERBOSITY.INFO,this.adapterName(),"Starting...");
		
		dash7Coordinator = new STDash7Coordinator();
		dash7Coordinator.addObserver(this);
		
		try 
		{
			dash7Coordinator.startRunning();
		} 
		catch (IOException e) {
			Logging.log(VERBOSITY.FATAL,this.adapterName(),e.getMessage());
			return false;
		}
		
		Logging.log(VERBOSITY.INFO,this.adapterName(),"Started");
		
		return true;
	}

	public void mnRequest(String request) {
		
		String id = "";
		String value = "0";
		
		//Parse MN-Request
		String[] req = request.split("@");
		id = req[1];
		if (req[0].equals("VALVE")) 
		{
			String[] values = req[1].split("&");
			id = values[0];
			value = values[1];	
		}
		
		if (!busy.tryAcquire()) 
		{
			mnResponse(req[0]+"!"+id+"&BUSY");
		}
		else 
		{
			running = new RequestThread(req[0],id.getBytes(),Byte.parseByte(value));
			running.start();
		}
	}

	@Override
	public String networkURI() {
		return "iot:DASH7";
	}

	@Override
	protected void doStop() {
		dash7Coordinator.stopRunning();
	}

	@Override
	public String adapterName() {
		return "DASH7 ADAPTER";
	}
}
