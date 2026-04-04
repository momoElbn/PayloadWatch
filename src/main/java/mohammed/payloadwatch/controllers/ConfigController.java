package mohammed.payloadwatch.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class ConfigController {

    @Value("${aws.cognito.client-id}")
    private String cognitoClientId;

    @Value("${aws.cognito.region}")
    private String cognitoRegion;

    // returns public config needed by the frontend
    @GetMapping("/config")
    public Map<String, String> getPublicConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("cognitoClientId", cognitoClientId);
        config.put("cognitoRegion", cognitoRegion);
        return config;
    }
}

