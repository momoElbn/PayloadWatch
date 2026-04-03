package mohammed.payloadwatch.repositories;

import mohammed.payloadwatch.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //Find a user by their Cognito sub
    User findByCognitoSub(String cognitoSub);
}
