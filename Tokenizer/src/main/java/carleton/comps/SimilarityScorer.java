package carleton.comps;


import org.antlr.v4.runtime.Token;

import java.util.List;

public interface SimilarityScorer {

    List<Integer> rankSimilarity(List<EdiToken> queryString,
                                        List<List<EdiToken>> tokens);

    /**
     * Returns a double value corresponding to the ranking between two stirngs
     * @param queryString
     * @param otherString
     * @return
     */
    static double scoreSimilarity(List<EdiToken> queryString, List<EdiToken>
            otherString) {return 0;}
}
