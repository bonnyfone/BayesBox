package bayesbox.input;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import bayesbox.logic.BayesNet;
import bayesbox.logic.BayesNetNode;
import bayesbox.util.Util;


 
/**
 * Classe che rappresenta il loader per reti di bayes caricate da file XML.
 */
public class BayesBox {

	/** documento XML sorgente */
	private Document BayesXML;

	/** Costruttore base */
	public BayesBox(){}
	
	/** modalita' verbosa */
	private static boolean VERBOSE=false;
	
	/** imposta la modalita di log */
	public static void setVerbose(boolean verbose){
		VERBOSE = verbose;
	}

	public static void LOG(String msg){
		if(VERBOSE)System.out.println(msg);
	}
	
	/**
	 * Costruttore che crea rete di bayes da file XML.
	 * @param path del file XML.
	 */
	public BayesBox(String path){
		try {
			loadFromFile(path);
		} catch (BayesBoxIOExc e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Metodo che ritorna l'oggetto documento del file XML.
	 * @return
	 */
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
			
		} catch (ParserConfigurationException e) {throw new BayesBoxIOExc("Parsing error."+e.getMessage());} 
		  catch (SAXException e) {throw new BayesBoxIOExc("SAX error."+e.getMessage());} 
		  catch (IOException e) {throw new BayesBoxIOExc("I/O error."+e.getMessage());}
	}

	/**
	 * Metodo che ritorna la rete di Bayes associata al loader.
	 * @return la rete di Bayes letta.
	 */
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
				BayesBox.LOG("NODE name: " + tmpNode.getVariable()       + "\n" +
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
						BayesBox.LOG("Node find: "+child.item(i).getAttributes().getNamedItem("NAME").getTextContent()+" alias " +currentNode.getDescription());
					}
					else if(val.equals("CONDSET")){
						NodeList subchild = child.item(i).getChildNodes();
						for(int j=0;j< subchild.getLength();j++){
							String subval = subchild.item(j).getNodeName();
							
							if(subval.equals("CONDELEM")){ //l'identificativo del nodo di cui stiamo per leggere le dipendenze
								dependingNodes.add(getBayesNetNodeById(tmpNodes, subchild.item(j).getAttributes().getNamedItem("NAME").getTextContent()+""));
								BayesBox.LOG("DependingNode find: "+subchild.item(j).getAttributes().getNamedItem("NAME").getTextContent()+" alias "+dependingNodes.get(dependingNodes.size()-1).getDescription());
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
								//BayesBox.LOG("Value found: "+tmpVal);
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
				BayesBox.LOG("Distribution:\n"+currentNode.getDistributionInfo());
			}
			
		}  
		
		ArrayList<BayesNetNode> roots = new ArrayList<BayesNetNode>();
		
		for(int i=0;i<tmpNodes.size();i++)
			if(tmpNodes.get(i).isRoot())roots.add(tmpNodes.get(i));
		
		//Costruisce la rete di Bayes e calcola una lista dei nodi in ordine consistente.
		BayesNet ris = new BayesNet(roots);
		ris.getVariables();
		return ris;
	}
	
	/**
	 * Metodo interno per aggiornare la CPT di un nodo
	 */
	private void inflateDistribution(BayesNetNode node, ArrayList<String> values){
		for(int i=0;i<values.size();i++){
			node.setProbability(Double.parseDouble(values.get(i)), getBooleanConfiguration(values.size(),i));
		}
	}
	
	/**
	 * Metodo interno che ritorna una array di booleani prefabbricato con l'enumerazione di una delle varie 
	 * configurazioni binarie possibili. Utilizzato assieme ad inflateDistribution per aggiornare le CPT dei nodi in fase
	 * di costruzione della rete.
	 */
	private boolean[] getBooleanConfiguration(int base, int pos){
		int size = base;//(int) Math.pow(2, base);
		int dim = (int) Util.log2(base);
		boolean ris[] = new boolean[dim];
		int targetBin = size-1-pos;
		String conf = Integer.toBinaryString(targetBin);
		while(conf.length()<dim)conf = "0"+conf;
		//BayesBox.LOG("Binario: "+conf);
		for(int i=0;i<ris.length;i++){
			if(conf.charAt(i) == '0')
				ris[i] = false;
			else 
				ris[i] = true;
		}
		return ris;
	}
	
