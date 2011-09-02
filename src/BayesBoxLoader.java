import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Stefano Bonetta
 * 
 */
public class BayesBoxLoader {

	/**The XML Document */
	private Document BayesXML;

	
	public BayesBoxLoader(){}

	/**
	 * Create Bayes network from XML file.
	 * @param path the path of the XML file
	 */
	public BayesBoxLoader(String path){
		try {
			loadFromFile(path);
		} catch (BayesBoxIOExc e) {
			e.printStackTrace();
		}
	}
	
	public Document getXMLDoc(){return BayesXML;}

	/**
	 * Load XML
	 * @param path the path of the xml file.
	 * @throws BayesBoxIOExc throws IO and XML errors.
	 */
	private void loadFromFile(String path) throws BayesBoxIOExc {
		try {
			File file = new File(path);

			//Disabilito caricamento esterno dtd (non utile), per evitare errori
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			
			DocumentBuilder db = dbf.newDocumentBuilder();
			BayesXML = db.parse(file);
			BayesXML.getDocumentElement().normalize();
			
		} catch (ParserConfigurationException e) {throw new BayesBoxIOExc("Parse error.");} 
		  catch (SAXException e) {throw new BayesBoxIOExc("SAX error.");} 
		  catch (IOException e) {throw new BayesBoxIOExc("IO error.");}
	}

	
	public BayesNet getBayesNet(){
		return null;
		
	}
	
	///DEBUG///
	public static void main(String argv[]) {
		try {

			BayesBoxLoader bb = new BayesBoxLoader("/home/ziby/Scrivania/ee4.xml");
			BayesNet net = bb.getBayesNet();

			//Tmp creation
			ArrayList<BayesNetNode> tmpNodes = new ArrayList<BayesNetNode>();
			
			Document XMLstruct = bb.getXMLDoc();
			
			//Leggo i nodi
			NodeList nodeLst = XMLstruct.getElementsByTagName("VAR");
			for (int s = 0; s < nodeLst.getLength(); s++) {
				Node fstNode = nodeLst.item(s);
				
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					BayesNetNode tmpNode = new BayesNetNode(fstNode.getAttributes().getNamedItem("NAME").getTextContent()+"");

					NodeList child = fstNode.getChildNodes();
					
					for(int i=0;i< child.getLength();i++){
						String val = child.item(i).getNodeName();
						if(val.equals("DESCRIPTION")){
							tmpNode.setDescription(child.item(i).getTextContent());
						}
						else if(val.equals("STATENAME")){
							tmpNode.setStateNames(child.item(i).getTextContent(),child.item(i+2).getTextContent());
							break;
						}
					}
					
					//DEBUG
					System.out.println("NODE name: " + tmpNode.getVariable()       + "\n" +
									   "     desc: " + tmpNode.getDescription()    + "\n" +
									   "     val1: " + tmpNode.getStateNames()[0]  + "\n" +
									   "     val2: " + tmpNode.getStateNames()[1]  + "\n" );
					tmpNodes.add(tmpNode);
				}
			}
			
			
			//Estraggo dipendenze
			nodeLst = XMLstruct.getElementsByTagName("DIST");
			
			//Var di comodo per evitare allocazioni ripetute
			BayesNetNode currentNode;
			ArrayList<BayesNetNode> dependingNodes;
			
			for (int s = 0; s < nodeLst.getLength(); s++) {
				Node fstNode = nodeLst.item(s);
				
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					
					NodeList child = fstNode.getChildNodes();
					
					currentNode=null;
					dependingNodes = new ArrayList<BayesNetNode>();
					ArrayList<String> smartValue = null;
					
					for(int i=0;i< child.getLength();i++){
						String val = child.item(i).getNodeName();
						if(val.equals("PRIVATE")){ //l'identificativo del nodo di cui stiamo per leggere le dipendenze
							currentNode = getBayesNetNodeById(tmpNodes, child.item(i).getAttributes().getNamedItem("NAME").getTextContent()+"");
							System.out.println("Node find: "+child.item(i).getAttributes().getNamedItem("NAME").getTextContent()+" alias " +currentNode.getDescription());
						}
						else if(val.equals("CONDSET")){
							NodeList subchild = child.item(i).getChildNodes();
							for(int j=0;j< subchild.getLength();j++){
								String subval = subchild.item(j).getNodeName();
								
								if(subval.equals("CONDELEM")){ //l'identificativo del nodo di cui stiamo per leggere le dipendenze
									dependingNodes.add(getBayesNetNodeById(tmpNodes, subchild.item(j).getAttributes().getNamedItem("NAME").getTextContent()+""));
									System.out.println("DependingNode find: "+subchild.item(j).getAttributes().getNamedItem("NAME").getTextContent()+" alias "+dependingNodes.get(dependingNodes.size()-1).getDescription());
								}
							}
						}
						else if(val.equals("DPIS")){
							NodeList subchild = child.item(i).getChildNodes();
							String tmpVal="";
							smartValue = new ArrayList<String>();
							for(int j=0;j< subchild.getLength();j++){
								String subval = subchild.item(j).getNodeName();
								
								
								if(subval.equals("DPI")){ //l'identificativo del nodo di cui stiamo per leggere le dipendenze
									tmpVal = (subchild.item(j).getTextContent()+"").trim();
									
									tmpVal = (tmpVal.subSequence(0,tmpVal.indexOf(" "))).toString().trim();
									smartValue.add(tmpVal);
									System.out.println("Value found: "+tmpVal);
									
								}
							}
						}
					}
					currentNode.influencedBy(dependingNodes);
					inflateDistribution(currentNode,smartValue);

					System.out.println("Distribution:\n"+currentNode.getDistribution().toString());
				}
				
			}  
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void inflateDistribution(BayesNetNode node, ArrayList<String> values){
		for(int i=0;i<values.size();i++){
			node.setProbability(Double.parseDouble(values.get(i)), getBooleanConfiguration(values.size(),i));
		}
	}
	
	private static boolean[] getBooleanConfiguration(int base, int pos){
		int size = base;//(int) Math.pow(2, base);
		
		int dim = (int) Util.log2(base);
		
		System.out.println(size);
		boolean ris[] = new boolean[dim];
		int targetBin = size-1-pos;
		String conf = Integer.toBinaryString(targetBin);
		while(conf.length()<dim)conf = "0"+conf;
		System.out.println("Binario: "+conf);
		for(int i=0;i<ris.length;i++){
			if(conf.charAt(i) == '0')
				ris[i] = false;
			else 
				ris[i] = true;
		}
		
		return ris;
	}
	
	private static BayesNetNode getBayesNetNodeById(ArrayList<BayesNetNode> list,String id){
		
		for(int i=0;i<list.size();i++){
			if(list.get(i).getVariable().equals(id))return list.get(i);
		}
		return null;
		
	}
}
