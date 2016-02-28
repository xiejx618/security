package org.exam.service.impl;

import org.exam.domain.Authority;
import org.exam.domain.Role;
import org.exam.domain.User;
import org.exam.repository.UserRepository;
import org.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xin on 15/1/14.
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findOne(Long id) {
        return userRepository.findOne(id);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public User loadUserByUsername(String username) {
        User user;
        try {
            user = userRepository.findByUsername(username);
            Set<Authority> authorities = new HashSet<>();
            for (Role role : user.getRoles()) {
                authorities.addAll(role.getAuthorities());
            }
            user.setAuthorities(authorities);
        } catch (Exception e) {
            return null;
        }
        return user;
    }


}
