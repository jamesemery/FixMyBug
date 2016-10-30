package server.javaparser;

import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * Class providing basic functionality for calculating of levenshtein
 * distance between two strings evaluated on a per-token basis for each.
 *
 * NOTE: a higher score indicates a greater degree of similarity
 */
public class LevScorer {

    /**
     * Uses a Levenstein distance calculation algorith in order to clalculate
     * the edit distance between two strings
     * @param queryString
     * @param tokens
     * @return
     */
    public List<Integer> rankSimilarity(List<Token> queryString, List<List<Token>> tokens) {
        //Integer[][] scores = new int[queryString.size()][tokens]
        return null;
    }

    /**
     * Uses a Levenstein distance calculation algorithm in order to calculate
     * the edit distance between two lists of tokens
     * @param queryString
     * @param tokens
     * @return
     */
    //@Override
    public static int scoreSimilarity(List<Integer> queryString, List<Integer>
            tokens) {
        if (queryString.size() == 0 || tokens.size() == 0) return 0;

        int[][] scores = new int[queryString.size() + 1][tokens.size() + 1];
        for (int i = 1; i < scores.length; i++) {
            for (int j = 1; j < scores[0].length; j++) {
                int match = scores[i-1][j-1];
                if (queryString.get(i-1) == tokens.get(j-1)) match++;
                int left = scores[i-1][j];
                int right = scores [i][j-1];
                int max = Math.max(match, Math.max(left, right));
                scores[i][j] = max;
            }
        }
        return scores[scores.length-1][scores[0].length-1];
    }
}
