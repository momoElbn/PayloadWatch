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
import org.springframework.stereotype.Service;

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
    public List<Monitor> getMonitorsByCognitoSub(Long userId) {
        return monitorRepository.findByUserId(userId);
    }

    public List<MonitorResponse> getAllMonitorsForUser(Long userId) {
        List<Monitor> monitors = monitorRepository.findByUserId(userId);
        List<MonitorResponse> monitorResponses = new ArrayList<>();

        for (Monitor monitor: monitors) {
            Long id = monitor.getId();
            String name = monitor.getName();
            String url = monitor.getUrl();
            String httpMethod = monitor.getHttpMethod();
            int interval = monitor.getIntervalInMinutes();
            String status = monitor.getCurrentStatus();

            List<ContractDto> contractDtos = new ArrayList<>();

            monitor.getContracts().forEach(contract -> {
                String expectedKey = contract.getExpectedKey();
                String expectedtype = contract.getExpectedType();
                contractDtos.add(new ContractDto(expectedKey, expectedtype));
            });

            monitorResponses.add(new MonitorResponse(id, name, url, httpMethod, interval, status, contractDtos));
        }

        return monitorResponses;
    }

    // CREATE
    @Transactional
    public Monitor createMonitor(String cognitoSub, MonitorRequest request) {
        User user = userRepository.findByCognitoSub(cognitoSub);

        if (user == null) {
            System.out.println("User not found for cognitoSub: " + cognitoSub);
            return null;
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
    public Monitor updateMonitor(Long userId, Long monitorId, MonitorRequest request) {
        Monitor monitor = monitorRepository.findByIdAndUserId(monitorId, userId);

        if (monitor == null) {
            System.out.println("Monitor not found for id: " + monitorId + " and userId: " + userId);
            return null;
        }

        monitor.setName(request.getName());
        monitor.setUrl(request.getUrl());
        monitor.setHttpMethod(request.getHttpMethod());
        monitor.setIntervalInMinutes(request.getInterval());

        // Clear existing contracts and add new ones
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
    public Monitor updateMonitorStatus(Long monitorId, String status, Long userId) {
        Monitor monitor = monitorRepository.findByIdAndUserId(monitorId, userId);

        if (monitor == null) {
            System.out.println("Monitor not found for id: " + monitorId + " and userId: " + userId);
            return null;
        }

        monitor.setCurrentStatus(status);
        monitor.setLastCheckedAt(java.time.Instant.now());

        return monitorRepository.save(monitor);
    }

    // DELETE
    @Transactional
    public boolean deleteMonitor(Long userId, Long monitorId) {
        Monitor monitor = monitorRepository.findByIdAndUserId(monitorId, userId);

        if (monitor == null) {
            System.out.println("Monitor not found for id: " + monitorId + " and userId: " + userId);
            return false;
        }

        monitorRepository.delete(monitor);
        return true;
    }
}
