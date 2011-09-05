
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ravi Mohan
 * 
 */
public class BayesNetNode {
        private String variable;
        private String description;
        private String[] stateNames;

        List<BayesNetNode> parents, children;

        ProbabilityDistribution distribution;

        public ProbabilityDistribution getDistribution(){
        	return distribution;
        }
        
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
        
        //Aggiunto 
        public BayesNetNode(String variable, String descr){
        	this(variable);
        	setDescription(descr);
        }
        
        //Aggiunto 
        public BayesNetNode(String variable, String descr, String... states){
        	this(variable,descr);
        	setStateNames(states);
        }
        
                

        public void influencedBy(BayesNetNode parent1) {
                addParent(parent1);
                parent1.addChild(this);
                distribution = new ProbabilityDistribution(parent1.getVariable());
        }

        public void influencedBy(BayesNetNode parent1, BayesNetNode parent2) {
                influencedBy(parent1);
                influencedBy(parent2);
                distribution = new ProbabilityDistribution(parent1.getVariable(),
                                parent2.getVariable());
        }
        
        
        public void influencedBy(ArrayList<BayesNetNode> list){
        	String[] vars = new String[list.size()];
        	for(int i = 0;i<list.size();i++){
        		addParent(list.get(i));
        		list.get(i).addChild(this);
        		vars[i] = list.get(i).getVariable();
        	}
        	
        	distribution = new ProbabilityDistribution(vars);	
        }
        
        //Aggiunto: influenze multiple (> 2)
        public void influencedBy(BayesNetNode... parents){
        	String[] vars = new String[parents.length];
        	for(int i = 0;i<parents.length;i++){
        		addParent(parents[i]);
        		parents[i].addChild(this);
        		vars[i] = parents[i].getVariable();
        	}
        	
        	distribution = new ProbabilityDistribution(vars);
        }

        public void setProbability(boolean b, double d) {
                distribution.set(d, b);
                if (isRoot()) {
                        distribution.set(1.0 - d, !b);
                }
        }

        public void setProbability(boolean b, boolean c, double d) {
                distribution.set(d, b, c);

        }
        
        //Aggiunto: influenze multiple
        public void setProbability(double d, boolean... pi){
        	distribution.set(d, pi);
        }

        public String getVariable() {
                return variable;
        }

        public List<BayesNetNode> getChildren() {
                return children;
        }

        public List<BayesNetNode> getParents() {
                return parents;
        }

        @Override
        public String toString() {
                return variable;
        }

        public double probabilityOf(Map<String, Boolean> conditions) {
                return distribution.probabilityOf(conditions);
        }

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
        
    	public String getDistributionInfo(){
    		String ris="";
    		if(isRoot())ris="[Root node]\n";
    		return ris+getDistribution().toString();
    	}
        
        //Aggiunto
		public void setDescription(String description) {
			this.description = description;
		}

        //Aggiunto
		public String getDescription() {
			return description;
		}

        //
        // PRIVATE METHODS
        //
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

        public boolean isRoot() {
                return (parents.size() == 0);
        }

		public void setStateNames(String... stateNames) {
			this.stateNames = stateNames;
		}

		public String[] getStateNames() {   
			return stateNames;
		}

}
