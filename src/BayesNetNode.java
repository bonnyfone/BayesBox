import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Classe che rappresenta un nodo della rete Bayesiana.
  * <br><i>(alcune idee e parti del codice appartengono a Ravi Mohan - MIT)</i>
 */
public class BayesNetNode {
	
		/** id del nodo/variabile */
        private String variable;
        
        /** descrizione del nodo */
        private String description;
        
        /** nomi degli stati */
        private String[] stateNames;
        //per ora consideriamo solo classificazioni binarie true/false

        /** lista dei padri */
        private List<BayesNetNode> parents;
        
        /** lista dei figli */
        private List<BayesNetNode> children;

        /** CPT del nodo */
        private ProbabilityDistribution distribution;

        /** Metodo che ritorna la CPT del nodo */
        public ProbabilityDistribution getDistribution(){
        	return distribution;
        }
        
        /**
         * Costruttore del nodo.
         * @param variable ID variabile
         */
        public BayesNetNode(String variable) {
                this.variable = variable;
                parents = new ArrayList<BayesNetNode>();
                children = new ArrayList<BayesNetNode>();
                distribution = new ProbabilityDistribution(variable);
               
                //----Aggiunti------ 
                description="bool";
                stateNames = new String[2];
                stateNames[0] = "true";
                stateNames[1] = "false";
                //---------------------
        }
        
        /**
         * Costruttore del nodo.
         * @param variable ID variabile
         * @param descr descrizione variabile
         */
        public BayesNetNode(String variable, String descr){
        	this(variable);
        	setDescription(descr);
        }
        
        /**
         * Costruttore del nodo
         * @param variable ID variabile
         * @param descr descrizione variabile
         * @param states elenco degli stati 
         */
        public BayesNetNode(String variable, String descr, String... states){
        	this(variable,descr);
        	setStateNames(states);
        }
        

        /**
         * Metodo che stabilisce la dipendenza del nodo corrente da un altro nodo.
         * @param parent1 nodo da cui dipendere
         */
        public void influencedBy(BayesNetNode parent1) {
                addParent(parent1);
                parent1.addChild(this);
                distribution = new ProbabilityDistribution(parent1.getVariable());
        }

        /**
         * Metodo che stabilisce la dipendenza del nodo corrente da altri nodi.
         * @param parent1 primo nodo da cui dipendere
         * @param parent2 secondo nodo da cui dipendere
         */
        public void influencedBy(BayesNetNode parent1, BayesNetNode parent2) {
                influencedBy(parent1);
                influencedBy(parent2);
                distribution = new ProbabilityDistribution(parent1.getVariable(),
                                parent2.getVariable());
        }
        
        /**
         * Metodo che stabilisce la dipendenza del nodo corrente da altri nodi.
         * @param list lista di nodi da cui dipendere.
         */
        public void influencedBy(ArrayList<BayesNetNode> list){
        	String[] vars = new String[list.size()];
        	for(int i = 0;i<list.size();i++){
        		addParent(list.get(i));
        		list.get(i).addChild(this);
        		vars[i] = list.get(i).getVariable();
        	}
        	
        	distribution = new ProbabilityDistribution(vars);	
        }
        
        
        /**
         * Metodo che stabilisce la dipendenza del nodo corrente da altri nodi.
         * @param parents lista di nodi da cui dipendere.
         */
        public void influencedBy(BayesNetNode... parents){
        	String[] vars = new String[parents.length];
        	for(int i = 0;i<parents.length;i++){
        		addParent(parents[i]);
        		parents[i].addChild(this);
        		vars[i] = parents[i].getVariable();
        	}
        	
        	distribution = new ProbabilityDistribution(vars);
        }

