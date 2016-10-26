package carleton.comps;

import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class with generic functionality for Ngram scoring algorithms for
 * evaluating two strings or token streams. Can be fed either strings of code
 * in the language, or token streams and will currently return a number
 * corresponding to the total number of matched n-length subsequences
 */
public class NgramScorer implements SimilarityScorer {
    static final int DEFAULT_N = 5;

    @Override
    public List<Integer> rankSimilarity(List<Token> queryString,
                                         List<List<Token>> strings) {
//        for (List<TOkens>)
//        // Looping through the other string adding everything to a set
//        for (int i = n; i <= otherString.size(); i++) {
//            String s = "";
//            for (int j = i - n ; j < i; j++) {
//                s += otherString.get(j).getType();
//            }
//            tokenSet.add(s);
//        }
        return null;
    }

    //@Override
    public static double scoreSimilarity(String queryString, String
            otherString) {
        try {
            return scoreSimilarity(new TokenizerBuilder(queryString,
                            "String").getTokens(),
                    new TokenizerBuilder(otherString, "String").getTokens(),
                    DEFAULT_N);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //@Override
    public static double scoreSimilarity(List<Token> queryString, List<Token>
            otherString) {
        return scoreSimilarity(queryString, otherString, DEFAULT_N);
    }

    /**
     * Performs an evaluation of the similarity of the two token sequences by
     * counting the number of equivalent n length subsequences of tokens they
     * share.
     * @param queryString
     * @param otherString
     * @param n
     * @return
     */
    public static double scoreSimilarity(List<Token> queryString, List<Token>
            otherString, int n) {
        Set<String> tokenSet = new HashSet<>();
        if (queryString.size()<n || otherString.size()<n) {
            return 0;
        }

        // Looping through the other string adding everything to a set
        for (int i = n; i <= otherString.size(); i++) {
            String s = "";
            for (int j = i - n ; j < i; j++) {
                s += otherString.get(j).getType();
            }
            tokenSet.add(s);
        }

        // Looping through the query string looking for what IS in the set
        double output = 0;
        for (int i = n; i <= queryString.size(); i++) {
            // TODO use the tokenizer builder for this

            String s = "";
            for (int j = i - n ; j < i; j++) {
                s += queryString.get(j).getType();
            }
            if (tokenSet.contains(s)) output += 1;
        }
        return output;
    }
}
