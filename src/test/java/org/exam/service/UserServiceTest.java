package org.exam.service;

import org.exam.config.AppConfig;
import org.exam.domain.entity.Authority;
import org.exam.domain.entity.Role;
import org.exam.domain.entity.User;
import org.exam.repository.jpa.JpaAuthorityRepo;
import org.exam.repository.jpa.JpaRoleRepo;
import org.exam.repository.jpa.JpaUserRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by on 16/6/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@Transactional("transactionManager")
public class UserServiceTest {
    @Autowired
    private JpaUserRepo jpaUserRepo;
    @Autowired
    private JpaAuthorityRepo jpaAuthorityRepo;
    @Autowired
    private JpaRoleRepo jpaRoleRepo;

    @Test
    @Rollback(false)
    public void testSave() {
        Authority authority1 = new Authority();
        authority1.setName("查看用户");
        authority1.setAuthority("USER_QUERY");
        jpaAuthorityRepo.save(authority1);
        Authority authority2 = new Authority();
        authority2.setName("保存用户");
        authority2.setAuthority("USER_SAVE");
        jpaAuthorityRepo.save(authority2);
        Authority authority3 = new Authority();
        authority3.setName("删除用户");
        authority3.setAuthority("USER_DELETE");
        jpaAuthorityRepo.save(authority3);
        Role role1 = new Role();
        role1.setName("管理员");
        role1.setAuthorities(new HashSet<>(Arrays.asList(authority2, authority3)));
        jpaRoleRepo.save(role1);
        User user1 = new User();
        user1.setUsername("admin");
        user1.setPassword("$2a$04$fCqcakHV2O.4AJgp3CIAGO9l5ZBq61Gt6YNzjcyC8M.js0ucpyun.");//admin
        user1.setRoles(new HashSet<>(Collections.singletonList(role1)));
        jpaUserRepo.save(user1);
    }

    @Test
    public void testFindAll() {
        Iterable<User> users = jpaUserRepo.findAll();
        for (User user : users) {
            System.out.println(user.getUsername());
        }
    }

    @Test
    public void testFindByUsername() {
        User user = jpaUserRepo.findByUsername("root");
        System.out.println(user);
    }

}