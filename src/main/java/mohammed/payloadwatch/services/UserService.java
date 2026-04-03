package mohammed.payloadwatch.services;

import jakarta.transaction.Transactional;
import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.repositories.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreateUser(Jwt jwt) {
        String cognitoSub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        //1. Query to find the user by their Cognito sub
        User user = userRepository.findByCognitoSub(cognitoSub);

        if (user == null) {
            User newUser = new User(cognitoSub, email);
            return userRepository.save(newUser);
        }

        return user;
    }

    @Transactional
    void updateUserSettings(String cognitoSub, boolean emailAlertsEnabled, String themePreference, String timezonePreference) {
        try {
            User user = userRepository.findByCognitoSub(cognitoSub);
            if (user != null) {
                user.setEmailAlertsEnabled(emailAlertsEnabled);
                user.setThemePreference(themePreference);
                user.setTimeZonePreference(timezonePreference);
                userRepository.save(user);
            }
        } catch (Exception e) {
            System.out.println("Error updating user settings: " + e.getMessage());
        }
    }
}
