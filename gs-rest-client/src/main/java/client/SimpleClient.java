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

    private static final String BASE_URL = "http://localhost:8080/";

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
    public static TokenizerBuilder tokenize(String string) throws IOException {
        try {
            System.out.print(string);
          TokenizerBuilder t = new TokenizerBuilder(string, "String");
          return t;
        } catch (IOException ex) {
          ex.printStackTrace();
        }
        System.out.println("Error while tokenizing... returning an empty string.");
        return new TokenizerBuilder("","String");
    }

    /*
     * A function that returns the lines of a file between provided starting and ending
     * line numbers. 
     *
     * @param fileName: a text file
     * @param firstLine: the starting line number of the desired code block
     * @param lastLine: the end line number of the desired code block, set to -1 for all lines
     * @return A String containing the lines from the text file we interpret to contain
     * buggy code. 
     */
    public static String getLinesFromFile(String fileName, int firstLine, int lastLine) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = null;
        int lineNum = 0;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                lineNum += 1;
                if (firstLine <=lineNum && ((lineNum == -1)||(lineNum <= lastLine))) {
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
    public static void makeRequest(ServerRequest serverRequest, String method) {
        makeRequest(serverRequest, null, null, method);
    }

    public static void makeRequest(ServerRequest serverRequest, TokenizerBuilder tokenBuilder,
                                   String sourceFile, String method) {
        makeRequest(serverRequest, new HarmonizationStateObject(tokenBuilder, sourceFile, 0, 0),
                method);
    }

    public static void makeRequest(ServerRequest serverRequest, HarmonizationStateObject harmonizationStateObject, String method) {
        System.out.println("Sending tokenized code: " + serverRequest.getBuggyCode() + "\nWith the error message: " + serverRequest.getErrorMessage() + "\n\n\n");

        try {
            //Setup an HTTP POST request
            URL url = new URL(BASE_URL + method);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            try {
                //Convert ServerRequest object to JSON string and send to server
                String serverRequestAsJsonString = mapper.writeValueAsString(serverRequest);
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

            //Read the byte response from the server in a BufferedReader
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;

            //Convert input stream of bytes into a JSON string
            StringBuilder returnedJsonStringBuilder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                returnedJsonStringBuilder.append(output).append("\n");
            }
            returnedJsonStringBuilder.setLength(returnedJsonStringBuilder.length() - 1); //remove extra newline
            String returnedJsonString = returnedJsonStringBuilder.toString();

            //Handle the server's response based on the requested method.
            handleResponse(returnedJsonString, harmonizationStateObject, method);
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /*
    * Handles the response from gs REST server.
    * @param json: a JSON string returned from the server
    * @param tokenBuilder: a TokenizerBuilder object that contains data for harmonizing tokenized data
    * @param method: the method requested from the server
    * 
    * @return void: Simply print returned data so that the user can see it.
    */
    public static void handleResponse(String json, HarmonizationStateObject stateObject,
                                      String method) {
        //Setup a new object mapper for converting json to POJO
        ObjectMapper mapper = new ObjectMapper();

        //Convert returned JSON string to a list of DatabaseEntry objects.
        try {
            DatabaseEntryListWrapper dbEntries;

            //Handle returned dbEntries based on requested server method.
            switch (method) {
            case "fix":
                dbEntries = mapper.readValue(json, DatabaseEntryListWrapper.class);
                System.out.println(dbEntries.getEntryList());

                for (DatabaseEntry e : dbEntries.getEntryList()) {
                    System.out.println("\nFixed Code:");
                    System.out.println(stateObject.harmonize(e));
                            //tokenBuilder.harmonize(e.getFixedCode(),sourceFile));
                }
                break;
            case "echo":
                dbEntries = mapper.readValue(json, DatabaseEntryListWrapper.class);
                System.out.println(dbEntries.getEntryList());
                break;
            case "index":
                System.out.println(json.toString());
                break;
            default:
                dbEntries = mapper.readValue(json, DatabaseEntryListWrapper.class);
                System.out.println(dbEntries.getEntryList());

                for (DatabaseEntry e : dbEntries.getEntryList()) {
                    System.out.println("\nFixed Code:");
                    System.out.println(stateObject.harmonize(e));
                }
                break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //Check to see if index method.
        if(args.length == 3 && args[2].equals("index")) {
            ServerRequest serverRequest = new ServerRequest(args[0], args[1]);
            makeRequest(serverRequest, args[2]);
            System.exit(0);
        }
        //Grab arguments from the command line and setup variables
        if(args.length != 5) {
            System.out.println("Usage: java -jar <jar> <file> <error> <line # start> <line # end> <server" +
                    " method> | java - jar <jar> <table name> <n> <index>");
            System.exit(0);
        }

        String fileName = args[0];
        String errorMessage = args[1];
        int startLine = Integer.parseInt(args[2]);
        int endLine = Integer.parseInt(args[3]);
        String method = args[4];
        ServerRequest serverRequest;


        //Identify the buggy code block within the provided file
        String buggyCodeBlock = "";// NOTE: THIS ISN't ACTUALLY USED
        String wholeFileCode = "";
        try {
            buggyCodeBlock = getLinesFromFile(fileName, startLine, endLine);
            wholeFileCode = getLinesFromFile(fileName, -1,-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Buggy code block:\n\n" + buggyCodeBlock + "\n");

        //Tokenize the buggy code block and return as a TokenizerBuilder
        TokenizerBuilder tokenBuilder = null;
        try {
            tokenBuilder = tokenize(wholeFileCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Tokens:\n\n" + tokenBuilder.getString() + "\n");

        //Generate the serverRequest from the provided tokenized code
        //and the input error message.
        StringBuilder tokenizedCodeBlock = new StringBuilder();
        tokenizedCodeBlock.append(tokenBuilder.getString(startLine, endLine));
        serverRequest = new ServerRequest(tokenizedCodeBlock.toString(), errorMessage);

        //Make the request to the server for the desired method (fix, echo, index)        
        switch (method) {
            case "fix":
                System.out.println("\nFixing your bug...\n\n\n");
                makeRequest(serverRequest, new HarmonizationStateObject(tokenBuilder,
                        wholeFileCode,startLine,endLine), method);
                break;
            case "echo":
                System.out.println("\nEchoing your request...\n\n\n");
                makeRequest(serverRequest, tokenBuilder, null, method);
                break;
            default:
                System.out.println("Invalid method provided.");
                break;
        }
    }
}
