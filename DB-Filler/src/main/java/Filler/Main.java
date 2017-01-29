

package Filler;

import Filler.Tokenizer.DBAscii;
import Filler.Tokenizer.TokenizerBuilder;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import java.io.IOException;

/*
 * Checks if the TokenizerBuilder class works.
 * @author Alex Griese
 * @author Jamie Emery
 */

public class Main {

    public static void main(String[] args) {
        try {

            // Testing the file type of the constructor.
//            List<Integer> test1 = java.util.Arrays.asList(0, 100, 30, 25, 26, 27);
//            String test1a = DBAscii.toAsciiFormat(test1);
//            System.out.println("test1a: " + test1a + "   test1a.size() = " + test1a.length());
//            List<Integer> test1b = DBAscii.toIntegerListFromAscii(test1a);
//            System.out.println("test1b: " + test1b);
//            if (test1b.equals(test1)) {
//                System.out.println("Failure");
//            }
//
//            System.out.println("Test2: " + DBAscii.toIntegerListFromAscii("qbds"));
//            System.out.println("Test3: " + DBAscii.toIntegerListFromAscii(""));   a
            String file1 = "package Filler.Tokenizer.javaparser.examples;\n" +
                    "\n" +
                    "public class HelloWorld {\n" +
                    "   public static void main(String[] args) {\n" +
                    "      System.out.println(\"Hello, World\");\n" +
                    "      System.out.println(\"\");\n" +
                    "   }\n" +
                    "}\n";

            String file2 = "package Filler.Tokenizer.javaparser.examples;\n" +
                    "\n" +
                    "// Comment comment comment\n" +
                    "public class HelloWorld2 {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        System.out.println(\"\");\n" +
                    "        System.out.println(\"Hello, World\");\n" +
                    "    }\n" +
                    "}\n";


            DBFillerInterface i = new DBFillerInterface("uploadTestDB");
            i.uploadToDatabase(file1,file2,5);
//            System.out.println(i.createDatabaseEntry(file1,file1,3,5,3,5).toStringVerbose());
            System.out.println("=========================");

            DiffMatchPatch differ = new DiffMatchPatch();
            LinkedList<DiffMatchPatch.Diff> diff = differ.diffMain(file1, file2);
            System.out.println(diff);
            differ.diffCleanupSemantic(diff);
            System.out.println(diff);

//            i.uploadToDatabase(file1, file1, 4);

//            System.out.println("DATABASE RETURN:\n\n"+i.SelectAll(1).toStringVerbose());

            // Creates a new instance of TokenizerBuilder.
            //TokenizerBuilder t = new TokenizerBuilder("./src/main/java/carleton/comps/javaparser/examples/HelloWorld.java","File");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
