package client;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    * Sends a ClientFile object (file + error message) to the server
    * as a JSON string and processes its response.
    */
    public static void fixMyBug(ClientFile clientFile) {
        try {
            //Setup an HTTP POST request
            URL url = new URL("http://localhost:8080/fix");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            try {
                //Convert ClientFile object to JSON string
                String clientFileAsJsonString = mapper.writeValueAsString(clientFile);
                OutputStream os = conn.getOutputStream();
                os.write(clientFileAsJsonString.getBytes());
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
            System.out.println("Output from Server .... \n");
            StringBuilder returnedJsonStringBuilder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                returnedJsonStringBuilder.append(output).append("\n");
            }
            returnedJsonStringBuilder.setLength(returnedJsonStringBuilder.length() - 1); //remove extra newline
            String returnedJsonString = returnedJsonStringBuilder.toString();

            ClientFile receivedBugFix = mapper.readValue(returnedJsonString, ClientFile.class);
            System.out.println(receivedBugFix.getFileContent());
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java -jar <jar> <file>");
            System.exit(0);
        }
        String fileName = args[0];
        String errorMessage = "Custom-Error-Message";
        StringBuilder stringBuilder = new StringBuilder();

        try {
            stringBuilder.append(fileToString(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ClientFile clientFile = new ClientFile(stringBuilder.toString(), errorMessage);

        fixMyBug(clientFile);
    }
}
