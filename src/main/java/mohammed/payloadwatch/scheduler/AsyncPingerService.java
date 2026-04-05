package mohammed.payloadwatch.scheduler;

import jakarta.transaction.Transactional;
import mohammed.payloadwatch.entities.Contract;
import mohammed.payloadwatch.entities.HealthLog;
import mohammed.payloadwatch.entities.Monitor;
import mohammed.payloadwatch.repositories.HealthLogRepository;
import mohammed.payloadwatch.repositories.MonitorRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AsyncPingerService {

    private final MonitorRepository monitorRepository;
    private final HealthLogRepository healthLogRepository;

    public AsyncPingerService(MonitorRepository monitorRepository, HealthLogRepository healthLogRepository) {
        this.monitorRepository = monitorRepository;
        this.healthLogRepository = healthLogRepository;
    }

    @Async
    @Transactional
    public void pingAndSave(Long monitorId, RestClient restClient) {
        Monitor monitor = monitorRepository.findByIdWithContracts(monitorId).orElse(null);
        if (monitor == null || !monitor.isActive()) {
            return;
        }

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
            statusCode = e.getStatusCode().value();
            errorMessage = "HTTP Error: " + statusCode;
            monitor.setCurrentStatus("DOWN");
        } catch (Exception e) {
            statusCode = 0;
            errorMessage = "Connection Failed: " + e.getMessage();
            monitor.setCurrentStatus("DOWN");
        }

        Instant end = Instant.now();
        int latencyCalcul = Math.toIntExact(end.toEpochMilli() - start.toEpochMilli());

        HealthLog healthLog = new HealthLog(monitor, statusCode, latencyCalcul, isSuccess, errorMessage);
        healthLogRepository.save(healthLog);

        monitor.setLastCheckedAt(Instant.now());
        monitorRepository.save(monitor);
    }
}
