package org.exam.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;

/**
 * Created by xin on 15/1/7.
 */
@Configuration
@PropertySource("classpath:config.properties")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"org.exam.repository"})
@ComponentScan(basePackages = "org.exam.service")
public class AppConfig {
    @Resource
    private Environment env;

    @Bean(destroyMethod="close")
    public DataSource dataSource() {
        ComboPooledDataSource dataSource=new ComboPooledDataSource();
        try {dataSource.setDriverClass(env.getProperty("db.driverClass"));} catch (PropertyVetoException e) {e.printStackTrace();}
        dataSource.setJdbcUrl(env.getProperty("db.jdbcUrl"));
        dataSource.setUser(env.getProperty("db.user"));
        dataSource.setPassword(env.getProperty("db.password"));
        dataSource.setInitialPoolSize(Integer.valueOf(env.getProperty("db.initialPoolSize")));
        dataSource.setAcquireIncrement(Integer.valueOf(env.getProperty("db.acquireIncrement")));
        dataSource.setMinPoolSize(Integer.valueOf(env.getProperty("db.minPoolSize")));
        dataSource.setMaxPoolSize(Integer.valueOf(env.getProperty("db.maxPoolSize")));
        dataSource.setMaxIdleTime(Integer.valueOf(env.getProperty("db.maxIdleTime")));
        dataSource.setIdleConnectionTestPeriod(Integer.valueOf(env.getProperty("db.idleConnectionTestPeriod")));
        return dataSource;
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.valueOf(env.getProperty("jpa.database")));
        jpaVendorAdapter.setGenerateDdl(Boolean.parseBoolean(env.getProperty("jpa.generateDdl")));
        jpaVendorAdapter.setShowSql(Boolean.parseBoolean(env.getProperty("jpa.showSql")));
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("org.exam.domain");
        emf.setJpaVendorAdapter(jpaVendorAdapter);
        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}
