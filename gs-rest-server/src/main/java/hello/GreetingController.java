package hello;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;

@RestController
public class GreetingController {

    @RequestMapping("/fix")
    public ClientFile clientCode(@RequestBody String input) {
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		//Convert JSON string to object
    		ClientFile clientFile = mapper.readValue(input, ClientFile.class);

	        System.out.println(clientFile.getFileContent());
	        System.out.println(clientFile.getErrorMessage());

            clientFile.setErrorMessage("Now we can edit what we receive!");

	        return clientFile;

    	} catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ClientFile("File Contents: ERROR", "ERRORERRORERROR");
    }
}
