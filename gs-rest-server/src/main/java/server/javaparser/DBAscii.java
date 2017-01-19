package server.javaparser;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that handles the conversion of data to and from compressed ascii format used for
 * database filling
 *
 * Technically fails for unicode codepoints that jump outside of UTF-16 but for our purpouses
 * shouldnt be an issue
 */
public class DBAscii {

    // Converts tokens or integers to ascii characters in a string
//    public static String tokensToAsciiFormat(List<Token> tokens) {
//        return toAsciiFormat(tokens.stream().map(token -> token.getType()).collect(Collectors.toList
//                ()));
//    }
    public static String toAsciiFormat(List<Integer> ints) {
        StringBuilder builder = new StringBuilder();

        for (int t : ints) {
            builder.append(Character.toChars(t + 1));
        }
        return builder.toString();
    }

    // Methods for conversion of an ascii formatted string back into a thing TODO fill
    public static List<Integer> toIntegerListFromAscii(String data) {
        List<Integer> out = new ArrayList<>();

        for (char c : data.toCharArray()) {
            out.add((int)c -1);
        }
        return out;
    }

}
