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
    public DatabaseEntry clientCode(@RequestBody String input) {
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		//Convert JSON string to object
    		ServerRequest server_request = mapper.readValue(input, ServerRequest.class);

	        System.out.println(server_request.getBuggyCode());
	        System.out.println(server_request.getErrorMessage());
	        
	        // Create a DatabaseEntry and return it.
	        // Several of these fields are not from the database, but could be!
	        DatabaseEntry database_entry = new DatabaseEntry();
	        database_entry.setId(-1);
	        database_entry.setErrorType(-2);
	        database_entry.setBuggyCode(server_request.getBuggyCode());
	        database_entry.setFixedCode(Test.Connect(server_request.getBuggyCode()));
	        database_entry.setCount(-3);
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
}
