package org.exam.repository;

import org.exam.domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by xin on 15/1/7.
 */
public interface UserRepository extends BaseRepository<User> {
    void deleteAllInBatch();
    User findByUsername(String username);
}
