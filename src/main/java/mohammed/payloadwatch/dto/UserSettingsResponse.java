package mohammed.payloadwatch.dto;

public record UserSettingsResponse(
        String accountId, // This will be the cognito_sub
        String email,
        String planTier,
        String timeZonePreference,
        String themePreference,
        boolean emailAlertsEnabled,
        long monitorsTracked
) {}
