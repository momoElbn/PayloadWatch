package mohammed.payloadwatch.scheduler;

import jakarta.transaction.Transactional;
import mohammed.payloadwatch.entities.Contract;
import mohammed.payloadwatch.entities.HealthLog;
import mohammed.payloadwatch.entities.Monitor;
import mohammed.payloadwatch.repositories.HealthLogRepository;
import mohammed.payloadwatch.repositories.MonitorRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class PollingEngine {

    private final MonitorRepository monitorRepository;
    private final HealthLogRepository healthLogRepository;
    private final RestClient restClient;

    public PollingEngine(MonitorRepository monitorRepository, HealthLogRepository healthLogRepository) {
        this.monitorRepository = monitorRepository;
        this.healthLogRepository = healthLogRepository;
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
    @Transactional
    public void pollEndpoints() {
        System.out.println("Polling endpoints for new payloads...");

        List<Monitor> monitors = monitorRepository.findMonitorsDueForPingAndIsActive();
        for (Monitor monitor : monitors) {

            Instant start = Instant.now();
            int statusCode;
            boolean isSuccess = false;
            String errorMessage;

            try {
                System.out.println("Pinging endpoint: " + monitor.getUrl());

                Map<String, Object> responseMap = restClient.method(HttpMethod.valueOf(monitor.getHttpMethod()))
                        .uri(monitor.getUrl())
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

                // If no exception was thrown, we know it's a 2xx success code
                statusCode = 200;

                boolean contractsPassed = true;
                StringBuilder errorMessageBuilder = new StringBuilder();

                if (responseMap != null) {
                    for (Contract contract: monitor.getContracts()) {
                        String expectedKey = contract.getExpectedKey();
                        String expectedtype = contract.getExpectedType();

                        if (!responseMap.containsKey(expectedKey)) {
                            errorMessageBuilder.append("Missing key: ").append(expectedKey).append(". ");
                            contractsPassed = false;
                            continue;
                        }

                        Object value = responseMap.get(expectedKey);
                        boolean typeMatches = switch (expectedtype.toUpperCase()) {
                            case "STRING" -> value instanceof String;
                            case "NUMBER" -> value instanceof Number;
                            case "BOOLEAN" -> value instanceof Boolean;
                            case "ARRAY" -> value instanceof List;
                            case "OBJECT" -> value instanceof Map;
                            default -> false;
                        };

                        if (!typeMatches) {
                            errorMessageBuilder.append("Type mismatch for key: ").append(expectedKey)
                                    .append(". Expected: ").append(expectedtype)
                                    .append(", but got: ").append(value.getClass().getSimpleName()).append(". ");
                            contractsPassed = false;
                        }
                    }
                } else {
                    errorMessageBuilder.append("Response body is empty. ");
                    contractsPassed = false;
                }

                if (contractsPassed) {
                    monitor.setCurrentStatus("UP");
                    isSuccess = true;
                    errorMessage = "No errors. All contracts passed.";
                } else {
                    monitor.setCurrentStatus("DOWN");
                    errorMessage = errorMessageBuilder.toString();
                }

            } catch (HttpStatusCodeException e) {
                // Catches HTTP errors like 404, 500, etc.
                statusCode = e.getStatusCode().value();
                errorMessage = "HTTP Error: " + statusCode;
                monitor.setCurrentStatus("DOWN");
            } catch (Exception e) {
                // Catches network failures, timeouts, bad URLs
                statusCode = 0;
                errorMessage = "Connection Failed: " + e.getMessage();
                monitor.setCurrentStatus("DOWN");
            }

            //Calculate latency and save the DB records OUTSIDE the try/catch
            //Guaranties that we always log and update the monitor, even if the request fails
            Instant end = Instant.now();
            int latencyCalcul = Math.toIntExact(end.toEpochMilli() - start.toEpochMilli());

            HealthLog healthLog = new HealthLog(monitor, statusCode, latencyCalcul, isSuccess, errorMessage);
            healthLogRepository.save(healthLog);

            monitor.setLastCheckedAt(Instant.now());
            monitorRepository.save(monitor);
        }
    }
}
