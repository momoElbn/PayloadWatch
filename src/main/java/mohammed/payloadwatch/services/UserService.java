package mohammed.payloadwatch.services;

import jakarta.transaction.Transactional;
import mohammed.payloadwatch.entities.User;
import mohammed.payloadwatch.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void updateUserSettings(Long userId, boolean emailAlertsEnabled, String themePreference, String timezonePreference) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setEmailAlertsEnabled(emailAlertsEnabled);
                user.setThemePreference(themePreference);
                user.setTimeZonePreference(timezonePreference);
                userRepository.save(user);
            }
        } catch (Exception e) {
            System.out.println("Error updating user settings: " + e.getMessage());
        }
    }

    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}
