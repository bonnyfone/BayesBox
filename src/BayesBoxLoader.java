import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

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

		//Tmp creation
		ArrayList<BayesNetNode> tmpNodes = new ArrayList<BayesNetNode>();
		
		Document XMLstruct = getXMLDoc();
		
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
		boolean isRootNode;
		for (int s = 0; s < nodeLst.getLength(); s++) {
			Node fstNode = nodeLst.item(s);
			isRootNode=false;
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
								//System.out.println("Value found: "+tmpVal);
								if(subchild.item(j).getAttributes().getNamedItem("INDEXES").getTextContent().equals("")){
									isRootNode=true;
								}
								
							}
						}

						
					}
				}
				//if(dependingNodes.size()==0)dependingNodes.add(currentNode);
				if(isRootNode){
					currentNode.setProbability(true, Double.parseDouble(smartValue.get(0)));
				}else{
					currentNode.influencedBy(dependingNodes);
					inflateDistribution(currentNode,smartValue);
				}
				System.out.println("Distribution:\n"+currentNode.getDistribution().toString());
			}
			
		}  
		
		ArrayList<BayesNetNode> roots = new ArrayList<BayesNetNode>();
		
		for(int i=0;i<tmpNodes.size();i++)
			if(tmpNodes.get(i).isRoot())roots.add(tmpNodes.get(i));
		
		 
		return new BayesNet(roots);
		//BayesNet result = new BayesNe
		
	}
	
	private void inflateDistribution(BayesNetNode node, ArrayList<String> values){
		for(int i=0;i<values.size();i++){
			node.setProbability(Double.parseDouble(values.get(i)), getBooleanConfiguration(values.size(),i));
		}
	}
	
	private boolean[] getBooleanConfiguration(int base, int pos){
		int size = base;//(int) Math.pow(2, base);
		int dim = (int) Util.log2(base);
		boolean ris[] = new boolean[dim];
		int targetBin = size-1-pos;
		String conf = Integer.toBinaryString(targetBin);
		while(conf.length()<dim)conf = "0"+conf;
		//System.out.println("Binario: "+conf);
		for(int i=0;i<ris.length;i++){
			if(conf.charAt(i) == '0')
				ris[i] = false;
			else 
				ris[i] = true;
		}
		return ris;
	}
	
	private BayesNetNode getBayesNetNodeById(ArrayList<BayesNetNode> list,String id){
		
		for(int i=0;i<list.size();i++){
			if(list.get(i).getVariable().equals(id))return list.get(i);
		}
		return null;
		
	} 
	
	
	///DEBUG///
	public static void main(String argv[]) {
		BayesBoxLoader bay = new BayesBoxLoader("/home/ziby/Scrivania/test bayes/ee3.xml");
		BayesNet net = bay.getBayesNet();
		
		//System.out.println(net.getPriorSample());
	
		//System.out.println(net.getVariables().toString());


		Hashtable<String, Boolean> evidence = new Hashtable<String, Boolean>();
		evidence.put("id0", true);
		//evidence.put("node_4", true);
		//evidence.put("node_6", true);
		 

		//TEST di enumerationAsk
		double[] ris =net.enumerationAsk("id1", evidence);
 		System.out.println(ris[0]+" , " + ris[1] );		
		
		//TEST di LIKELIHOODWEIGHTING
		//double[] ris =net.likelihoodWeighting("id1", evidence, 1000);
		//System.out.println(ris[0]+" , " + ris[1] );
		  
		//TEST di REJECTIONSAMPLING
//		double[] ris2 = net.rejectionSample("id1", evidence, 100);
//		System.out.println(ris2[0]+" , " + ris2[1] + " (campioni consistenti: "+(int)ris2[2]+", "+ris2[3]+"%)");

//		boolean i = true;
//		int c =0;
//		while(i){
//			
//			Hashtable a = net.getPriorSample();
//			if(a.get("id0")==Boolean.FALSE && a.get("id1")==Boolean.TRUE){
//				i=false;
//				System.out.println("Campione generato: "+net.getComprensiveResult(a).toString());
//				System.out.println("Giri: "+c);
//			}
//			
//			c++;
//		}
		
		//System.out.println("Campione generato: "+net.getComprensiveResult(net.getPriorSample()).toString());
		
		
		//net.rejectionSample(X, evidence, numberOfSamples)
	}
}
