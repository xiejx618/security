package org.exam.service.impl;

import org.exam.domain.entity.Authority;
import org.exam.domain.entity.Role;
import org.exam.domain.entity.User;
import org.exam.repository.jpa.JpaUserRepo;
import org.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 15/1/14.
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JpaUserRepo jpaUserRepo;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpaUserRepo.findAll(pageable);
    }

    @Override
    public User save(User user) {
        return jpaUserRepo.save(user);
    }

    @Override
    public User findOne(Long id) {
        return jpaUserRepo.findOne(id);
    }

    @Override
    public void delete(Long id) {
        jpaUserRepo.delete(id);
    }

    @Override
    public User findByUsername(String username) {
        return jpaUserRepo.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String username) {
        User user = jpaUserRepo.findByUsername(username);
        if (user != null) {
            Set<Authority> authorities = new HashSet<>();
            for (Role role : user.getRoles()) {
                authorities.addAll(role.getAuthorities());
            }
            user.setAuthorities(authorities);
        }
        return user;
    }
}
