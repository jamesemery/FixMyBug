package hello;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class GreetingController {

    @RequestMapping("/fix")
    public ClientCode clientCode(@RequestBody String input) {
        System.out.println(input);
        return new ClientCode(input);
    }
}
