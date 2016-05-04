package arces.unibo.KPI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import arces.unibo.tools.Logging;
import arces.unibo.tools.Logging.VERBOSITY;

public class SIBResponse {

	public  String Message= "";
	public  String MessageType = "";
	public  String TransactionType = "";
	public  String Status = "";
	public  String transactionID;
	public  String node_id="";
	public  String space_id="";
	public  String triples_encoding="";
	public  String subscription_id="";
	public  String update_id="";
	public  String indication_sequence;
	public Vector<Vector<String>> new_results= new Vector<Vector<String> >();
	public Vector<Vector<String>> obsolete_results= new Vector<Vector<String> >();
	public Vector<Vector<String>> query_results= new Vector<Vector<String> >();
	public SSAP_sparql_response sparqlquery_results= null;
	public SSAP_sparql_response sparql_ind_new_results= null;
	public SSAP_sparql_response sparql_ind_old_results= null;
	public String rdf_xml_graph ="";
	public String rdf_xml_remove_graph ="";
	public String queryType = "";

	public SIBResponse() {
		// TODO Auto-generated constructor stub
	}
	
	public SIBResponse(String primitive, String message, String subscriptionID){
		if ((message == null) && (primitive.equals("UNSUB"))){
			Message= "";
			MessageType = "CONFIRM";
			TransactionType = "UNSUBSCRIBE";
			Status = "m3:unknown";
			transactionID = "";
			node_id="";
			space_id="";
			triples_encoding="";
			subscription_id=subscriptionID;
			update_id="";
			indication_sequence = "";
			new_results= new Vector<Vector<String> >();
			obsolete_results= new Vector<Vector<String> >();
			query_results= new Vector<Vector<String> >();
			sparqlquery_results= null;
			sparql_ind_new_results= null;
			sparql_ind_old_results= null;
			rdf_xml_graph ="";
			rdf_xml_remove_graph ="";
			queryType = "";
		}
	}

	public SIBResponse(String xml) throws JDOMException, IOException
	{
		this.Message = xml;
		this.Status = " parsing message ";
		
		SAXBuilder builder = new SAXBuilder();
		Document sparql_response_document = null;

		sparql_response_document = builder.build(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));

		//	        GET ROOT ELEMENT + NAMESPACE
		if(sparql_response_document != null)
		{
			Element root = sparql_response_document.getRootElement();
			//			GET ROOT CHILDREN ()
			this.MessageType = root.getChildText("message_type");
			this.transactionID = root.getChildText("transaction_id");
			this.TransactionType = root.getChildText("transaction_id");
			this.node_id = root.getChildText("node_id");
			this.space_id = root.getChildText("space_id");

			List <Element> parameterList = root.getChildren("parameter");

			ArrayList<Element> parToProcess = new ArrayList<Element>();

			for(Element Parameter : parameterList)
			{
				String parName = Parameter.getAttributeValue("name");

				switch (parName){
					case "status": 
						this.Status = Parameter.getText(); 
						break;
					case "subscription_id": 
						this.subscription_id = Parameter.getText();
						break;
					case "update_id" : 
						this.update_id = Parameter.getText();
						break;
					case "type": 
						this.queryType = Parameter.getText(); //here the query type if needed 
						break;
					case "new_results": 
						parToProcess.add(Parameter);
						break;
					case "obsolete_results": 
						parToProcess.add(Parameter);
						break;
					case "results": 
						parToProcess.add(Parameter);
						break;
					case "bnodes": 
						break;
					default : 
						Logging.log(VERBOSITY.DEBUG,"KPI","Error while reading parameter name: " + parName);
						break;
				}
			}

			for(int i = 0; i < parToProcess.size(); i++)
			{
				Element par = parToProcess.get(i);
				String parName = par.getAttributeValue("name");
				if(par.getChild("triple_list")!= null)
				{
					this.queryType = "RDF-M3";
					Vector<Vector<String>> triplev = new Vector<Vector<String>>();
					Element el = par.getChild ("triple_list");

					List<Element> triples =  el.getChildren("triple");

					for (Element etriple: triples){   
						Vector<String> singleton=new Vector<String>();

						singleton.add(etriple.getChild("subject").getText());
						singleton.add(etriple.getChild("predicate").getText());
						singleton.add(etriple.getChild("object").getText());
						singleton.add(etriple.getChild("object").getAttributeValue("type"));

						triplev.add(singleton);			
					}

					if (parName.equals("results"))
					{
						this.query_results = triplev;
					}
					else if (parName.equals("new_results"))
					{
						this.new_results = triplev;
					}
					else if (parName.equals("parToProcess"))
					{
						this.obsolete_results = triplev;
					}

				}
				else if(par.getChild("sparql", Namespace.getNamespace("http://www.w3.org/2005/sparql-results#"))!= null)
				{
					this.queryType = "sparql";
					SSAP_sparql_response sparql_resp = new SSAP_sparql_response(par.getChild("sparql", Namespace.getNamespace("http://www.w3.org/2005/sparql-results#")));
					if (parName.equalsIgnoreCase("results"))
					{
						this.sparqlquery_results = sparql_resp;
					}
					else if (parName.equalsIgnoreCase("new_results"))
					{
						this.sparqlquery_results = sparql_resp;
					}
					else if (parName.equalsIgnoreCase("parToProcess"))
					{
						this.sparqlquery_results = sparql_resp;
					}
				}

			}
		}
	}

	@Override
	public String toString()
	{
		return this.Message;
	}
	
	/**
	 * This method checks if operations is confirmed.<br>
	 * <b>NOTE:</b> for <b>unsubscribe</b> returns always true.
	 * 
	 * @return <b>true</b> if operation is confirmed<br><b>false</b> if operation fails
	 */
	public boolean isConfirmed(){
		if(this.TransactionType != null && this.TransactionType.equalsIgnoreCase("unsubscribe"))
			return true;
		return this.Status.equalsIgnoreCase("m3:success");
	}
}
