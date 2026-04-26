package mohammed.payloadwatch.controllers;

import mohammed.payloadwatch.dto.MonitorRequest;
import mohammed.payloadwatch.dto.MonitorResponse;
import mohammed.payloadwatch.entities.Monitor;
import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.services.MonitorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitors")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping()
    public List<MonitorResponse> getMonitorsByUserId(@AuthenticationPrincipal User user) {
        return monitorService.getAllMonitorsForUser(user.getId());
    }

    @PostMapping()
    public Monitor createMonitor(@AuthenticationPrincipal User user, @RequestBody MonitorRequest monitorRequest) {
        return monitorService.createMonitor(user.getId(), monitorRequest);
    }

    @PutMapping("/{monitorId}")
    public Monitor updateMonitor(@AuthenticationPrincipal User user, @PathVariable Long monitorId, @RequestBody MonitorRequest monitorRequest) {
        return monitorService.updateMonitor(user.getId(), monitorId, monitorRequest);
    }

    @DeleteMapping("/{monitorId}")
    public void deleteMonitor(@AuthenticationPrincipal User user, @PathVariable Long monitorId) {
        monitorService.deleteMonitor(user.getId(), monitorId);
    }

    @GetMapping("/{monitorId}/activity")
    public boolean getMonitorActivity(@AuthenticationPrincipal User user, @PathVariable Long monitorId) {
        return monitorService.getMonitorByIdAndUserId(monitorId, user.getId()).isActive();
    }

    @PatchMapping("/{monitorId}/activity")
    public void updateMonitorActivity(@AuthenticationPrincipal User user, @PathVariable Long monitorId, @RequestParam("isActive") Boolean isActive) {
        monitorService.updateMonitorActivity(monitorId, user.getId(), isActive);
    }
}
