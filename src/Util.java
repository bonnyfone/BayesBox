/**
 * Created on Aug 24, 2003 by Ravi Mohan
 * modified by Stefano Bonetta
 */

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class Util {
	
        public static final String NO = "No";

        public static final String YES = "Yes";

        private static Random r = new Random();

        /**
         * Ritorna la testa di una lista
         * @param <T> tipo
         * @param l lista
         * @return il primo elemento
         */
        public static <T> T first(List<T> l) {

                List<T> newList = new ArrayList<T>();
                for (T element : l) {
                        newList.add(element);
                }
                return newList.get(0);
        }

        /**
         * Ritorna la coda di una lista
         * @param <T> tipo
         * @param l lista
         * @return la coda (lista)
         */
        public static <T> List<T> rest(List<T> l) {
                List<T> newList = new ArrayList<T>();
                for (T element : l) {
                        newList.add(element);
                }
                newList.remove(0);
                return newList;
        }
        
        /**
         * Ritorna un booleano a caso.
         * @return valore booleano casuale
         */
        public static boolean randomBoolean() {
                int trueOrFalse = r.nextInt(2);
                return (!(trueOrFalse == 0));
        }

        /**
         * Normalizza una distr. di probabilità.
         * @param probDist una distr. di probabilità
         * @return un array con la distribuzione normalizzata.
         */
        public static double[] normalize(double[] probDist) {
                int len = probDist.length;
                double total = 0.0;
                for (double d : probDist) {
                        total = total + d;
                }

                double[] normalized = new double[len];
                if (total != 0) {
                        for (int i = 0; i < len; i++) {
                                normalized[i] = probDist[i] / total;
                        }
                }
                double totalN = 0.0;
                for (double d : normalized) {
                        totalN = totalN + d;
                }

                return normalized;
        }

        /**
         * Normalizza una distr. di probabilità.
         * @param values  una distr. di probabilità
         * @return un lista con la distribuzione normalizzata.
         */
        public static List<Double> normalize(List<Double> values) {
                double[] valuesAsArray = new double[values.size()];
                for (int i = 0; i < valuesAsArray.length; i++) {
                        valuesAsArray[i] = values.get(i);
                }
                double[] normalized = normalize(valuesAsArray);
                List<Double> results = new ArrayList<Double>();
                for (int i = 0; i < normalized.length; i++) {
                        results.add(normalized[i]);
                }
                return results;
        }

        public static int min(int i, int j) {
                return (i > j ? j : i);
        }

        public static int max(int i, int j) {
                return (i < j ? j : i);
        }

        public static int max(int i, int j, int k) {
                return max(max(i, j), k);
        }

        public static int min(int i, int j, int k) {
                return min(min(i, j), k);
        }

        /**
         * Ritorna un elemento random da una lista
         * @param <T> tipo
         * @param l lista
         * @return un elemento a caso della lista
         */
        public static <T> T selectRandomlyFromList(List<T> l) {
                int index = r.nextInt(l.size());
                return l.get(index);
        }

        /**
         * Ritorna la <i>moda</i> della distribuzione
         * @param <T> tipo
         * @param l lista (rappresenta una distribuzione)
         * @return l'elemento <i>moda</i>
         */
        public static <T> T mode(List<T> l) {
                Hashtable<T, Integer> hash = new Hashtable<T, Integer>();
                //conteggia elementi
                for (T obj : l) {
                        if (hash.containsKey(obj)) {
                                hash.put(obj, hash.get(obj).intValue() + 1);
                        } else {
                                hash.put(obj, 1);
                        }
                }

                T maxkey = hash.keySet().iterator().next();
                //individua l'elmento di maggior frequenza
                for (T key : hash.keySet()) {
                        if (hash.get(key) > hash.get(maxkey)) {
                                maxkey = key;
                        }
                }
                return maxkey;
        }

        public static String[] yesno() {
                return new String[] { YES, NO };
        }

        public static double log2(double d) {
                return Math.log(d) / Math.log(2);
        }
        
        /**
         * Calcola entropia 
         * @param probabilities distribuzione di probabilità
         * @return informazione entropica
         */
        public static double information(double[] probabilities) {
                double total = 0.0;
                for (double d : probabilities) {
                        total += (-1.0 * log2(d) * d);
                }
                return total;
        }

        /**
         * Costruisce una lista eliminando un elemento da un'altra
         * @param <T> tipo 
         * @param list lista
         * @param member elemento da eliminare nella lista di ritorno
         * @return una lista uguale a quella passata senza l'elemento specificato
         */
        public static <T> List<T> removeFrom(List<T> list, T member) {
                List<T> newList = new ArrayList<T>();
                for (T s : list) {
                        if (!(s.equals(member))) {
                                newList.add(s);
                        }
                }
                return newList;
        }

        /**
         * Ritorna il valore della somma dei quadrati degli elementi 
         * @param <T> tipo 
         * @param list lista elementi
         * @return somma dei quadrati
         */
        public static <T extends Number> double sumOfSquares(List<T> list) {
                double accum = 0;
                for (T item : list) {
                        accum = accum + (item.doubleValue() * item.doubleValue());
                }
                return accum;
        }


        /**
         * Replica una stringa n volte
         * @param s stringa da ripetere
         * @param n
         * @return una stringa che rappresenta la ripetizione di n volte della stringa in input
         */
        public static String ntimes(String s, int n) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < n; i++) {
                        buf.append(s);
                }
                return buf.toString();
        }

        /**
         * Controlla la consistenza di un numero double
         * @param d
         */
        public static void checkForNanOrInfinity(double d) {
                if (Double.isNaN(d)) {
                        throw new RuntimeException("Not a Number");
                }
                if (Double.isInfinite(d)) {
                        throw new RuntimeException("Infinite Number");
                }
        }
        
 

}
