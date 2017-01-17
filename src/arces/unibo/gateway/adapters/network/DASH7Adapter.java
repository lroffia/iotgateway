package arces.unibo.gateway.adapters.network;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import arces.unibo.SEPA.application.Logger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.Logger.VERBOSITY;

import dash7Adapter.STDash7Coordinator;
import dash7Adapter.STDash7Coordinator.BatteryNotify;
import dash7Adapter.STDash7Coordinator.BeaconNotify;
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
 * 1) STATUS!<NODE_ID>&<VALUE>&<TIMESTAMP>
 * 2) TEMPERATURE!<NODE_ID>&<VALUE>&<TIMESTAMP>
 * 3) VALVE!<NODE_ID>&<VALUE>&<TIMESTAMP>
 * 4) BATTERY!<NODE_ID>&<VALUE>&<TIMESTAMP>
 * 5) BEACON!<NODE_ID>&<TIMESTAMP>
 *  
 * */

public class DASH7Adapter extends MNAdapter implements Observer{
	STDash7Coordinator dash7Coordinator = null;
	private RequestThread running = null;
	private String response = "";
	private static Semaphore busy = new Semaphore(1,true);
	
	@Override
	public void update(Observable o, Object arg) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String timestamp = sdf.format(date);
		
		if(arg instanceof StatusNotify) 
			response = String.format("STATUS!%s&%s&%s",((StatusNotify) arg).id,((StatusNotify) arg).status,timestamp);
		if(arg instanceof TemperatureNotify) 
			response = String.format("TEMPERATURE!%s&%.2f&%s",((TemperatureNotify) arg).id,((TemperatureNotify) arg).temperature,timestamp);
		if(arg instanceof ValveNotify) 
			response = String.format("VALVE!%s&%s&%s",((ValveNotify) arg).id,((ValveNotify) arg).ack,timestamp);
		if(arg instanceof BatteryNotify) 
			response = String.format("BATTERY!%s&%s&%s",((BatteryNotify) arg).id,((BatteryNotify) arg).vBatt,timestamp);
		if(arg instanceof BeaconNotify) 
			response = String.format("BEACON!%s&%s",((BeaconNotify) arg).id,timestamp);
		
		//Wake-up waiting thread or send response (beacon)
		if (running != null) {
			if (running.isAlive())
				running.interrupt();
			else
				mnResponse(response);
		}
		else mnResponse(response);
	}
	
	class RequestThread extends Thread {
		
		private String req;
		private byte[] id;
		private byte value;
		private long timeout = 15000;
		
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
		String path = "GatewayProfile.sap";
		ApplicationProfile appProfile = new ApplicationProfile();
		
		if(!appProfile.load(path)) {
			Logger.log(VERBOSITY.FATAL, "DASH7", "Failed to load: "+ path);
			return;
		}
		else Logger.log(VERBOSITY.INFO, "DASH7", "Loaded application profile "+ path);
		
		DASH7Adapter adapter;
		adapter =new DASH7Adapter(appProfile);
		
		if(adapter.start()) {
			Logger.log(VERBOSITY.INFO,adapter.adapterName(),"Press any key to exit...");
			System.in.read();
			if(adapter.stop()) Logger.log(VERBOSITY.INFO,adapter.adapterName(),adapter.adapterName() + " stopped");
		}
		else {
			Logger.log(VERBOSITY.FATAL,adapter.adapterName(),adapter.adapterName() + " is NOT running");
			Logger.log(VERBOSITY.FATAL,adapter.adapterName(),"Press any key to exit...");
			System.in.read();
		}	
	}
	
	public DASH7Adapter(ApplicationProfile appProfile){
		super(appProfile);
	}
	
	@Override
	public boolean doStart() {
		Logger.log(VERBOSITY.INFO,this.adapterName(),"Starting...");
		
		dash7Coordinator = new STDash7Coordinator();
		dash7Coordinator.addObserver(this);
		
		try 
		{
			dash7Coordinator.startRunning();
		} 
		catch (IOException e) {
			Logger.log(VERBOSITY.FATAL,this.adapterName(),e.getMessage());
			return false;
		}
		
		Logger.log(VERBOSITY.INFO,this.adapterName(),"Started");
		
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
