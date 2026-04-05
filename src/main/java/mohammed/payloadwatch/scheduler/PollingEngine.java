package mohammed.payloadwatch.scheduler;

import mohammed.payloadwatch.entities.Monitor;
import mohammed.payloadwatch.repositories.MonitorRepository;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PollingEngine {

    private final MonitorRepository monitorRepository;
    private final AsyncPingerService asyncPingerService;
    private final RestClient restClient;

    public PollingEngine(MonitorRepository monitorRepository, AsyncPingerService asyncPingerService) {
        this.monitorRepository = monitorRepository;
        this.asyncPingerService = asyncPingerService;
        // Configure a strict 5-second timeout
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds to establish a connection
        factory.setReadTimeout(5000);    // 5 seconds to finish reading the data

        // Build the RestClient with our custom timeout factory
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Scheduled(fixedRate = 10000)
    public void pollEndpoints() {
        System.out.println("Polling endpoints for new payloads...");

        List<Monitor> monitors = monitorRepository.findMonitorsDueForPingAndIsActive();
        for (Monitor monitor : monitors) {
            asyncPingerService.pingAndSave(monitor.getId(), restClient);
        }
    }
}
