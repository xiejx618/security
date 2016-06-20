package org.exam.repository.mongo;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

/**
 * Created by on 16/6/19.
 */
@NoRepositoryBean
public interface MongoBaseRepo<T extends Serializable> extends PagingAndSortingRepository<T, String>, QueryDslPredicateExecutor<T> {
}
