package org.exam.repository.jpa;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

/**
 * Created on 16/2/28.
 */
@NoRepositoryBean
public interface JpaBaseRepo<T extends Serializable> extends PagingAndSortingRepository<T, Long>, QueryDslPredicateExecutor<T> {

}
