

package client.Tokenizer;

import carleton.comps.javaparser.JavaLexer;
import carleton.comps.javaparser.JavaParser;
import carleton.comps.IdentifierListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/*
 * Checks if the TokenizerBuilder class works.
 * @author Alex Griese
 * @author Jamie Emery
 */

public class Main {

    public static void main(String[] args) {
        printIdentifiers();
        //try {

        // Testing the file type of the constructor.

        // Creates a new instance of TokenizerBuilder.
        //TokenizerBuilder t = new TokenizerBuilder("./Tokenizer/src/main/java/carleton/comps/javaparser/examples/HelloWorld.java","File");


        // Prints the tokenized version of HelloWorld.java.
        //System.out.println(t.getString());

        //List<Token> tokens = t.getTokens();
        //System.out.println(t.tokensToString(tokens));

        // Gets a list of Tokens that come from the HelloWorld.java file between lines 1 and 6.
        //List<Token> l = t.betweenLines(0,4);

        // Prints out the list of Tokens in lines 1 to 6 in the HelloWorld.java file.
        //System.out.println(t.tokensToString(l));

        // Prints out the detokenized, tokenized code.
        //System.out.println(t.harmonize(t.getString()));

            /*========================================================================================================*/


        // Testing the String type of constructor.


            /*TokenizerBuilder x = new TokenizerBuilder("package carleton.comps.javaparser.examples;\n" +
                    "\n" +
                    "public class HelloWorld {\n" +
                    "   public static void main(String[] args) { \n" +
                    "      System.out.println(\"Hello, World\");\n" +
                    "   }\n" +
                    "}","String");

            // Prints the tokenized version of HelloWorld.java.
            //System.out.println(t.getString());

            //List<Token> tokens = t.getTokens();
            //System.out.println(t.tokensToString(tokens));

            // Gets a list of Tokens that come from the HelloWorld.java file between lines 1 and 6.
            //List<Token> l = t.betweenLines(3,3);

            // Prints out the list of Tokens in lines 1 to 6 in the HelloWorld.java file.
            //System.out.println(t.tokensToString(l));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private static void printIdentifiers() {
        // Get our lexer
        JavaLexer lexer = new JavaLexer(new ANTLRInputStream("package carleton.comps.javaparser;\n" +
                "\n" +
                "public class HelloWorld {\n" +
                "   public static void main(String[] args) {\n" +
                "      System.out.println(\"Hello, World\");\n" +
                "   }\n" +
                "}"));

        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass the tokens to the parser
        JavaParser parser = new JavaParser(tokens);

        // Specify our entry point
        JavaParser.CompilationUnitContext compilationUnitContext = parser.compilationUnit();

        // Walk it and attach our listener
        ParseTreeWalker walker = new ParseTreeWalker();
        IdentifierListener listener = new IdentifierListener();
        walker.walk(listener, compilationUnitContext);

        System.out.println("=============OUTSIDE====================");
        for (String o : listener.outside) {
            System.out.println(o);
        }

        System.out.println("=============CLASS====================");
        for (String c : listener.classIds) {
            System.out.println(c);
        }

        System.out.println("=============FUNCTION====================");
        for (String f : listener.function) {
            System.out.println(f);
        }

        System.out.println("=============VARIABLE====================");
        for (String v : listener.variable) {
            System.out.println(v);
        }
        System.out.println("=============VARIABLECLASS====================");
        for (String v : listener.variableClass) {
            System.out.println(v);
        }
        System.out.println("=============VARIABLEFUNCTION====================");
        for (String v : listener.variableFunction) {
            System.out.println(v);
        }
        System.out.println("=============FUNCTIONVARIABLECLASS====================");
        for (String v : listener.functionVariableClass) {
            System.out.println(v);
        }
    }
}
