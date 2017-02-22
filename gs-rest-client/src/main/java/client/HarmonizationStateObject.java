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
    private static int COMPLETEAL_TOKEN_MATCH = 5;
    private static int COMPLETEAL_IDENTIFIER_MATCH = 2;
    private static int COMPLETEAL_POSSIBLE_MATCH = 4;
    private static int COMPLETEAL_MISMATCH = -4;
    private static int COMPLETEAL_INDEL = -1;

    private static int LOCAL_TOKEN_MATCH = 5;
    private static int LOCAL_IDENTIFIER_MATCH = 2;
    private static int LOCAL_POSSIBLE_MATCH = 4;
    private static int LOCAL_MISMATCH = -3;
    private static int LOCAL_INDEL = -2;

    //LIST OF VARIOUS GENERIC NAMES TO INPUT
    private int GLOBAL_VAR_GRAB_INDEX = 0;
    private static String[] VAR_GRAB_BAG = new String[]{"X","Y","Z","A","B","C","D","M","N","H",
            "J","I"};


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
        userAssignments = userAssignments.subList(userCode.get(0).getTokenIndex(),userCode.get
                (userCode.size()-1).getTokenIndex());

        List<Integer > buggyCode = Arrays.asList(e.getBuggyCode().split(" ")).stream().map
                (Integer::parseInt).collect(Collectors.toList());
        List<Integer > buggyCodeAssignments = Arrays.asList(e.getBuggyCodeAssignments().split(" ")).stream()
                .map(Integer::parseInt).collect(Collectors.toList());

        List<Integer > fixedCode = Arrays.asList(e.getFixedCode().split(" ")).stream().map
                (Integer::parseInt).collect(Collectors.toList());
        List<Integer > fixedCodeAssignments = Arrays.asList(e.getFixedCodeAssignments().split(" ")).stream()
                .map(Integer::parseInt).collect(Collectors.toList());

        System.out.println("userCode to buggy code Alignment:\nuserCode: "+userCode.stream().map(tok
                -> tok.getType()).collect(Collectors.toList())+"\nbuggyCode:"+buggyCode);
        HarmonizationAlignment userToBugAlignment = new HarmonizationAlignment(userCode.stream().map(tok
                -> tok.getType()).collect(Collectors.toList()), userAssignments, buggyCode,
                buggyCodeAssignments, AlignmentType.LOCAL);
        System.out.println("buggy to fix code Alignment:\nbuggyCode:"+buggyCode+"\nfixCode: "+fixedCode);
        HarmonizationAlignment bugToFixAlignment = new HarmonizationAlignment(buggyCode,
                buggyCodeAssignments, fixedCode, fixedCodeAssignments, AlignmentType.COMPLETE);

        // Holds the detokenized, tokenized code.
        StringBuilder builder = new StringBuilder();

        // Mapping containing the actual string values for the used ambiguous tokens
        Map<Integer,String> fixTokenMappedString = new HashMap<>();

        int previousToken = 0;

        // Finding where to start and end harmonization based on the double alignment
        int userStartTokenIndex=0;
        int userEndTokenIndex=0;
        int fixStartTokenIndex=0;
        int fixEndTokenIndex=0;
        try {
            userStartTokenIndex = userToBugAlignment.getUserStartIndex();
            userEndTokenIndex = userToBugAlignment.getUserEndIndex();
            fixStartTokenIndex = bugToFixAlignment.getFixStartIndex(userToBugAlignment.getFixStartIndex
                    (userStartTokenIndex));
            fixEndTokenIndex = bugToFixAlignment.getFixEndIndex(userToBugAlignment.getFixEndIndex
                    (userEndTokenIndex));
        } catch (Exception e1){
            System.out.println("Something went wrong with grabbing the fix start and end " +
                    "alignments");
            System.out.println(e1.toString());
            e1.printStackTrace();
            //TODO probably throw and exception here
        }
        System.out.println("userStartTokenIndex ="+userStartTokenIndex+"\nuserEndTokenIndex " +
                "="+userEndTokenIndex+"\nfixStartTokenIndex " +
                "="+fixStartTokenIndex+"\nfixEndTokenIndex ="+fixEndTokenIndex);

        // Generating the index that holds all of the character seqences between tokens in the
        // original code for the token index
        String[] spacings = getUserSpacing(userCode);
        String prependingSpacing = spacings[0];
        for (int i = 0; i<userStartTokenIndex; i++) {
            builder.append(spacings[i]+userCode.get(i).getText());
        }


        // Loops through the fixed code and tries to print it
        for (int i = fixStartTokenIndex; i<=fixEndTokenIndex; i++) {
            int token = fixedCode.get(i);
            //Add whatever spacing needs to be added to the previous stuff

            fakeUserSpacing(builder, token, previousToken);//, prependingSpacing);

            previousToken = token;

            // If its not ambiguous just print as-is
            if (!TokenizerBuilder.isAmbiguousToken(token)) {
                // Fixing the issue of adding tokens
                String display = JavaParser.VOCABULARY.getLiteralName(token);
                builder.append(display.substring(1, display.length() - 1));
            }

            else {
                Set<Integer> mappedAssignments = userToBugAlignment.getPossibleMappings
                        (fixedCodeAssignments.get(i));
                try {
                    builder.append(getName(token, fixedCodeAssignments.get(i), mappedAssignments,
                            userCode, userAssignments, fixTokenMappedString));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

        // Adding in any extra code to the back from harmonization
        for (int i = userEndTokenIndex+1; i<userCode.size(); i++){
            builder.append(spacings[i]+userCode.get(i).getText());
        }
        System.out.println(fixTokenMappedString);
        System.out.println("===========\n"+builder.toString()+"\n=========");

        GLOBAL_VAR_GRAB_INDEX = 0;
        return builder.toString()+"\n";
    }

    /**
     * Helper method that handles the logic for determining what the original users whitespacing
     * was in the vicinity of the specified index location in the users code.
     *
     * Returns String array:
     *        -Array index 0 corresponds to characters preceeding the first token from the frist
     *        line, specifically its looking for what sort of spacing the user had before the
     *        first line.
     *        -Array index 1..j.n corresponding to characters preceeding item j in the array
     *        -Array index n+1 corrsponding to characters following the last item in the array
     *        but preceeding the next token from the original code.
     *
     * @ param userTokens
     * @return
     */
    private String[] getUserSpacing(List<EdiToken> userTokens) {
//        System.out.println("Genereating user spaicing map");
        String[] output = new String[userTokens.size()+1];
        Scanner s = new Scanner(originalCode);
        String currentLine ="";
        int curLineNumber = 0;
        int curCharInLine = 0;

        // Setting this to true to start is a hack i know...
        boolean inMultiLine = true;
        String buffer = "";
        System.out.println(userTokens);
        for (int i = 0; i<userTokens.size(); i++){
//            System.out.println("for loop index: "+i);
//            System.out.println("inMultiLine: "+inMultiLine);
//            System.out.println("linenumber: "+i);
            // Ensuring that the current line matches the correct line of the token
            while(curLineNumber<userTokens.get(i).getLine()) {
                if (!s.hasNextLine()) {
                    System.out.println("SOME SERIOUS SHIT is wrong here, the original user code " +
                            "lines don't match with what we are looking for");
                }
                // If in a multiLine, we don't want to add to the builder buffer
                if (!inMultiLine) {
                    buffer = buffer + currentLine.substring(curCharInLine) +"\n";
                    curCharInLine = 0;
                }
                currentLine = s.nextLine();
                curLineNumber++;
            }
            inMultiLine = false;
//            System.out.println("new linenumber: "+i);
//            System.out.println("curLine: \'"+currentLine+"\'");
//            System.out.println("getstartindex: "+userTokens.get(i).getStartIndex());
//            System.out.println("getendindex: "+userTokens.get(i).getStopIndex());
//            System.out.println("getCharPositionInLine: "+userTokens.get(i).getTokenIndex());

            output[i] = buffer + currentLine.substring(curCharInLine, userTokens.get(i).getCharPositionInLine());
            buffer = "";

            //Handling line spanning tokens
            String tokenText = userTokens.get(i).getText();
            if (tokenText.contains("\n")||tokenText.contains("\r")) {
                String[] lines = tokenText.split("\r\n|\r|\n", -1);
                curCharInLine = lines[lines.length-1].length()+1;
                inMultiLine = true;
            } else {
                curCharInLine = userTokens.get(i).getCharPositionInLine()+tokenText.length();
            }
        }
        // Filling in anything from the last line
        int lines = userTokens.get(userTokens.size()-1).getText().split("\r\n|\r|\n", -1).length-1;
        while (lines>0) {
            currentLine = s.nextLine();
            lines--;
        }
        output[output.length-1] = currentLine.substring(curCharInLine);
        System.out.println("User Spacings:\n"+Arrays.asList(output));
        return output;

    }

    /*
     * First Pass approach to spacing code.
     * @Param: builder, holds the detokenized code.
     * @Param: token, the current token.
     * @Return: builder, the sanitized, detokenized code.
     */
    private StringBuilder fakeUserSpacing(StringBuilder builder, int token, int previousToken) {
                                            //String prependingSpacing) {

        // If the token is a semicolon, then the there is a new line.
        if (previousToken == 63) {
            builder.append("\n");//+prependingSpacing);
            return builder;
        }
        // If the token is a left bracket, then open a new line
        else if (previousToken == 59) {
            builder.append("\n\t");//+prependingSpacing);
            return builder;
        }

        // If the token is not a '(', ')', '[', ']', '{', '}', ';', or '.' then we add a space before it.
        else if (((token<57 || token > 63) && (token!=65)) && ((previousToken<57 || previousToken
                > 63 || previousToken == 59 || previousToken == 62) && (previousToken!=65)) &&
                (builder.lastIndexOf("\n")!=builder.length()-1)) {
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
    private String getName(int token, int assignment , Set<Integer> observedAssociations,
                           List<EdiToken> userCode, List<Integer> userMappings, Map<Integer, String> fixTokenMappedString) throws Exception {
        if (fixTokenMappedString.containsKey(assignment)) {
            return fixTokenMappedString.get(assignment);
        }
        // The case where there is no reasonable mapping to the user entry
        else if (observedAssociations.isEmpty()) {
            if (fixTokenMappedString.containsKey(assignment)) {
                return fixTokenMappedString.get(assignment);
            } else {

                String fakeName = "";
                String extra = "";
                if (token == 51) {
                    fakeName = "##int";
                } else if (token == 52) {
                    fakeName = "##double";
                } else if (token == 53) {
                    fakeName = "##bool";
                } else if (token == 54) {
                    fakeName = "'##char";
                    extra = "'";
                } else if (token == 55) {
                    fakeName = "\"##String";
                    extra = "\"";
                } else if (token == 56) {
                    fakeName = "##null";
                } else if (token == 100) {
                    fakeName = "##IDENTIFIER";
                } else if (token == 110) {
                    fakeName = "##CLASS";
                } else if (token == 111) {
                    fakeName = "##FUNCTION";
                } else if (token == 112) {
                    fakeName = "##VAR";
                } else if (token == 113) {
                    fakeName = "##OUTSIDE";
                } else if (token == 114) {
                    fakeName = "##VAR/CLASS";
                } else if (token == 115) {
                    fakeName = "##FUN/CLASS";
                } else {
                    fakeName = "##IDENTIFIER";
                }
                fakeName = fakeName + "_" + VAR_GRAB_BAG[GLOBAL_VAR_GRAB_INDEX++] + "##" + extra;
                fixTokenMappedString.put(assignment, fakeName);
                return fakeName;
            }
        }

        // if only one observed association, returns a single value
        else if (observedAssociations.size() == 1) {
            int key = (int) observedAssociations.toArray()[0];
            String param = userCode.get(userMappings.indexOf(key)).getText();
            fixTokenMappedString.put(assignment, param);
            return param;

        }

        // the case if there are multiple observed associations...
        else {
            StringJoiner joiner = new StringJoiner("/");
            for (int key : observedAssociations) {
                joiner.add(userCode.get(userMappings.indexOf(key)).getText());
            }
            fixTokenMappedString.put(assignment,joiner.toString());
            return joiner.toString();
        }
    }

    /**
     * Inner class for holding onto Alignments in a way that allows for various useful functions
     * that should not be done in the harmonizaion loop
     */
    private class HarmonizationAlignment {
        private List<Alignments> matching;
        private AlignmentType alignmentType;
        //NOTE this contains the reverse mapping, from the errcode to the fixed code
        private Map<Integer, List<Integer>> codeMapping = new HashMap<>();
        // Keeping track of the lengths of each array for posterity sake
        private int userCodeLength;
        private int buggyCodeLength;

        public HarmonizationAlignment(List<Integer> userCode, List<Integer>
                userAssignments, List<Integer> buggyCode, List<Integer> buggyCodeAssignments,
                                      AlignmentType type) {
            alignmentType = type;
            if (type == AlignmentType.LOCAL) {
                matching = runLocalAlignment(userCode, buggyCode);
            } else {
                matching = runAlignment(userCode, buggyCode);
            }
            makeMap(userCode, userAssignments, buggyCode, buggyCodeAssignments);
            userCodeLength = userCode.size();
            buggyCodeLength = buggyCode.size();
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
                switch (path) {
                    case MATCH:
                        int t1 = userCode.get(i);
                        int t2 = buggyCode.get(j);
                        if (TokenizerBuilder.isAmbiguousToken(t1) &&
                                (TokenizerBuilder.isAmbiguousToken(t2))) {
                            if (TokenizerBuilder.isDegenerate(t1,t2)){
                                addMatch(buggyCodeAssignments.get(j), userAssignments.get(i));
                            } else
                            System.out.println("Degenracy issue between tokens '"+t1+"' and '"+t2+"'");
//                        System.out.println(codeMapping);
//                        System.out.println("add match for tokens:"+t1+" "+t2);
//                        System.out.println("buggyCodeAssignments:"+buggyCodeAssignments);
//                        System.out.println("userAssignments:"+userAssignments);
//                        System.out.println(codeMapping);

                        }
                        i++;
                        j++;
                        break;
                    case MISMATCH:
                        i++;
                        j++;
                        break;
                    case INSERTION:
                        j++;
                        break;
                    case ClipINSERTION:
                        j++;
                        break;
                    case DELETION:
                        i++;
                        break;
                    case ClipDELETION:
                        i++;
                        break;
                }
            }
        }

        /**
         * Helper Method that adds a match to the match list
         */
        private void addMatch(int key, int value) {
            if (codeMapping.containsKey(key)) {
                codeMapping.get(key).add(value);
            } else {
                List<Integer> l = new ArrayList<>();
                l.add(value);
                codeMapping.put(key,l);
            }
        }

        /**
         * Method that determines the index of the first matched character in the user code
         * compared with the fix code.
         */
        public int getUserStartIndex() {
            if (alignmentType==AlignmentType.LOCAL) {
                int firstUserIndex = 0;
                int matchingIndex = 0;
                while (matchingIndex < matching.size()) {
                    if (matching.get(matchingIndex)==Alignments.ClipDELETION) {
                        firstUserIndex++;
                    } else if (matching.get(matchingIndex)!=Alignments.ClipINSERTION) {
                        break;
                    }
                    matchingIndex++;
                }
                return firstUserIndex;//TODO check for OBOB's
            }
            return 0;
        }

        /**
         * Method that calculates the index of the last item in the user code that is important
         */
        public int getUserEndIndex() {
            if (alignmentType==AlignmentType.LOCAL) {
                int lastUserIndex = userCodeLength-1;
                int matchingIndex = matching.size()-1;
                while (matchingIndex > 0) {
                    if (matching.get(matchingIndex)==Alignments.ClipDELETION) {
                        lastUserIndex--;
                    } else if (matching.get(matchingIndex)!=Alignments.ClipINSERTION) {
                        break;
                    }
                    matchingIndex--;
                }
                return lastUserIndex;//TODO check for OBOB's
            }
            return userCodeLength-1;
        }

        /**
         * Method that determines the index of the first token to use in the second alignment
         * based on the provided userStartTokenIndex. Will determine the token based on the first
         * fixcode token that aligns to the user code alignment
         * @param userStartTokenIndex
         */
        public int getFixStartIndex(int userStartTokenIndex) throws Exception {
            int curUserIndex = -1;
            int curFixIndex = -1;
            int matchingIndex = 0;
            System.out.println("Getting fix start index:"+userStartTokenIndex);
            while (matchingIndex < matching.size()) {
                if (curUserIndex>=userStartTokenIndex) {
                    return (curFixIndex>=0?curFixIndex:0);
                }

                // Else, properly increment the index counters
                Alignments cur = matching.get(matchingIndex);
                if (cur==Alignments.ClipDELETION) {
                    curUserIndex++;
                } else if (cur==Alignments.DELETION) {
                    curUserIndex++;
                } else if (cur==Alignments.ClipINSERTION) {
                    curFixIndex++;
                } else if (cur==Alignments.INSERTION) {
                    curFixIndex++;
                } else if (cur==Alignments.MATCH) {
                    curUserIndex++;
                    curFixIndex++;
                } else if (cur==Alignments.MISMATCH) {
                    curUserIndex++;
                    curFixIndex++;
                } else {
                    throw new Exception("Alignment was bad, unexpected character '"+cur+"' at " +
                            "position "+matchingIndex+" from the alignment: "+matching);
                }

                matchingIndex++;
            }
            throw new Exception("Something went wrong searching for the userIndexItem '"+
                    userStartTokenIndex+"' from the alignment: "+matching);
        }

        /**
         * Method that determines the index of the last token to use in the second alignment
         * based on the provided userStartTokenIndex. Will determine the token based on the last
         * fixcode token that aligns to the user code alignment
         * @param userEndTokenIndex
         */
        public int getFixEndIndex(int userEndTokenIndex) throws Exception {
            int curUserIndex = userCodeLength;
            int curFixIndex = buggyCodeLength;
            int lastFixIndex = buggyCodeLength;
            int matchingIndex = matching.size()-1;
            System.out.println("Getting fix end index:"+userEndTokenIndex);
            while (matchingIndex >= 0) {
                System.out.println("Matching index is:"+matchingIndex+"  " +
                        "curUserIndex:"+curUserIndex+"   curFixIndex:"+curFixIndex+"     " +
                        "lastMatchIndex:"+lastFixIndex);
                if (curUserIndex <= userEndTokenIndex) {
                    // We keep the last match index because we want to take the last of a run of
                    // insertions and keep it.
                    return lastFixIndex -1;
                }

                // Else, properly increment the index counters
                Alignments cur = matching.get(matchingIndex);
                if (cur==Alignments.ClipDELETION) {
                    curUserIndex--;
                } else if (cur==Alignments.DELETION) {
                    if (curUserIndex-1<=userEndTokenIndex){
                        return lastFixIndex -1;
                    } else {
                        lastFixIndex = curFixIndex;
                    }
                    curUserIndex--;
                } else if (cur==Alignments.ClipINSERTION) {
                    curFixIndex--;
                    lastFixIndex = curFixIndex;
                } else if (cur==Alignments.INSERTION) {
                    curFixIndex--;
                } else if (cur==Alignments.MATCH) {
                    if (curUserIndex-1<=userEndTokenIndex){
                        return lastFixIndex -1;
                    } else {
                        lastFixIndex = curFixIndex;
                    }
                    curUserIndex--;
                    curFixIndex--;
                } else if (cur==Alignments.MISMATCH) {
                    if (curUserIndex-1<=userEndTokenIndex){
                        return lastFixIndex -1;
                    } else {
                        lastFixIndex = curFixIndex;
                    }
                    curUserIndex--;
                    curFixIndex--;
                } else {
                    throw new Exception("Alignment was bad, unexpected character '"+cur+"' at " +
                            "position "+matchingIndex+" from the alignment: "+matching);
                }

                matchingIndex--;
            }
            throw new Exception("Something went wrong searching for the userIndexItem '"+
                    userEndTokenIndex+"' from the alignment: "+matching);
        }


        /**
         * intermediate step alignment using smith-watterman approach
         * <p>
         * TODO make this a local alignment in the future
         */
        public List<Alignments> runAlignment(List<Integer> userCode, List<Integer> buggyCode) {
            int[][] scores = new int[userCode.size() + 1][buggyCode.size() + 1];
            Alignments[][] last = new Alignments[userCode.size() + 1][buggyCode.size() + 1];

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
                        match += COMPLETEAL_TOKEN_MATCH;
                        last[i][j] = Alignments.MATCH;

                        // if they are possible matches
                    } else if (TokenizerBuilder.isDegenerate(userCode.get(i - 1), buggyCode
                            .get(j - 1))) {
                        match += COMPLETEAL_POSSIBLE_MATCH;
                        last[i][j] = Alignments.MATCH;
                    }

                        // if they were both identifiers
                    else if (TokenizerBuilder.isIdentifier(userCode.get(i - 1))
                            && TokenizerBuilder.isIdentifier(buggyCode.get(j - 1))) {
                        match += COMPLETEAL_IDENTIFIER_MATCH;
                        last[i][j] = Alignments.MATCH;


                    } else {
                        match+= COMPLETEAL_MISMATCH;
                    }

                    match++;
                    int left = scores[i - 1][j]+COMPLETEAL_INDEL;
                    int right = scores[i][j - 1]+COMPLETEAL_INDEL;
                    int max = Math.max(match, Math.max(left, right));

                    //choose matches over other options
                    if (match == max) ;
                    else if (left == max) last[i][j] = Alignments.DELETION;
                    else last[i][j] = Alignments.INSERTION;
                    scores[i][j] = max;
                }
            }

            // assembling the output
            List<Alignments> output1 = new ArrayList<>();
            List<Alignments> output2 = new ArrayList<>();
            int i = scores.length-1;
            int j = scores[0].length-1;
            while ((i != 0) || (j != 0)) {
                output1.add(last[i][j]);
                if ((last[i][j] == Alignments.MATCH)||(last[i][j] == Alignments.MISMATCH)) {
                    i--;
                    j--;
                } else if (last[i][j] == Alignments.INSERTION) {
                    j--;
                } else i--;
            }

            //reversing for output
            for (int a = output1.size()-1; a>=0; a--) output2.add(output1.get(a));
            System.out.println("Alignment was: " + output2);
            return output2;
        }



        /**
         * Local intermediate alignmen step using smith-waterman approach
         * <p>
         * TODO make this a local alignment in the future
         */
        public List<Alignments> runLocalAlignment(List<Integer> userCode, List<Integer> buggyCode) {
            int[][] scores = new int[userCode.size() + 1][buggyCode.size() + 1];
            Alignments[][] last = new Alignments[userCode.size() + 1][buggyCode.size() + 1];

            // setting up the initial conditions on the alginment table
            Arrays.fill(last[0], Alignments.NULL);
            for (int i = 0; i < last.length; i++) {
                last[i][0] = Alignments.NULL;
            }
            last[0][0] = Alignments.NULL;

            for (int i = 1; i < scores.length; i++) {
                for (int j = 1; j < scores[0].length; j++) {
                    int match = scores[i - 1][j - 1];

                    // if they match
                    if ((userCode.get(i - 1) == buggyCode.get(j - 1))) {
                        match += LOCAL_TOKEN_MATCH;
                        last[i][j] = Alignments.MATCH;

                        // if they are possible matches
                    } else if (TokenizerBuilder.isDegenerate(userCode.get(i - 1), buggyCode
                            .get(j - 1))) {
                        match += LOCAL_POSSIBLE_MATCH;
                        last[i][j] = Alignments.MATCH;
                    }

                    // if they were both identifiers
                    else if (TokenizerBuilder.isIdentifier(userCode.get(i - 1))
                            && TokenizerBuilder.isIdentifier(buggyCode.get(j - 1))) {
                        match += LOCAL_IDENTIFIER_MATCH;
                        last[i][j] = Alignments.MATCH;
                    } else {
                        match += LOCAL_MISMATCH;
                        last[i][j] = Alignments.MISMATCH;
                    }

                    match++;
                    int left = scores[i - 1][j] + LOCAL_INDEL;
                    int right = scores[i][j - 1] + LOCAL_INDEL;
                    int max = Math.max(match, Math.max(left, right));

                    //choose matches over other options
                    if (max <= 0) {
                        max = 0;
                        last[i][j] = Alignments.NULL;
                    } else if (match == max) ;
                    else if (left == max) {last[i][j] = Alignments.DELETION;}
                    else last[i][j] = Alignments.INSERTION;
                    scores[i][j] = max;
                }
            }

            // finding the best aligned subsection of the array to output
            List<Alignments> output1 = new ArrayList<>();
            List<Alignments> output2 = new ArrayList<>();
            int i = scores.length-1;
            int j = scores[0].length-1;
            int max = 0;
            for (int x = 1; x<last.length;x++) {
                for (int y = 1; y<last[0].length;y++) {
                    if (scores[x][y]>=max) {
                        max = scores[x][y];
                        i = x;
                        j = y;
                    }
                }
            }
            // Adding Clip Insertions to the front and the back
            for (int x = i;  x < scores.length-1; x++) {
                output1.add(Alignments.ClipDELETION);
            }
            for (int y = j;  y < scores[0].length-1; y++) {
                output1.add(Alignments.ClipINSERTION);
            }

            while ((i != 0) || (j != 0)) {
                if (last[i][j] == Alignments.NULL) {
                    break;
                }
                output1.add(last[i][j]);
                if ((last[i][j] == Alignments.MATCH)||(last[i][j] == Alignments.MISMATCH)) {
                    i--;
                    j--;
                } else if (last[i][j] == Alignments.INSERTION) {
                    j--;
                } else if (last[i][j] == Alignments.DELETION) i--;
            }

            // Adding clips to the front of the array and the back
            while ((i != 0) || (j != 0)) {
                if (i != 0) {
                    output1.add(Alignments.ClipDELETION);
                    i--;
                } else {
                    output1.add(Alignments.ClipINSERTION);
                    j--;
                }
            }

            //reversing for output
            for (int a = output1.size()-1; a>=0; a--) output2.add(output1.get(a));
            System.out.println("Alignment was: " + output2);
            return output2;
        }
    }

    private enum Alignments {
        NULL,MATCH, MISMATCH, INSERTION, DELETION, ClipINSERTION, ClipDELETION
    }

    private enum AlignmentType {
        LOCAL, COMPLETE
    }

}
