package mohammed.payloadwatch.controllers;

import mohammed.payloadwatch.dto.UserSettingsResponse;
import mohammed.payloadwatch.dto.UserSettingsUpdateRequest;
import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.services.MonitorService;
import mohammed.payloadwatch.services.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;
    private final MonitorService monitorService;

    public UserController(UserService userService, MonitorService monitorService) {
        this.userService = userService;
        this.monitorService = monitorService;
    }

    @GetMapping
    public UserSettingsResponse getMySettings(@AuthenticationPrincipal Jwt jwt) {
        // 1. Get the user (JIT provisioning ensures they exist)
        User user = userService.getOrCreateUser(jwt);

        // 2. Count their monitors
        long monitorCount = monitorService.getAllMonitorsForUser(user.getCognitoSub()).size();

        // 3. Send back the packaged DTO
        return new UserSettingsResponse(
                user.getCognitoSub(),
                user.getEmail(),
                user.getPlanTier(),
                user.getTimeZonePreference(),
                user.getThemePreference(),
                user.isEmailAlertsEnabled(),
                monitorCount
        );
    }

    @PutMapping("/settings")
    public void updateMySettings(@AuthenticationPrincipal Jwt jwt, @RequestBody UserSettingsUpdateRequest request) {
        User user = userService.getOrCreateUser(jwt);

        // This uses the exact method we built in your UserService earlier!
        userService.updateUserSettings(
                user.getCognitoSub(),
                request.emailAlertsEnabled(),
                request.themePreference(),
                request.timeZonePreference()
        );
    }
}
