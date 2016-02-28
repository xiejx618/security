package org.exam.repository;

import org.exam.config.AppConfig;
import org.exam.domain.Authority;
import org.exam.domain.Role;
import org.exam.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by xin on 14/10/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@Transactional
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Test
    @Rollback(false)
    public void testSave() {
        Authority authority1=new Authority();
        authority1.setName("查看用户");
        authority1.setAuthority("USER_QUERY");
        authorityRepository.save(authority1);
        Authority authority2=new Authority();
        authority2.setName("保存用户");
        authority2.setAuthority("USER_SAVE");
        authorityRepository.save(authority2);
        Authority authority3=new Authority();
        authority3.setName("删除用户");
        authority3.setAuthority("USER_DELETE");
        authorityRepository.save(authority3);
        Role role1=new Role();
        role1.setName("管理员");
        role1.setAuthorities(new HashSet<>(Arrays.asList(authority2, authority3)));
        roleRepository.save(role1);
        User user1=new User();
        user1.setUsername("admin");
        user1.setPassword("$2a$04$fCqcakHV2O.4AJgp3CIAGO9l5ZBq61Gt6YNzjcyC8M.js0ucpyun.");//admin
        user1.setRoles(new HashSet<>(Arrays.asList(role1)));
        userRepository.save(user1);
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
