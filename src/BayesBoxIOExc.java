
public class BayesBoxIOExc extends Exception {

	private static final long serialVersionUID = -8603029922852087144L;
	
	private String msg;
	
	public BayesBoxIOExc(){
		
	}

	public BayesBoxIOExc(String msg){
		this.msg = msg; 
	}

	
	public String getError(){
		return msg;
	}

}
