package mohammed.payloadwatch.services;

import mohammed.payloadwatch.entities.HealthLog;
import mohammed.payloadwatch.entities.Monitor;
import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.repositories.HealthLogRepository;
import org.springframework.stereotype.Service;

@Service
public class EmailAlertService {

    private final HealthLogRepository healthLogRepository;

    public EmailAlertService(HealthLogRepository healthLogRepository) {
        this.healthLogRepository = healthLogRepository;
    }

    public boolean downtimeAlert(User user, Monitor monitor) {
        if (user == null || monitor == null) {
            System.out.println("EmailAlertService: Missing data for downtime alert");
            return false;
        }

        HealthLog latestLog = healthLogRepository.findFirstByMonitorIdOrderByTimestampDesc(monitor.getId());

        if (latestLog == null) {
            System.out.println("EmailAlertService: No health logs found for monitor " + monitor.getId());
            return false;
        }

        // Send alert if downtime
        if (!latestLog.isSuccess()) {
            if(user.isEmailAlertsEnabled()) {
                // Send email
                System.out.println("EmailAlertService: Sending downtime alert email to " + user.getEmail());
            }
        }

        return true;
    }
}
