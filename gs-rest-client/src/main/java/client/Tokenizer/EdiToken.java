package client.Tokenizer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

/**
 * Created by alexgriese on 2/7/17.
 */
public class EdiToken {
    private Token token;
    private int type;

    public EdiToken(Token t) {
        token = t;
        type = t.getType();
    }
    public int getChannel() {
        return token.getChannel();
    }
    public int getCharPositionInLine() {
        return token.getCharPositionInLine();
    }
    public CharStream getInputStream() {
        return token.getInputStream();
    }
    public int getLine() {
        return token.getLine();
    }
    public int getStartIndex() {
        return token.getStartIndex();
    }
    public int getStopIndex() {
        return token.getStopIndex();
    }
    public String getText() {
        return token.getText();
    }
    public int getTokenIndex() {
        return token.getTokenIndex();
    }
    public TokenSource getTokenSource() {
        return token.getTokenSource();
    }
    public void setType(int t) {
        type = t;
    }
    public int getType() {
        return type;
    }
}
