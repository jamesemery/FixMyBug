package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.json.GsonJsonParser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;

@RestController
public class FixMyBugController {

    @RequestMapping("/fix")
    public DatabaseEntryListWrapper fixMyBug(@RequestBody String input) {
        //Setup the JSON object mapper and our DBConnection
    	ObjectMapper mapper = new ObjectMapper();
        DatabaseServer DBConnection = new DatabaseServer("/Users/fixmybug/FixMyBug/DB-Filler/uploadTestDB");

    	try {
    		//Convert JSON string to object
            ServerRequest serverRequest = mapper.readValue(input, ServerRequest.class);

	        System.out.println(serverRequest.getBuggyCode());
	        System.out.println(serverRequest.getErrorMessage());

	        // Create a DatabaseEntry and return it.
	        List<DatabaseEntry> database_entries = DBConnection.getMostSimilarEntries(serverRequest
                    .getBuggyCode());
            System.out.println(database_entries);
	        return DatabaseServer.sanitizeForJsonTransmission(new DatabaseEntryListWrapper
                    (database_entries));

    	} catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new DatabaseEntryListWrapper(new ArrayList<>());
        // TODO this needs to convert back from dangerous stings
    }

    @RequestMapping("/echo")
    public DatabaseEntryListWrapper echo(@RequestBody String input) {
        //Setup the JSON object mapper
        ObjectMapper mapper = new ObjectMapper();

        try {
            //Convert JSON string to object
            ServerRequest serverRequest = mapper.readValue(input, ServerRequest.class);

            System.out.println("BuggyCode received: " + serverRequest.getBuggyCode() + "\n");
            System.out.println("Error message received: " + serverRequest.getErrorMessage() + "\n");
            System.out.println("Echoing back the received data...\n\n\n");

            return DatabaseServer.sanitizeForJsonTransmission(new DatabaseEntryListWrapper(new DatabaseEntry(-1, "crap", "sdf", "squid", "Sdf"))); //TODO fix this outdated method


        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new DatabaseEntryListWrapper(new DatabaseEntry(-1, "crap", "sdf", "squid", "Sdf"))
                ; //
        // TODO fix this outdated method
    }

    // THIS IS 100% a hack, I am using the ServerRequest object to send arguments for the
    // indexing. I think there needs to be a more general purpose server request object than this
    @RequestMapping("/index")
    public String indexArray(@RequestBody String input) {
        //Setup the JSON object mapper
        ObjectMapper mapper = new ObjectMapper();
        DatabaseServer DBConnection = new DatabaseServer("/FixMyBugDB/TEST_DATABASE");

        try {
            //Convert JSON string to object
            ServerRequest serverRequest = mapper.readValue(input, ServerRequest.class);

            System.out.println(serverRequest.getBuggyCode());
            System.out.println(serverRequest.getErrorMessage());

            int rowsCreated = DBConnection.createIndex(Integer.parseInt(serverRequest
                            .getErrorMessage()), serverRequest.getBuggyCode());

            return "Success, created a "+serverRequest.getErrorMessage()+"gram " +
                    "index of table '"+serverRequest.getBuggyCode()+"' which created "+rowsCreated
                    +" lines of index";

        } catch (JsonGenerationException e) {
            e.printStackTrace();
            return "failed, JsonGenerationException";
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return "failed, JsonMappingException";
        } catch (IOException e) {
            e.printStackTrace();
            return "failed, IOException";
        } catch (SQLException e) {
            e.printStackTrace();
            return "failed, SQLException";
        }
    }
}
