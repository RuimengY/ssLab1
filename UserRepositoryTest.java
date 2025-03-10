package com.example.lab1;

import com.example.lab1.repository.UserRepository;
import com.example.lab1.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByUsername_WhenUserExists_ReturnsTrue() {
        // 创建并保存用户
        User user = new User("testuser", "encryptedPass");
        userRepository.save(user);

        // 验证用户名是否存在
        boolean exists = userRepository.existsByUsername("testuser");
        assertThat(exists).isTrue();
    }

    @Test
    void findByUsername_WhenUserExists_ReturnsUser() {
        User user = new User("testuser", "encryptedPass");
        userRepository.save(user);

        User foundUser = userRepository.findByUsername("testuser");
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
    }
}