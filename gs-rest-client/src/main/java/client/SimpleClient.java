package client;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import client.Tokenizer.TokenizerBuilder;

/*
* A Simple REST client that will send ClientFile objects to a server,
* and handle its response.
*/
public class SimpleClient {

    /*
    * Reads in a text file and returns it as a string.
    */
    public static String fileToString(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            stringBuilder.setLength(stringBuilder.length() - 1); //remove extra newline
            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    /*
     * Utilizes the TokenizerBuilder class to tokenize a given file
     */
    public static String tokenize(String string) throws IOException {
        try {
          TokenizerBuilder t = new TokenizerBuilder(string, "String");
          //System.out.println(t.getString());
          return t.getString();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
        return "";
    }

    /*
     * Reads in a text file and returns it as a list of strings corresponding to
     * the given lines..
     */
    public static String fileLinesToString(String fileName, int firstLine, int lastLine) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = null;
        int lineNum = 0;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                lineNum += 1;
                if (firstLine <=lineNum && lineNum <= lastLine) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }
            }
            if (stringBuilder.length() != 0) stringBuilder.setLength(stringBuilder.length() - 1); //remove extra newline
            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    /*
    * Sends a ClientFile object (file + error message) to the server
    * as a JSON string and processes its response.
    */
    public static void fixMyBug(ServerRequest server_request) {
        try {
            //Setup an HTTP POST request
            URL url = new URL("http://localhost:8080/fix");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            try {
                //Convert ServerRequest object to JSON string
                String serverRequestAsJsonString = mapper.writeValueAsString(server_request);
                OutputStream os = conn.getOutputStream();
                os.write(serverRequestAsJsonString.getBytes());
                os.flush();
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Read JSON response from the server in a BufferedReader
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n\n");
            StringBuilder returnedJsonStringBuilder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                returnedJsonStringBuilder.append(output).append("\n");
            }
            returnedJsonStringBuilder.setLength(returnedJsonStringBuilder.length() - 1); //remove extra newline
            String returnedJsonString = returnedJsonStringBuilder.toString();

            DatabaseEntry bug_fix = mapper.readValue(returnedJsonString, DatabaseEntry.class);

            System.out.println(bug_fix);
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Usage: java -jar <jar> <file> <line #: bug start> <line #: bug end>");
            System.exit(0);
        }
        String fileName = args[0];
        int startLine = Integer.parseInt(args[1]);
        int endLine = Integer.parseInt(args[2]);
        
        String relevant_code = "";
        try {
            relevant_code = fileLinesToString(fileName, startLine, endLine);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String tokenized_code = "";
        //Tokenize the input file
        try {
          tokenized_code = tokenize(relevant_code);
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        String errorMessage = "Custom-Error-Message";
        StringBuilder stringBuilder = new StringBuilder();
        //try {
            stringBuilder.append(tokenized_code);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        ServerRequest server_request = new ServerRequest(stringBuilder.toString(), errorMessage);

        fixMyBug(server_request);
    }
}
