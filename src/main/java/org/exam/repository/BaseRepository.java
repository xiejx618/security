package org.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Created by xin on 16/2/28.
 */
@NoRepositoryBean
public interface BaseRepository<T extends Serializable> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

}
