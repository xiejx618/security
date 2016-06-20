package org.exam.repository.jpa;

import org.exam.domain.entity.User;

/**
 * Created on 15/1/7.
 */
public interface JpaUserRepo extends JpaBaseRepo<User> {
    void deleteAllInBatch();

    User findByUsername(String username);
}
