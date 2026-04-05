package mohammed.payloadwatch.services;

import jakarta.transaction.Transactional;
import mohammed.payloadwatch.dto.ContractDto;
import mohammed.payloadwatch.dto.MonitorRequest;
import mohammed.payloadwatch.dto.MonitorResponse;
import mohammed.payloadwatch.entities.Contract;
import mohammed.payloadwatch.entities.Monitor;
import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.repositories.MonitorRepository;
import mohammed.payloadwatch.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final UserRepository userRepository;

    public MonitorService(MonitorRepository monitorRepository, UserRepository userRepository) {
        this.monitorRepository = monitorRepository;
        this.userRepository = userRepository;
    }

    // READ

    public List<MonitorResponse> getAllMonitorsForUser(String cognitoSub) {
        List<Monitor> monitors = monitorRepository.findByUserCognitoSub(cognitoSub);
        List<MonitorResponse> monitorResponses = new ArrayList<>();

        for (Monitor monitor: monitors) {
            Long id = monitor.getId();
            String name = monitor.getName();
            String url = monitor.getUrl();
            String httpMethod = monitor.getHttpMethod();
            int interval = monitor.getIntervalInMinutes();
            String status = monitor.getCurrentStatus();
            boolean active = monitor.isActive();

            List<ContractDto> contractDtos = new ArrayList<>();

            monitor.getContracts().forEach(contract -> {
                String expectedKey = contract.getExpectedKey();
                String expectedtype = contract.getExpectedType();
                contractDtos.add(new ContractDto(expectedKey, expectedtype));
            });

            monitorResponses.add(new MonitorResponse(id, name, url, httpMethod, interval, status, active, contractDtos));
        }

        return monitorResponses;
    }

    public Monitor getMonitorByIdAndCognitoSub(Long monitorId, String cognitoSub) {
        return monitorRepository.findByUserCognitoSubAndMonitorId(cognitoSub, monitorId);
    }

    // CREATE
    @Transactional
    public Monitor createMonitor(String cognitoSub, MonitorRequest request) {
        User user = userRepository.findByCognitoSub(cognitoSub);

        if (user == null) {
            System.out.println("User not found for cognitoSub: " + cognitoSub);
            return null;
        }

        // Get current monitor count
        int currentMonitorCount = getAllMonitorsForUser(cognitoSub).size();

        // Enforce monitor limits
        boolean isPro = "PRO".equalsIgnoreCase(user.getPlanTier());
        int maxMonitors = isPro ? 50 : 5;

        if (currentMonitorCount >= maxMonitors) {
            String errorMessage = isPro
                ? "You have reached your monitor limit (" + maxMonitors + ")."
                : "You have reached your monitor limit (" + maxMonitors + "). Please upgrade your plan for more monitors.";

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    errorMessage
            );
        }

        Monitor monitor = new Monitor();
        monitor.setUser(user);
        monitor.setName(request.getName());
        monitor.setUrl(request.getUrl());
        monitor.setHttpMethod(request.getHttpMethod());
        monitor.setIntervalInMinutes(request.getInterval());
        monitor.setCurrentStatus("UNKNOWN");

        for(ContractDto contractDto : request.getContracts()) {
            Contract contract = new Contract();

            contract.setMonitor(monitor);
            contract.setExpectedKey(contractDto.getExpectedKey());
            contract.setExpectedType(contractDto.getExpectedType());

            monitor.getContracts().add(contract);
        }

        return monitorRepository.save(monitor);
    }

    // UPDATE
    @Transactional
    public Monitor updateMonitor(String cognitoSub, Long monitorId, MonitorRequest request) {
        Monitor monitor = monitorRepository.findByUserCognitoSubAndMonitorId(cognitoSub, monitorId);

        if (monitor == null) {
            System.out.println("Monitor not found for id: " + monitorId + " and cognitoSub: " + cognitoSub);
            return null;
        }

        monitor.setName(request.getName());
        monitor.setUrl(request.getUrl());
        monitor.setHttpMethod(request.getHttpMethod());
        monitor.setIntervalInMinutes(request.getInterval());

        // Update contracts
        monitor.getContracts().clear();
        for(ContractDto contractDto : request.getContracts()) {
            Contract contract = new Contract();

            contract.setMonitor(monitor);
            contract.setExpectedKey(contractDto.getExpectedKey());
            contract.setExpectedType(contractDto.getExpectedType());

            monitor.getContracts().add(contract);
        }

        return monitorRepository.save(monitor);
    }

    @Transactional
    public Monitor updateMonitorActivity(Long monitorId, String cognitoSub, Boolean isActive) {
        Monitor monitor = monitorRepository.findByUserCognitoSubAndMonitorId(cognitoSub, monitorId);

        if (monitor == null) {
            System.out.println("Monitor not found for id: " + monitorId + " and cognitoSub: " + cognitoSub);
            return null;
        }

        monitor.setActive(isActive);

        return monitorRepository.save(monitor);
    }

    // DELETE
    @Transactional
    public void deleteMonitor(String cognitoSub, Long monitorId) {
        Monitor monitor = monitorRepository.findByUserCognitoSubAndMonitorId(cognitoSub, monitorId);

        if (monitor == null) {
            System.out.println("Monitor not found for id: " + monitorId + " and cognitoSub: " + cognitoSub);
            return;
        }

        monitorRepository.delete(monitor);
    }
}
