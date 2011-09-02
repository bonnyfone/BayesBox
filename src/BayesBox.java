import java.util.Vector;


public class BayesBox {

	private Vector<BayesNode> net;
	
	
	public BayesBox(){
		net = new Vector<BayesNode>();
	}
	
	
	public void addNode(BayesNode node){
		net.add(node);
	}
	

}




class BayesNode{
	
	private String varName;
	
	public BayesNode(){
		
	}
	
}
