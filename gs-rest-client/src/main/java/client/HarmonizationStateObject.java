package client;

import client.Tokenizer.EdiToken;
import client.Tokenizer.TokenizerBuilder;
import client.Tokenizer.javaparser.JavaParser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * class for storing all those FUCKING bits of state information that have to be passed arround
 * between step
 */
public class HarmonizationStateObject {
    private static int TOKEN_MATCH = 5;
    private static int IDENTIFIER_MATCH = 2;
    private static int POSSIBLE_MATCH = 4;


    private TokenizerBuilder tokenizedCode;
    private String originalCode;
    private int startLine;
    private int endLine;

    // Methods

    public TokenizerBuilder getTokenizedCode() {
        return tokenizedCode;
    }

    public void setTokenizedCode(TokenizerBuilder tokenizedCode) {
        this.tokenizedCode = tokenizedCode;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public HarmonizationStateObject(TokenizerBuilder tokenBuilder, String wholeFileCode, int startLine, int endLine) {
        this.tokenizedCode = tokenBuilder;
        this.originalCode = wholeFileCode;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    /**
     * IMPORTANT, THIS METHOD IS UNDER ACTIVE DEVELOPMENT
     *
     * @param e Database entry object onto which harmonization gets done
     * @return
     */
    public String harmonize(DatabaseEntry e) {
        List<EdiToken> userCode = tokenizedCode.betweenLines(startLine, endLine);
        List<Integer> userAssignments = TokenizerBuilder
            .generateDisambiguationList(tokenizedCode.getTokens());

        List<Integer > buggyCode = Arrays.asList(e.getBuggyCode().split(" ")).stream().map
                (Integer::parseInt).collect(Collectors.toList());
        List<Integer > buggyCodeAssignments = Arrays.asList(e.getBuggyCodeAssignments().split(" ")).stream()
                .map(Integer::parseInt).collect(Collectors.toList());

        List<Integer > fixedCode = Arrays.asList(e.getFixedCode().split(" ")).stream().map
                (Integer::parseInt).collect(Collectors.toList());
        List<Integer > fixedCodeAssignments = Arrays.asList(e.getFixedCodeAssignments().split(" ")).stream()
                .map(Integer::parseInt).collect(Collectors.toList());

        HarmonizationAlignment userToBugAlignment = new HarmonizationAlignment(userCode.stream().map(tok
                -> tok.getType()).collect(Collectors.toList()), userAssignments, buggyCode, buggyCodeAssignments);
        //TODO maybe not
        HarmonizationAlignment bugToFixAlignment = new HarmonizationAlignment(buggyCode,
                userAssignments, fixedCode, fixedCodeAssignments);

        // Holds the detokenized, tokenized code.
        StringBuilder builder = new StringBuilder();

        // Mapping containing the actual string values for the used ambiguous tokens
        Map<Integer,String> fixTokenMappedString = new HashMap<>();

        int previousToken = 0;

        for (int i = 0; i<fixedCode.size(); i++) {
            int token = fixedCode.get(i);
            //Add whatever spacing needs to be added to the previous stuff
            sanitize(builder, token, previousToken);
            previousToken = token;

            // If its not ambiguous just print as-is
            if (!TokenizerBuilder.isAmbiguousToken(token)) {
                builder.append(JavaParser.VOCABULARY.getLiteralName(token));
            }

            else {
                Set<Integer> mappedAssignments = userToBugAlignment.getPossibleMappings
                        (fixedCodeAssignments.get(i));
                try {
                    builder.append(getName(token, mappedAssignments,userCode,userAssignments,
                            fixTokenMappedString));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

        return builder.toString()+"\n";
    }

    /*
     * First Pass approach to spacing code.
     * @Param: builder, holds the detokenized code.
     * @Param: token, the current token.
     * @Return: builder, the sanitized, detokenized code.
     */
    private StringBuilder sanitize(StringBuilder builder, int token, int previousToken) {

        // If the token is a semicolon, then the there is a new line.
        if (token == 63) {
            builder.append(";\n");
            return builder;
        }

        // If the token is not a '(', ')', '[', ']', '{', '}', ';', or '.' then we add a space before it.
        else if (((token<57 || token > 63) && (token!=65)) && ((previousToken<57 || previousToken > 63 || previousToken == 59 || previousToken == 62) && (previousToken!=65)) && !newLine) {
            builder.append(" ");
        }
        return builder;
    }

    /**
     * Method that handles the logic for getting the string corresponding to the user code for a
     * given token, if it sees a novel association then it will
     *
     * @param token  takes ambiguous token
     * @param observedAssociations    Set containing the observed user code assignment nubmer
     * @param userCode   User code segment corresponding to harmonized lines
     * @param userMappings user code mappings
     * @param fixTokenMappedString
     * @return
     */
    private String getName(int token, Set<Integer> observedAssociations, List<EdiToken> userCode,
                           List<Integer> userMappings, Map<Integer, String> fixTokenMappedString) throws Exception {
        // The case where there is no reasonable mapping to the user entry
        if (observedAssociations.isEmpty()) {
            if (token == 51) {
                builder.append("IntegerLiteral");

            } else if (token == 52) {
                if (floatingTokenss.peek()!= null) {
                    builder.append(floatingTokenss.remove());
                } else {
                    builder.append("FloatingPointLiteral");
                }
            } else if (token == 53) {
                if (booleanTokenss.peek() != null) {
                    builder.append(booleanTokenss.remove());
                } else {
                    builder.append("BooleanLiteral");
                }
            } else if (token == 54) {
                if (characterTokenss.peek() != null) {
                    builder.append(characterTokenss.remove());
                } else {
                    builder.append("CharacterLiteral");
                }
            } else if (token == 55) {
                if (stringTokenss.peek() != null) {
                    builder.append(stringTokenss.remove());
                } else {
                    builder.append("StringLiteral");
                }
            } else if (token == 56) {
                if (nullTokenss.peek() != null) {
                    builder.append(nullTokenss.remove());
                } else {
                    builder.append("nullLiteral");
                }
            } else if (token == 100) {
                if (identifierTokenss.peek() != null) {
                    builder.append(identifierTokenss.remove());
                } else {
                    builder.append("Identifier");
                }
            }
        }

        // if only one observed association, returns a single value
        else if (observedAssociations.size() == 1) {
            int key = (int)observedAssociations.toArray()[0];
            String param = userCode.get(userMappings.indexOf(key)).getText();
            if (fixTokenMappedString.containsKey(key)) {
                if (!param.equals(fixTokenMappedString.get(key))) {
                    String message = "Mismatch in fixTokenMappedString association list, " +
                            "already contained \'"+fixTokenMappedString.get(key)+"\' but tried " +
                            "to associate a new \'"+param+"\'";
                    throw new Exception(message);
                }
            }
            fixTokenMappedString.put(key,param);
            return param;
        }

        // the case if there are multiple observed associations...
        else {

        }
    }

    /**
     * Inner class for holding onto Alignments in a way that allows for various useful functions
     * that should not be done in the harmonizaion loop
     */
    private class HarmonizationAlignment {
        List<Alignments> matching;
        //NOTE this contains the reverse mapping, from the errcode to the fixed code
        Map<Integer, List<Integer>> codeMapping = new HashMap<>();

        public HarmonizationAlignment(List<Integer> userCode, List<Integer>
                userAssignments, List<Integer> buggyCode, List<Integer> buggyCodeAssignments) {
            matching = runAlignment(userCode, buggyCode);
            makeMap(userCode, userAssignments, buggyCode, buggyCodeAssignments);
        }

        /*
         * Method that returns the set of all possible mappings for a given usercode alignment,
         * the set will have size 1 if its
         * NOTE: this method returns the mappings from the ERRcode to the USER code
         */
        public Set<Integer> getPossibleMappings(int i) {
            if (codeMapping.containsKey(i)) {
                Set<Integer> out = new HashSet<>();
                for(int j: codeMapping.get(i)) out.add(j);
                return out;
            } else return new HashSet<>();
        }

        /**
         * Method that maps an alignment of matched tokens to other tokens
         */
        private void makeMap(List<Integer> userCode, List<Integer>
                userAssignments, List<Integer> buggyCode, List<Integer> buggyCodeAssignments) {
            int i = 0;
            int j = 0;
            for (Alignments path: matching) {
                if (path==Alignments.MATCH) {
                    int t1 = userCode.get(i);
                    int t2 = buggyCode.get(j);

                    //if they are both amibiguous
                    if (TokenizerBuilder.isAmbiguousToken(t1)&&
                        (TokenizerBuilder.isAmbiguousToken(t2))) {
                        addMatch(buggyCodeAssignments.get(j),userAssignments.get(i));
                    }
                    i++;
                    j++;
                } else if (path==Alignments.MISMATCH) {
                    i++;
                    j++;
                } else if (path==Alignments.INSERTION){
                    j++;
                } else i++;
            }
        }

        /**
         * Helper Method that adds a match to the match list
         */
        private void addMatch(int user, int buggy) {
            if (codeMapping.containsKey(user)) {
                codeMapping.get(user).add(buggy);
            } else {
                List<Integer> l = new ArrayList<>();
                l.add(buggy);
                codeMapping.put(user,l);
            }
        }



        /**
         * intermediate step alignment using smith-watterman approach
         * <p>
         * TODO make this a local alignment in the future
         */
        private List<Alignments> runAlignment(List<Integer> userCode, List<Integer> buggyCode) {
            int[][] scores = new int[userCode.size() + 1][buggyCode.size() + 1];
            Alignments[][] last = new Alignments[userCode.size()][buggyCode.size()];

            // setting up the initial conditions on the alginment table
            Arrays.fill(last[0], Alignments.INSERTION);
            for (int i = 0; i < last.length; i++) {
                last[i][0] = Alignments.DELETION;
            }
            last[0][0] = Alignments.NULL;

            for (int i = 1; i < scores.length; i++) {
                for (int j = 1; j < scores[0].length; j++) {
                    int match = scores[i - 1][j - 1];

                    last[i][j] = Alignments.MISMATCH;

                    // if they match
                    if ((userCode.get(i - 1) == buggyCode.get(j - 1))) {
                        match += TOKEN_MATCH;
                        last[i][j] = Alignments.MATCH;

                        // if they are possible matches
                    } else if (TokenizerBuilder.isDegenerate(userCode.get(i - 1), buggyCode
                            .get(i - 1))) {
                        match += POSSIBLE_MATCH;
                        last[i][j] = Alignments.MATCH;
                    }

                        // if they were both identifiers
                    else if (TokenizerBuilder.isIdentifier(userCode.get(i - 1))
                            && TokenizerBuilder.isIdentifier(buggyCode.get(i - 1))) {
                        match += IDENTIFIER_MATCH;
                        last[i][j] = Alignments.MATCH;
                    }

                    match++;
                    int left = scores[i - 1][j];
                    int right = scores[i][j - 1];
                    int max = Math.max(match, Math.max(left, right));

                    //choose matches over other options
                    if (match == max) ;
                    else if (left == max) last[i][j] = Alignments.INSERTION;
                    else last[i][j] = Alignments.DELETION;
                    scores[i][j] = max;
                }
            }

            // assembling the output
            List<Alignments> output1 = new ArrayList<>();
            List<Alignments> output2 = new ArrayList<>();
            int i = scores.length-1;
            int j = scores[0].length-1;
            while ((i == 0) && (j == 0)) {
                output1.add(last[i][j]);
                if (last[i][j] == Alignments.MATCH) {
                    i--;
                    j--;
                } else if (last[i][j] == Alignments.INSERTION) {
                    i--;
                } else j--;
            }

            //reversing for output
            for (Alignments a : output1) output2.add(a);
            return output2;
        }
    }

    private enum Alignments {
        NULL,MATCH, MISMATCH, INSERTION, DELETION
    }

}
