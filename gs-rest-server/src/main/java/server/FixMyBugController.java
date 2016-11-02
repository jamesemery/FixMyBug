package server;

import java.util.concurrent.atomic.AtomicLong;
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
    public DatabaseEntry fixMyBug(@RequestBody String input) {
        //Setup the JSON object mapper and our DBConnection
    	ObjectMapper mapper = new ObjectMapper();
        DatabaseServer DBConnection = new DatabaseServer("/FixMyBugDB/TEST_DATABASE");

    	try {
    		//Convert JSON string to object
            ServerRequest serverRequest = mapper.readValue(input, ServerRequest.class);

	        System.out.println(serverRequest.getBuggyCode());
	        System.out.println(serverRequest.getErrorMessage());
	        
	        // Create a DatabaseEntry and return it.
	        DatabaseEntry database_entry = DBConnection.SelectAll(serverRequest.getBuggyCode());
	        return database_entry;


    	} catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new DatabaseEntry(-1, -2, "crap", "squid", -5);
    }

    @RequestMapping("/test")
    public DatabaseEntry echo(@RequestBody String input) {
        //Setup the JSON object mapper
        ObjectMapper mapper = new ObjectMapper();

        try {
            //Convert JSON string to object
            ServerRequest serverRequest = mapper.readValue(input, ServerRequest.class);

            System.out.println(serverRequest.getBuggyCode());
            System.out.println(serverRequest.getErrorMessage());

            return new DatabaseEntry(-1, -2, serverRequest.getBuggyCode(), serverRequest.getErrorMessage(), -5);


        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new DatabaseEntry(-1, -2, "crap", "squid", -5);
    }
}
