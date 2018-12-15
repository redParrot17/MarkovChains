package markov;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Markov {

    private Matrix<String> matrix;

    /**
     * Creates a new {@link Markov} object ready to be trained with new text.
     */
    public Markov() {
        matrix = new Matrix<>();
    }

    /**
     * Creates a new {@link Markov} object with the specified {@link Matrix}.
     * @param matrix the {@link Matrix} to be used for text prediction.
     */
    public Markov(Matrix<String> matrix) {
        this.matrix = matrix;
    }

    /**
     * Returns a copy of the {@link Matrix} containing all the prediction and probability data.
     * @return the {@link Matrix} that contains all the prediction and probability data.
     */
    public Matrix<String> getMatrix() {
        return new Matrix<>(matrix);
    }

    /**
     * Generates a new random sentence from the data used during training.
     * @return the generated word sequence
     */
    public String generateSentence() {
        if (matrix.isEmpty())
            throw new IllegalStateException("Cannot generate a sentence before training data is submitted!");
        return generateWordSequence(matrix.getRandomPair());
    }

    /**
     * Generates a new random sentence from the data used during training.
     * @param startingWord the first two words used to start the sentence
     * @return the generated word sequence
     */
    public String generateSentence(String startingWord) {
        if (matrix.isEmpty())
            throw new IllegalStateException("Cannot generate a sentence before training data is submitted!");
        return generateWordSequence(startingWord);
    }

    private String generateWordSequence(String startingWord) {
        if (matrix.isEmpty())
            throw new IllegalStateException("Cannot generate a sentence before training data is submitted!");
        String last = startingWord;
        StringBuilder sb = new StringBuilder();
        sb.append(last);
        while (true) {
            String next = matrix.getNextWord(last);

            if (sb.indexOf(" ") != -1)
                last = (sb.substring(sb.lastIndexOf(" ")) + " " + next).trim();

            if (isEndCharacter(next)) {
                sb.append(next);
                break;
            } else if (isSpecial(next)) {
                sb.append(next);
            } else {
                sb.append(" ").append(next);
            }
        }
        return toTitleCase(sb.toString().trim());
    }

    /**
     * Incorporates the specified text into the prediction system.
     * @param text the text to be added
     */
    public void submit(String text) {
        try (Scanner scnr = new Scanner(text)) {
            train(scnr);
        }
    }

    /**
     * Incorporates the text within the specified file into the prediction system.
     * @param file the text file containing the text to be incorporated into the prediction system.
     * @throws FileNotFoundException if for some reason the file cannot be found
     */
    public void submit(File file) throws FileNotFoundException {
        try (Scanner scnr = new Scanner(file)) {
            train(scnr);
        }
    }

    private void train(Scanner scnr) {

        boolean isFirst = true;
        String first = "";
        String second = "";
        String third = "";

        while (true) {

            if (isFirst) {
                isFirst = false;
                if (scnr.hasNext()) second = scnr.next();
                else break;
                if (endsWithSpecial(second)) {
                    third = String.valueOf(second.charAt(second.length()-1));
                    second = clean(second);
                } else {
                    second = clean(second);
                    if (scnr.hasNext()) third = scnr.next();
                    else break;
                }
            } else {
                first = second;
                if (isSpecial(third)) second = third;
                else second = clean(third);
                if (endsWithSpecial(third) && !isSpecial(third))
                    third = String.valueOf(third.charAt(third.length()-1));
                else {
                    if (scnr.hasNext()) third = scnr.next();
                    else break;
                }
            }

            first = first.trim();
            second = second.trim();
            third = third.trim();

            String firstSet;
            if (isSpecial(second)) firstSet = (first+second).toLowerCase();
            else firstSet = (first+" "+second).toLowerCase();
            String secondSet = isSpecial(third) ? third : clean(third);

            matrix.addPair(firstSet, secondSet);
        }

        if (endsWithSpecial(third)) {
            first = second;
            second = clean(third);
            third = String.valueOf(third.charAt(third.length()-1));
            String firstSet;
            if (isSpecial(second)) firstSet = (first.trim()+second.trim()).toLowerCase();
            else firstSet = (first.trim()+" "+second.trim()).toLowerCase();
            String secondSet = isSpecial(third) ? third : clean(third);
            matrix.addPair(firstSet, secondSet);
        }

        matrix.updateProbabilities();
    }

    private boolean isEndCharacter(String text) {
        return text.endsWith(".") ||
                text.endsWith("?") ||
                text.endsWith("!");
    }

    private String clean(String word) {
        return word.replaceAll("[^a-zA-Z0-9']", "").trim().toLowerCase();
    }

    private boolean endsWithSpecial(String word) {
        return word.endsWith(".") || word.endsWith("!") || word.endsWith("?") || word.endsWith(":") || word.endsWith(";");
    }

    private boolean isSpecial(String word) {
        return word.equals(".") || word.equals("!") || word.equals("?") || word.equals(":") || word.equals(";");
    }

    private String toTitleCase(String string) {
        if (string.length() == 1) return String.valueOf(Character.toTitleCase(string.charAt(0)));
        return Character.toTitleCase(string.charAt(0))+string.substring(1);
    }

}
