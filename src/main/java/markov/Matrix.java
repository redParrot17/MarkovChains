package markov;

import java.util.*;

/**
 * @param <T> the data type this Matrix is to keep track of
 * @since 7
 */
public class Matrix<T> {

    private boolean isEmpty;
    private HashSet<T> uniqueWords;
    private HashMap<T, HashMap<T, Integer>> matches;
    private HashMap<T, HashMap<T, Double>> probabilities;

    /**
     * Creates a new {@link Matrix} ready to be loaded with word pairs.
     */
    public Matrix() {
        isEmpty = true;
        uniqueWords = new HashSet<>();
        matches = new HashMap<>();
        probabilities = new HashMap<>();
    }

    /**
     * Creates a copy of the specified {@link Matrix}.
     * @param other the {@link Matrix} to be copied.
     */
    public Matrix(Matrix<T> other) {
        this.isEmpty = other.isEmpty;
        this.uniqueWords = new HashSet<>(other.uniqueWords);
        this.matches = new HashMap<>();
        for (T key : other.matches.keySet()) {
            this.matches.put(key, new HashMap<>(other.matches.get(key)));
        }
        this.probabilities = new HashMap<>();
        for (T key : other.probabilities.keySet()) {
            this.probabilities.put(key, new HashMap<>(other.probabilities.get(key)));
        }
    }

    /**
     * @return true if the Matrix contains no elements
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * Loads the specified element pair
     * @param first  first element of the pair
     * @param second second element of the pair
     */
    public void addPair(T first, T second) {
        uniqueWords.add(second);
        if (matches.containsKey(first)) {
            HashMap<T, Integer> pairs = matches.get(first);
            if (pairs.containsKey(second))
                pairs.put(second, pairs.get(second)+1);
            else pairs.put(second, 1);
        } else {
            HashMap<T, Integer> pairs = new HashMap<>();
            pairs.put(second, 1);
            matches.put(first, pairs);
        }
        isEmpty = false;
    }

    public T getNextWord(T word) {
        if (isEmpty) throw new IllegalStateException("Cannot predict next word from an empty Matrix!");
        if (probabilities.containsKey(word)) {
            double p = Math.random();
            double cumulativeProbability = 0.0;
            for (Map.Entry<T, Double> entry : probabilities.get(word).entrySet()) {
                cumulativeProbability += entry.getValue();
                if (p <= cumulativeProbability) {
                    return entry.getKey();
                }
            }
        }
        return getRandomElement();
    }

    private double getPairProbability(T first, T second) {
        if (!matches.containsKey(first)) return 0.0;
        if (!matches.get(first).containsKey(second)) return 0.0;
        int pairs = 0;
        for (int count : matches.get(first).values()) {
            pairs += count;
        }
        double result = matches.get(first).get(second) / (pairs*1.0);
        return Double.isNaN(result) ? 0.0 : result;
    }

    public void updateProbabilities() {
        if (isEmpty) throw new IllegalStateException("Cannot update probabilities of an empty Matrix!");
        probabilities.clear();
        for (T first : matches.keySet()) {
            for (T second : matches.get(first).keySet()) {
                double prob = getPairProbability(first, second);
                if (!probabilities.containsKey(first)) {
                    HashMap<T, Double> subList = new HashMap<>();
                    subList.put(second, prob);
                    probabilities.put(first, subList);
                } else {
                    probabilities.get(first).put(second, prob);
                }
            }
        }
    }

    public T getRandomElement() {
        if (isEmpty) throw new NoSuchElementException("Cannot fetch a random element from an empty Matrix!");
        Random generator = new Random();
        Object[] keys = uniqueWords.toArray();
        return (T) keys[generator.nextInt(keys.length)];
    }

    public T getRandomPair() {
        if (isEmpty) throw new NoSuchElementException("Cannot fetch a random element from an empty Matrix!");
        Random generator = new Random();
        Object[] keys = matches.keySet().toArray();
        return (T) keys[generator.nextInt(keys.length)];
    }

}
