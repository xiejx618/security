package org.exam.config;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Created on 15/1/7.
 */
@Configuration
@PropertySource("classpath:config.properties")
@ComponentScan(basePackages = "org.exam.service.impl")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.exam.repository.jpa")
@EnableMongoRepositories(basePackages = "org.exam.repository.mongo")
public class AppConfig {

    @Resource
    private Environment env;

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setDriverClassName(env.getProperty("ds.driverClassName"));
        dataSource.setUrl(env.getProperty("ds.url"));
        dataSource.setUsername(env.getProperty("ds.username"));
        dataSource.setPassword(env.getProperty("ds.password"));
        dataSource.setInitialSize(env.getProperty("ds.initialSize", Integer.class));
        dataSource.setMinIdle(env.getProperty("ds.minIdle", Integer.class));
        dataSource.setMaxIdle(env.getProperty("ds.maxIdle", Integer.class));
        dataSource.setMaxActive(env.getProperty("ds.maxActive", Integer.class));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.valueOf(env.getProperty("jpa.database")));
        jpaVendorAdapter.setGenerateDdl(env.getProperty("jpa.generateDdl", Boolean.class));
        jpaVendorAdapter.setShowSql(env.getProperty("jpa.showSql", Boolean.class));
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("org.exam.domain.entity");
        emf.setJpaVendorAdapter(jpaVendorAdapter);
        Properties properties = new Properties();
        properties.setProperty("hibernate.default_schema", env.getProperty("jpa.defaultSchema"));
        emf.setJpaProperties(properties);
        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory().getObject());
    }

    @Bean
    public MongoDbFactory mongoDbFactory() throws UnknownHostException {
        return new SimpleMongoDbFactory(new MongoClient(env.getProperty("mongo.host"), env.getProperty("mongo.port", Integer.class)), env.getProperty("mongo.db"));
    }

    @Bean
    public MongoTemplate mongoTemplate() throws UnknownHostException {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        mongoTemplate.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
        return mongoTemplate;
    }
}
