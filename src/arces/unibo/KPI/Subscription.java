package arces.unibo.KPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Vector;

import arces.unibo.KPI.SSAP_sparql_response;
import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public class Subscription {

	Socket         in_socket;

	iKPIC_subscribeHandler2 handler;

	public Subscription(Socket in_sock, iKPIC_subscribeHandler2 hand)
	{
		final iKPIC_subscribeHandler2 f_eh= hand;
		final Socket         ft_kpSocket = in_sock;
		InputStream reader = null;
		
		try 
		{
			reader = in_sock.getInputStream();
		} 
		catch (IOException e2) {
			Logging.log (VERBOSITY.FATAL,"KPI",e2.getMessage());
		}

		final BufferedReader	ft_in = new BufferedReader(  new InputStreamReader(reader));

		Logging.log (VERBOSITY.DEBUG,"KPI","Subscription: going to start thread");

		Thread eventThread = new Thread()
		{
			public void run() {
				SSAP_XMLTools xmlTools=new SSAP_XMLTools(null,null,null);
				String msg_event="";   
				String restOfTheMessage="";
				int buffsize= 4 *1024;
				StringBuilder builder = new StringBuilder();
				char[] buffer = new char[buffsize];
				int charRead =0;
				
				try
				{
					while (  ( (charRead = ft_in.read(buffer, 0, buffer.length)) != (-1)) || (!restOfTheMessage.isEmpty())  ) 
					{
						if(!restOfTheMessage.equals(""))
						{
							builder.append(restOfTheMessage);
							restOfTheMessage = "";
						}
						if(charRead != -1)
						{
							builder.append(buffer, 0 , charRead);
						}

						msg_event = builder.toString();


						if(  msg_event.contains("<SSAP_message>") 
								&& msg_event.contains("</SSAP_message>"))
						{//One or more messages in the same notification
							
							int index = msg_event.indexOf("</SSAP_message>") + 15;
							restOfTheMessage = msg_event.substring(index);
							msg_event = msg_event.substring(0, index);
							String subID = xmlTools.getSubscriptionID(msg_event);

							// here it starts single message processing and it is possible to launch multiple threads for parallelization

							if(xmlTools.isUnSubscriptionConfirmed(msg_event))
							{
								f_eh.kpic_UnsubscribeEventHandler( subID  );
								return;
							}
							else 
							{
								String indSequence = xmlTools.getSSAPmsgIndicationSequence(msg_event);
								if(xmlTools.isRDFNotification(msg_event))
								{
									Vector<Vector<String>> triples_n = new Vector<Vector<String>>();
									triples_n = xmlTools.getNewResultEventTriple(msg_event);
									Vector<Vector<String>> triples_o = new Vector<Vector<String>>();
									triples_o = xmlTools.getObsoleteResultEventTriple(msg_event);
									f_eh.kpic_RDFEventHandler(triples_n, triples_o, indSequence, subID);
								}
								else
								{
									SSAP_sparql_response resp_new = xmlTools.get_SPARQL_indication_new_results(msg_event);
									SSAP_sparql_response resp_old = xmlTools.get_SPARQL_indication_obsolete_results(msg_event);
									f_eh.kpic_SPARQLEventHandler(resp_new, resp_old, indSequence, subID);
								}

								if(  restOfTheMessage.contains("<SSAP_message>") 
										&& restOfTheMessage.contains("</SSAP_message>"))//a complete message in the rest of the message
								{						
									String test = restOfTheMessage.substring(0, restOfTheMessage.indexOf("</SSAP_message>") +15);
									if (xmlTools.isUnSubscriptionConfirmed(test))
									{
										f_eh.kpic_UnsubscribeEventHandler( subID  );
										return;	
									}
								}

								buffer = new char[buffsize];
								charRead = 0;
								msg_event="";
								builder = new StringBuilder();
							}
						}
					}
					try
					{
						Logging.log(VERBOSITY.DEBUG,"KPI","I should not go here untili unsubscribe");
						ft_in.close();
						ft_kpSocket.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
						f_eh.kpic_ExceptionEventHandler(e);
						Logging.log(VERBOSITY.FATAL,"KPI",e.getMessage());
					}	
				}

				catch(Exception e)
				{
					e.printStackTrace();
					f_eh.kpic_ExceptionEventHandler(e);
					Logging.log(VERBOSITY.FATAL,"KPI",e.getMessage());
				}
			}
			};

			eventThread.start(); 
	}
}




