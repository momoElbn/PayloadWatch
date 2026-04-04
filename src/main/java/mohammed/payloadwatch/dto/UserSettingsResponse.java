package mohammed.payloadwatch.dto;

public record UserSettingsResponse(
        String accountId, // user cognito sub
        String email,
        String planTier,
        String timeZonePreference,
        String themePreference,
        boolean emailAlertsEnabled,
        long monitorsTracked
) {}
