package mohammed.payloadwatch.controllers;

import mohammed.payloadwatch.dto.PasswordChangeRequest;
import mohammed.payloadwatch.dto.UserSettingsResponse;
import mohammed.payloadwatch.dto.UserSettingsUpdateRequest;
import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.services.MonitorService;
import mohammed.payloadwatch.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public UserSettingsResponse getMySettings(@AuthenticationPrincipal User user) {
        // get monitor count
        long monitorCount = monitorService.getAllMonitorsForUser(user.getId()).size();

        // return user stats
        return new UserSettingsResponse(
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getPlanTier(),
                user.getTimeZonePreference(),
                user.getThemePreference(),
                user.isEmailAlertsEnabled(),
                monitorCount
        );
    }

    @PutMapping("/settings")
    public void updateMySettings(@AuthenticationPrincipal User user, @RequestBody UserSettingsUpdateRequest request) {
        // apply settings update
        userService.updateUserSettings(
                user.getId(),
                request.emailAlertsEnabled(),
                request.themePreference(),
                request.timeZonePreference()
        );
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal User user, @RequestBody PasswordChangeRequest request) {
        boolean success = userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect current password.");
        }
    }
}