        /**
         * Imposta la probabilità dell'evento del nodo.
         * <i>NB:Se il nodo è radice, si considera l'evento atomico.
         * Se il nodo ha delle dipendenze, il valore dello stato identifica la configurazione del padre.</i>
         * @param b stato
         * @param d probabilità
         */
        public void setProbability(boolean b, double d) {
                distribution.set(d, b);
                if (isRoot()) {
                        distribution.set(1.0 - d, !b);
                }
        }

//        public void setProbability(boolean b, boolean c, double d) {
//                distribution.set(d, b, c);
//
//        }
        
        /**
         * Imposta la probabilità dell'evento del nodo.
         * Il valore dello stato identifica la configurazione dei padri.</i>
         * @param bi configurazione dei padri
         * @param d probabilità
         */
        public void setProbability(double d, boolean... pi){
        	distribution.set(d, pi);
        }

        
        /**
         * Ritorna la variabile (ID) del nodo.
         * @return Stringa con l'ID del nodo
         */
        public String getVariable() {
                return variable;
        }

        
        /**
         * Ritorna la lista dei figli del nodo.
         * @return lista di nodi.
         */
        public List<BayesNetNode> getChildren() {
                return children;
        }
        

        /**
         * Ritorna la lista dei padri del nodo.
         * @return lista di nodi.
         */
        public List<BayesNetNode> getParents() {
                return parents;
        }


        /**
         * Ritorna la probabilità della configurazione di eventi passata come parametro.
         * @param conditions configurazione di nodi
         * @return probabilità della configurazione
         */
        public double probabilityOf(Map<String, Boolean> conditions) {
                return distribution.probabilityOf(conditions);
        }

        
        /**
         * Metodo che rappresenta l'atto di decisione (return true/false) in relazione all'evento casuale generato e 
         * alla probabilità della configurazione passata come parametro. 
         * @param probability probabilità generata casualmente, da confrontare con la probabilità degli eventi.
         * @param modelBuiltUpSoFar modello degli eventi.
         * @return
         */
        public Boolean isTrueFor(double probability, Map<String, Boolean> modelBuiltUpSoFar) {
                HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
                if (isRoot()) {
                		//System.out.println(getVariable() + " is root!");
                        conditions.put(getVariable(), Boolean.TRUE);
                } else {
            
                	for (int i = 0; i < parents.size(); i++) {
                                BayesNetNode parent = parents.get(i);
                                conditions.put(parent.getVariable(), modelBuiltUpSoFar.get(parent.getVariable()));
                        }
                }
                double trueProbability = probabilityOf(conditions);
                //System.out.println("Probability: "+probability + " - TrueProb: "+trueProbability);
                if (probability <= trueProbability) {
                        return Boolean.TRUE;
                } else {
                        return Boolean.FALSE;
                }
        }

        /**
         * Stabilisce se il nodo è radice.
         * @return
         */
        public boolean isRoot() {
                return (parents.size() == 0);
        }
        
        /**
         * Ritorna una rappresentazione testuale della CPT del nodo.
         * @return stringa che rappresenta la CPT del nodo.
         */
    	public String getDistributionInfo(){
    		String ris="";
    		if(isRoot())ris="[Root node]\n";
    		return ris+getDistribution().toString();
    	}
    	
    	
    	
    	
    	
    	
    	//-------------------------------------------------------
    	//***************** vari Set & Get **********************
    	//-------------------------------------------------------
    	
    	
		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

        private void addParent(BayesNetNode node) {
                if (!(parents.contains(node))) {
                        parents.add(node);
                }
        }

        private void addChild(BayesNetNode node) {
                if (!(children.contains(node))) {
                        children.add(node);
                }
        }


		public void setStateNames(String... stateNames) {
			this.stateNames = stateNames;
		}

		public String[] getStateNames() {   
			return stateNames;
		}
		
        @Override
        public boolean equals(Object o) {

                if (this == o) {
                        return true;
                }
                if ((o == null) || (this.getClass() != o.getClass())) {
                        return false;
                }
                BayesNetNode another = (BayesNetNode) o;
                return variable.equals(another.variable);
        }
		
        @Override
        public String toString() {
                return variable;
        }

}
