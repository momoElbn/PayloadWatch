package mohammed.payloadwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PayloadWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayloadWatchApplication.class, args);
    }

}
