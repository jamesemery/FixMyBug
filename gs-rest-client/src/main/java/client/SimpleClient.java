package client;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleClient {

    // http://localhost:8080/RESTfulExample/json/product/post
    public static void main(String[] args) {

        try {
            URL url = new URL("http://localhost:8080/fix");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            //Read in Java text file.
            String fileName = args[0];
            String input = "";
            String line = null;

            try {
                FileReader fileReader = new FileReader(fileName);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while((line = bufferedReader.readLine()) != null) {
                    input += line + "\n";
                }

                bufferedReader.close();

            } catch(FileNotFoundException ex) {
                System.out.println("Unable to open file.");
            }


            ObjectMapper mapper = new ObjectMapper();
            ClientFile clientFile = new ClientFile(input, "Compile-Error-Message");

            try {
                //Convert object to JSON string
                String jsonInString = mapper.writeValueAsString(clientFile);
                OutputStream os = conn.getOutputStream();
                os.write(jsonInString.getBytes());
                os.flush();
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            //     throw new RuntimeException("Failed : HTTP error code : "
            //             + conn.getResponseCode());
            // }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}
