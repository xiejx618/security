package org.exam.repository;

import org.exam.domain.User;

/**
 * Created by xin on 15/1/7.
 */
public interface UserRepository extends BaseRepository<User> {
    void deleteAllInBatch();

    User findByUsername(String username);
}
