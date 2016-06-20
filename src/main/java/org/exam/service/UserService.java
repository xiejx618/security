package org.exam.service;

import org.exam.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created on 15/1/14.
 */
public interface UserService {
    Page<User> findAll(Pageable pageable);

    User save(User user);

    User findOne(Long id);

    void delete(Long id);

    User findByUsername(String username);

    User loadUserByUsername(String username);

}