	/**
	 * Estrae il nodo della rete con uno specifico id. <i>NB: il metodo si trova qui perchè viene usato MENTRE si sta creando
	 * la rete di bayes letta da XML, non possiamo quindi affidarci a metodi propri della classe BayesNet in quanto la rete non 
	 * è ancora disponibile.</i>.
	 * @param list lista di nodi
	 * @param id id del nodo
	 * @return il nodo cercato
	 */
	private BayesNetNode getBayesNetNodeById(ArrayList<BayesNetNode> list,String id){
		
		for(int i=0;i<list.size();i++){
			if(list.get(i).getVariable().equals(id))return list.get(i);
		}
		return null;
		
	} 

	
	///DEBUG///
	public static void main(String argv[]) {
		BayesBox bay = new BayesBox("/home/ziby/Scrivania/test_bayes/asia.xml");
		BayesNet net = bay.getBayesNet();
		
		//BayesBox.LOG(net.getPriorSample());
		//BayesBox.LOG(net.getVariables().toString());

		Hashtable<String, Boolean> evidence = new Hashtable<String, Boolean>();
//		String var = "label1";
		String var = "DYSPNOEA";
//		String var = "out";
//		String var = "label4";
//		evidence.put("node_7", true);
//		evidence.put("node_8", false);
//		evidence.put("smk", true);
//		evidence.put("pxr", false);
//		evidence.put("monte1", false);
//		evidence.put("monte2", true);
//		evidence.put("monte3", false);
//		evidence.put("monte4", true);
//		evidence.put("middle1", true);
//	    evidence.put("middle2", false);
//		evidence.put("middle3", true);
//		evidence.put("label3", true);
//		evidence.put("label2", true);
//		evidence.put("SMOKING", true);
		evidence.put("BRONCHITIS", true);
		evidence.put("POSITIVE_X_RAY", true);
//		evidence.put("Watson", true);
		Hashtable< String, Boolean> evidence2=BayesNet.cloneEvidenceVariables(evidence);
		Hashtable< String, Boolean> evidence3=BayesNet.cloneEvidenceVariables(evidence);
		  
		//TEST di enumerationAsk  ********************************************************************************
		BayesBox.LOG("\n---------------------");
		BayesBox.LOG(">>>> Enumeration Ask");
		long endTime,startTime;
		startTime = System.currentTimeMillis();
		//double[] ris =net.enumerationAsk(var, evidence);
		double[] ris;
		try {
			ris = net.enumerationAsk(var, evidence,BayesNet.MODE_DESCRIPTION);
			endTime = System.currentTimeMillis();
	 		BayesBox.LOG(ris[0]+" , " + ris[1] );	
			 BayesBox.LOG("Total elapsed time: "+ (endTime-startTime)+"\n---------------------");
		} catch (BayesBoxIOExc e) {
			BayesBox.LOG(e.getError());
		
		}

		//if(true)return; //FORZA FINE
		 
		 
		//TEST di LIKELIHOODWEIGHTING *****************************************************************************
 		BayesBox.LOG(">>>> Likelihood");
 		startTime = System.currentTimeMillis();
 		
 		double[] ris2;
		try {
			ris2 = net.likelihoodWeighting(var, evidence2, 1700, BayesNet.MODE_DESCRIPTION);
			endTime = System.currentTimeMillis();
			BayesBox.LOG(ris2[0]+" , " + ris2[1] );
			 BayesBox.LOG("Total elapsed time: "+ (endTime-startTime)+"\n---------------------");
		} catch (BayesBoxIOExc e) {
			BayesBox.LOG(e.getError());
		}
		
		  
		//TEST di REJECTIONSAMPLING		 
		BayesBox.LOG(">>>> Rejectionsampling");
	 	startTime = System.currentTimeMillis();
	 	double[] ris3;
		try {
			ris3 = net.rejectionSample(var, evidence3, 5000, BayesNet.MODE_DESCRIPTION);
			endTime = System.currentTimeMillis();
			BayesBox.LOG(ris3[0]+" , " + ris3[1] + " (campioni consistenti: "+(int)ris3[2]+", "+ris3[3]+"%)");
			 BayesBox.LOG("Total elapsed time: "+ (endTime-startTime)+"\n---------------------");
		} catch (BayesBoxIOExc e) {
			// TODO Auto-generated catch block
			BayesBox.LOG(e.getError());
		}

			  		 
//		//TEST di REJECTIONSAMPLING
//		double[] ris2 = net.rejectionSample("id1", evidence, 100);
//		BayesBox.LOG(ris2[0]+" , " + ris2[1] + " (campioni consistenti: "+(int)ris2[2]+", "+ris2[3]+"%)");

//		boolean i = true;
//		int c =0;
//		while(i){
//			
//			Hashtable a = net.getPriorSample();
//			if(a.get("id0")==Boolean.FALSE && a.get("id1")==Boolean.TRUE){
//				i=false;
//				BayesBox.LOG("Campione generato: "+net.getComprensiveResult(a).toString());
//				BayesBox.LOG("Giri: "+c);
//			}
//			
//			c++;
//		}
		
		//BayesBox.LOG("Campione generato: "+net.getComprensiveResult(net.getPriorSample()).toString());
		
		
		//net.rejectionSample(X, evidence, numberOfSamples)
	}
}
