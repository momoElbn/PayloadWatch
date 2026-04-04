package mohammed.payloadwatch.controllers;

import mohammed.payloadwatch.dto.MonitorRequest;
import mohammed.payloadwatch.dto.MonitorResponse;
import mohammed.payloadwatch.entities.Monitor;
import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.services.MonitorService;
import mohammed.payloadwatch.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/monitors")
public class MonitorController {

    private final MonitorService monitorService;
    private final UserService userService;

    public MonitorController(MonitorService monitorService, UserService userService) {
        this.userService = userService;
        this.monitorService = monitorService;
    }

    @GetMapping()
    public List<MonitorResponse> getMonitorsByCognitoSub(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.getOrCreateUser(jwt);

        return monitorService.getAllMonitorsForUser(user.getCognitoSub());
    }

    @PostMapping()
    public Monitor createMonitor(@AuthenticationPrincipal Jwt jwt, @RequestBody MonitorRequest monitorRequest) {
        User user = userService.getOrCreateUser(jwt);

        return monitorService.createMonitor(user.getCognitoSub(), monitorRequest);
    }

    @PutMapping("/{monitorId}")
    public Monitor updateMonitor(@AuthenticationPrincipal Jwt jwt, @PathVariable Long monitorId, @RequestBody MonitorRequest monitorRequest) {
        User user = userService.getOrCreateUser(jwt);

        return monitorService.updateMonitor(user.getCognitoSub(), monitorId, monitorRequest);
    }

    @DeleteMapping("/{monitorId}")
    public void deleteMonitor(@AuthenticationPrincipal Jwt jwt, @PathVariable Long monitorId) {
        User user = userService.getOrCreateUser(jwt);

        monitorService.deleteMonitor(user.getCognitoSub(), monitorId);
    }

    @PatchMapping("/{monitorId}/activity")
    public Monitor updateMonitorActivity(@AuthenticationPrincipal Jwt jwt, @PathVariable Long monitorId, @RequestParam Boolean isActive) {
        User user = userService.getOrCreateUser(jwt);

        return monitorService.updateMonitorActivity(monitorId, user.getCognitoSub(), isActive);
    }
}
