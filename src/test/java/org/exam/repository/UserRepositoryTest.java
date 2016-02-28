package org.exam.repository;

import org.exam.config.AppConfig;
import org.exam.domain.Authority;
import org.exam.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by xin on 14/10/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@Transactional
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @Rollback(false)
    public void testSave() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("$2a$04$fCqcakHV2O.4AJgp3CIAGO9l5ZBq61Gt6YNzjcyC8M.js0ucpyun.");//admin
        userRepository.save(user);
    }

    @Test
    public void testFindAll() {
        Iterable<User> users = userRepository.findAll();
        for (User user : users) {
            System.out.println(user.getUsername());
        }
    }

    @Test
    public void testFindByUsername() {
        User user = userRepository.findByUsername("root");
        System.out.println(user);
    }
}
