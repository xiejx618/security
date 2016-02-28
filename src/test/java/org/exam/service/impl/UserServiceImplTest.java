package org.exam.service.impl;

import org.exam.config.AppConfig;
import org.exam.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * Created by xin on 16/2/28.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@Transactional
public class UserServiceImplTest {
    @Autowired
    private UserService userService;
    @Test
    public void testFindAll() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testFindOne() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testFindByUsername() throws Exception {

    }

    @Test
    public void testLoadUserByUsername() throws Exception {

    }
}