package org.exam.repository;

import org.exam.config.AppConfig;
import org.exam.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void testSave() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");
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
    public void findByUsername() {
        User user = userRepository.findByUsername("root");
        System.out.println(user);
    }


}
