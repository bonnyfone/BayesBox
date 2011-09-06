/**
 * Classe che rappresente le eccezioni che possono accadere durante la manipolazione ed il calcolo di reti di Bayes. 
 */
public class BayesBoxIOExc extends Exception {

	private static final long serialVersionUID = -8603029922852087144L;
	
	/** messaggio di errore */
	private String msg;
	
	/** Costruttore vuoto */
	public BayesBoxIOExc(){}

	/**
	 * Costruttore con messaggio di errore.
	 * @param msg il messaggio di errore.
	 */
	public BayesBoxIOExc(String msg){
		this.msg = msg; 
	}

	/**
	 * Restituisce l'errore.
	 * @return stringa con l'errore.
	 */
	public String getError(){
		return msg;
	}

}
