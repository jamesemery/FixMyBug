package client;

import client.Tokenizer.TokenizerBuilder;

/**
 * class for storing all those FUCKING bits of state information that have to be passed arround
 * between step
 */
public class HarmonizationStateObject {
    private TokenizerBuilder tokenizedCode;
    private String originalCode;
    private int startLine;

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

    private int endLine;

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
        return tokenizedCode.harmonize(e.getFixedCode());
    }
}
