package server.javaparser;

//import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Class providing basic functionality for calculating of levenshtein
 * distance between two strings evaluated on a per-token basis for each.
 *
 * NOTE: a higher score indicates a greater degree of similarity
 */
public class LevScorer {
    private static int TOKEN_MATCH = 5;
    private static int IDENTIFIER_MATCH = 3;

    private static int LOCAL_TOKEN_MATCH = 5;
    private static int LOCAL_POSSIBLE_MATCH = 4;
    private static int LOCAL_IDENTIFIER_MATCH = 3;
    private static int LOCAL_MISMATCH = -3;
    private static int LOCAL_INDEL = -2;
    /**
     * Uses a Levenstein distance calculation algorith in order to clalculate
     * the edit distance between two strings
     * @param queryString
     * @param tokens
     * @return
     */
//    public List<Integer> rankSimilarity(List<Token> queryString, List<List<Token>> tokens) {
//        //Integer[][] scores = new int[queryString.size()][tokens]
//        return null;
//    }

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
                int match = scores[i - 1][j - 1];
                if (queryString.get(i - 1) == tokens.get(j - 1)) match += TOKEN_MATCH;
                else if (((queryString.get(i - 1) == 100) || (queryString.get(i - 1)
                        > 109)) && ((tokens.get(j - 1) == 100) || (tokens.get(i - 1)
                        > 109))) match += IDENTIFIER_MATCH;
                int left = scores[i - 1][j];
                int right = scores[i][j - 1];
                int max = Math.max(match, Math.max(left, right));
                scores[i][j] = max;
            }
        }
        return scores[scores.length - 1][scores[0].length - 1];
    }

    /**
     * Uses a Smith-Waterman approach to calculate the local best alignment between two sequences
     */
    public static int scoreSimilarityLocal(List<Integer> queryString, List<Integer> tokens) {
        if (queryString.size() == 0 || tokens.size() == 0) return 0;
        System.out.println("Alignming things\nUserCode = "+queryString+"\nErrCode = "+tokens);

        int[][] scores = new int[queryString.size() + 1][tokens.size() + 1];

        for (int i = 1; i < scores.length; i++) {
            for (int j = 1; j < scores[0].length; j++) {
                int match = scores[i - 1][j - 1];

                // if they match
                if ((queryString.get(i - 1) == tokens.get(j - 1))) {
                    match += LOCAL_TOKEN_MATCH;

                    // if they were both identifiers
                } else if (((queryString.get(i - 1) == 100) || (queryString.get(i - 1)
                            > 109)) && ((tokens.get(j - 1) == 100) || (tokens.get(j - 1)
                            > 109))) match += LOCAL_IDENTIFIER_MATCH;
                else {
                    match += LOCAL_MISMATCH;
                }

                int left = scores[i - 1][j] + LOCAL_INDEL;
                int right = scores[i][j - 1] + LOCAL_INDEL;
                int max = Math.max(match, Math.max(left, right));

                //choose matches over other options
                if (max <= 0) {
                    max = 0;
                }
                scores[i][j] = max;
            }
        }

        // finding the best aligned subsection of the array to output
        int maxAlignment = 0;
        for (int x = 1; x<scores.length;x++) {
            for (int y = 1; y<scores[0].length;y++) {
                if (scores[x][y]>=maxAlignment) {
                    maxAlignment = scores[x][y];
                }
            }
        }
        return maxAlignment;
    }

}
