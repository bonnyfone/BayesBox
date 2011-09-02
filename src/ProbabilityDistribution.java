import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ravi Mohan
 * modified by Stefano Bonetta
 */
public class ProbabilityDistribution {
	/** Righe della tabella della Probabilità congiunta */
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
					"Invalid number of values ("+values.length+"), must = # of Random Variables in distribution:"
							+ variableNames.size());
		}
		rows.add(new Row(probability, values));
	}

	public double probabilityOf(String variableName, boolean b) {
		HashMap<String, Boolean> h = new HashMap<String, Boolean>();
		h.put(variableName, b);
		return probabilityOf(h);
	}

	public double probabilityOf(Map<String, Boolean> conditions) {
		double prob = 0.0;
		for (Row row : rows) {
			boolean rowMeetsAllConditions = true;
			for (Map.Entry<String, Boolean> c : conditions.entrySet()) {
				if (!(row.matches(c.getKey(), c.getValue()))) {
					rowMeetsAllConditions = false;
					break;
				}
			}
			if (rowMeetsAllConditions) {
				prob += row.probability;
			}
		}

		return prob;
	}

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