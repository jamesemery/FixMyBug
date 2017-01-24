

package Filler;

import Filler.Tokenizer.DBAscii;
import Filler.Tokenizer.TokenizerBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
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
            List<Integer> test1 = java.util.Arrays.asList(0, 100, 30, 25, 26, 27);
            String test1a = DBAscii.toAsciiFormat(test1);
            System.out.println("test1a: " + test1a + "   test1a.size() = " + test1a.length());
            List<Integer> test1b = DBAscii.toIntegerListFromAscii(test1a);
            System.out.println("test1b: " + test1b);
            if (test1b.equals(test1)) {
                System.out.println("Failure");
            }

            System.out.println("Test2: " + DBAscii.toIntegerListFromAscii("qbds"));
            System.out.println("Test3: " + DBAscii.toIntegerListFromAscii(""));

            String file1 = new String(Files.readAllBytes(Paths.get
                    ("./src/main/java/Filler/Tokenizer/javaparser/examples" +
                            "/HelloWorld.java")));

            DBFillerInterface i = new DBFillerInterface("uploadTestDB");
            System.out.println(i.createDatabaseEntry(file1,file1,3,5,3,5));

            i.uploadToDatabase(file1,file1,4);


            // Creates a new instance of TokenizerBuilder.
            TokenizerBuilder t = new TokenizerBuilder("./src/main/java/carleton/comps/javaparser/examples/HelloWorld.java","File");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
