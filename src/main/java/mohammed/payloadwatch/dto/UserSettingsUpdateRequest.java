package mohammed.payloadwatch.dto;

public record UserSettingsUpdateRequest(
        boolean emailAlertsEnabled,
        String themePreference,
        String timeZonePreference
) {}
