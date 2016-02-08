package arces.unibo.gateway.adapters.network;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import dash7Adapter.STDash7Coordinator;
import dash7Adapter.STDash7Coordinator.StatusNotify;
import dash7Adapter.STDash7Coordinator.TemperatureNotify;
import dash7Adapter.STDash7Coordinator.ValveNotify;

/*
 * MN-Request Strings
 * 1) STATUS?<NODE_ID>
 * 2) TEMPERATURE?<NODE_ID>
 * 3) VALVE?<NODE_ID>&<VALUE>
 * 
 * MN-Response Strings
 * 1) STATUS!<NODE_ID>&<VALUE>
 * 2) TEMPERATURE!<NODE_ID>&<VALUE>
 * 3) VALVE!<NODE_ID>&<VALUE>
 *  
 * */

public class DASH7Adapter extends MNAdapter implements Observer{
	
	STDash7Coordinator dash7Coordinator = null;
	
	private static Semaphore busy = new Semaphore(1,true);
	
	public DASH7Adapter(){
		super();
	}
	
	@Override
	public boolean start() {
		System.out.println("*****************");
		System.out.println("* DASH7 Adapter *");
		System.out.println("*****************");
		
		System.out.println("DASH7 Request Strings");
		System.out.println("--------------------------------------------------------------");
		System.out.println("STATUS?<NODE_ID>");	
		System.out.println("TEMPERATURE?<NODE_ID>");	
		System.out.println("VALVE?<NODE_ID>&<VALUE>");	
		System.out.println("--------------------------------------------------------------");
		System.out.println("DASH7 Response Strings");
		System.out.println("--------------------------------------------------------------");
		System.out.println("STATUS&<NODE_ID>&<VALUE>");	
		System.out.println("TEMPERATURE&<NODE_ID>&<VALUE>");	
		System.out.println("VALVE&<NODE_ID>&<VALUE>");	
		System.out.println("--------------------------------------------------------------");
		
		dash7Coordinator = new STDash7Coordinator();
		dash7Coordinator.addObserver(this);
			
		return super.start();
	}

	@Override
	public void update(Observable o, Object arg) {
		String response = "";
		
		if(arg instanceof StatusNotify) response = String.format("STATUS!%s&%s",((StatusNotify) arg).id,((StatusNotify) arg).status);
		if(arg instanceof TemperatureNotify) response = String.format("TEMPERATURE!%s&%.2f",((TemperatureNotify) arg).id,((TemperatureNotify) arg).temperature);
		if(arg instanceof ValveNotify) response = String.format("VALVE!%s&%s",((ValveNotify) arg).id,((ValveNotify) arg).ack);
		
		//Send MN-RESPONSE
		if (!response.equals("")) super.mnResponse(response);
		
		busy.release();
	}

	public void mnRequest(String request) {
		
		String id = "";
		String value = "";
		
		String[] req = request.split("@");
		id = req[1];
		if (req[0].equals("VALVE")) {
			String[] values = req[1].split("&");
			id = values[0];
			value = values[1];	
		}
		
		//ST DASH7 manages one request at a time 
		if (!busy.tryAcquire()) {
			super.mnResponse(req[0]+"!"+id+"&BUSY");
			return;
		}
		
		try {
			switch(req[0]){
				case "STATUS": dash7Coordinator.SendGetStatusCommand(id.getBytes());			
					break;
				case "TEMPERATURE": dash7Coordinator.SendReadTempCommand(id.getBytes());
					break;
				case "VALVE": dash7Coordinator.SendSetValveCommand(id.getBytes(),Byte.parseByte(value));
					break;
			}
		} catch (IOException e) {
			super.mnResponse(req[0]+"!"+id+"&ERROR");
			e.printStackTrace();
		}
	}

	@Override
	public String networkURI() {
		return "iot:DASH7";
	}
}
