package kz.salyqtez.broker.service;

import kz.salyqtez.broker.model.User;
import kz.salyqtez.broker.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveBlankUser(String userName, Long userId, String description) {
        User user = new User();
        user.setUser(userName);
        user.setBotUserId(userId);
        user.setDescription(description);
        userRepository.save(user);
    }
}
