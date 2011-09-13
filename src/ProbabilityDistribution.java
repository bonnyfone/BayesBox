import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe che rappresenta una distribuzione di proabilità.
 * <br><i>(alcune idee e parti del codice appartengono a Ravi Mohan - MIT)</i>
 */
public class ProbabilityDistribution {
	
	/** Righe della CPT */
	private List<Row> rows = new ArrayList<Row>();
	
	/** Mappa <VariableName:DistributionIndex> */
	private LinkedHashMap<String, Integer> variableNames = new LinkedHashMap<String, Integer>();
	
	/**
	 * Costruttore di una distribuzione di probabilità
	 * @param vNames nomi variabili
	 */
	public ProbabilityDistribution(String... vNames) {
		for (int i = 0; i < vNames.length; i++) {
			variableNames.put(vNames[i], i);
		}
	}

	/**
	 * Imposta il valore di probabilità per una data configurazione delle variabili (intese come sequenza di valori
	 * booleani 
	 * @param probability valore della probabilità per questa configurazione
	 * @param values configurazione delle variabili
	 */
	public void set(double probability, boolean... values) {
		if (values.length != variableNames.size()) {
			throw new IllegalArgumentException(
					"Numero non valido di valori ("+values.length+"), deve essere = # di var casuali della distribuzione:"
							+ variableNames.size());
		}
		rows.add(new Row(probability, values));
	}

	/**
	 * Metodo che ritorna la probabilità dell'evento del nodo.
	 * @param variableName id dell'evento
	 * @param b valore aspettato
	 * @return probabilità dell'evento
	 */
	public double probabilityOf(String variableName, boolean b) {
		HashMap<String, Boolean> h = new HashMap<String, Boolean>();
		h.put(variableName, b);
		return probabilityOf(h);
	}

	
	public double probabilityOf(Map<String, Boolean> conditions) {
        double prob = 0.0;
        for (Row row : rows) {
                Iterator<String> iter = conditions.keySet().iterator();
                boolean rowMeetsAllConditions = true;
                while (iter.hasNext()) {
                        String variable = (String) iter.next();
                        boolean value = ((Boolean) conditions.get(variable))
                                        .booleanValue();
                        if (!(row.matches(variable, value))) {
                                rowMeetsAllConditions = false;
                                break;
                                // return false;
                        }
                }
                if (rowMeetsAllConditions) {
                        prob += row.probability;
                }
        }

        return prob;
}

	
	
	/**
	 * Metodo che ritorna la probabilità della data configurazione
	 * @param conditions configurazione degli eventi
	 * @return probabilità dell'evento
	 */
//	public double probabilityOf(Map<String, Boolean> conditions) {
//		double prob = -1.0;
//		boolean sentinel=true;
//		for (Row row : rows) {
//			boolean rowMeetsAllConditions = true;
//			if(conditions==null)System.out.println("conditions null");
//			for (Map.Entry<String, Boolean> c : conditions.entrySet()) {
//				if (!(row.matches(c.getKey(), c.getValue()))) {
//					rowMeetsAllConditions = false;
//					break;
//				}
//			}
//			if (rowMeetsAllConditions) {
//				if(sentinel){ prob=0.0;sentinel=false; }
//				prob += row.probability;
//			}else{
//				//<-------- QUI pescare i casi in cui non rispetta TUTTE le condizioni
//			}
//		}
//		//if(prob==-1)System.out.println("prob = -1");
//		//System.out.println(conditions.toString()+" -> "+prob);
//		return prob;
//	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Row row : rows) {
			b.append(row.toString() + "\n");
		}

		return b.toString();
	}



	/**
	 * Classe interna che rappresenta una riga della tabella.
	 */
	class Row {
		/** valore di probabiltà per la configurazione corrente della riga */
		private double probability;
		
		/** configurazione della riga */
		private boolean[] values;

		/**
		 * Costruttore di una riga
		 * @param probability valore di probabilità per la configurazione corrente
		 * @param vals configurazione delle variabili
		 */
		Row(double probability, boolean... vals) {
			this.probability = probability;
			values = new boolean[vals.length];
			System.arraycopy(vals, 0, values, 0, vals.length); //fast array copy
		}
		/**
		 * Accede al valore della specifica variabile e controlla se metcha con il valore boolean passato.
		 * @param vName variabile
		 * @param value valore da controllare
		 * @return match
		 */
		public boolean matches(String vName, boolean value) {
			boolean rVal = false;
			Integer idx = variableNames.get(vName);
			if (null != idx) {
				rVal = values[idx] == value;
			}
			return rVal;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append("[");
			boolean first = true;
			for (Map.Entry<String, Integer> v : variableNames.entrySet()) {
				if (first) {
					first = false;
				} else {
					b.append(", ");
				}
				b.append(v.getKey());
				b.append("=");
				b.append(values[v.getValue()]);
			}
			b.append("]");
			b.append(" => ");
			b.append(probability);

			return b.toString();
		}
	}
}