package mohammed.payloadwatch.scheduler;

import mohammed.payloadwatch.entities.Monitor;
import mohammed.payloadwatch.repositories.MonitorRepository;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.List;

public class PollingEngine {

    private final MonitorRepository monitorRepository;

    public PollingEngine(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    // Schedueled function to run every 60 seconds

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void pollEndpoints() {
        System.out.println("Polling endpoints for new payloads...");
        // Logic to poll endpoints and process payloads goes here

        List<Monitor> monitors = monitorRepository.findMonitorsDueForPingAndIsActive();
        for (Monitor monitor : monitors) {
            // Logic to ping the monitor's endpoint and process the response
            System.out.println("Pinging endpoint: " + monitor.getUrl());

            // After processing, update the monitor's last ping time
            monitor.setLastCheckedAt(Instant.now());
            monitorRepository.save(monitor);
        }
    }
}
