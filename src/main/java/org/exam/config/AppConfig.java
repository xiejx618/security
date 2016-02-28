package org.exam.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

/**
 * Created by xin on 15/1/7.
 */
@Configuration
@PropertySource("classpath:config.properties")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"org.exam.repository"})
public class AppConfig {
    @Resource
    private Environment env;

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {
            dataSource.setDriverClass(env.getProperty("c3p0.driverClass"));
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        dataSource.setJdbcUrl(env.getProperty("c3p0.jdbcUrl"));
        dataSource.setUser(env.getProperty("c3p0.user"));
        dataSource.setPassword(env.getProperty("c3p0.password"));
        dataSource.setInitialPoolSize(Integer.valueOf(env.getProperty("c3p0.initialPoolSize")));
        dataSource.setAcquireIncrement(Integer.valueOf(env.getProperty("c3p0.acquireIncrement")));
        dataSource.setMinPoolSize(Integer.valueOf(env.getProperty("c3p0.minPoolSize")));
        dataSource.setMaxPoolSize(Integer.valueOf(env.getProperty("c3p0.maxPoolSize")));
        dataSource.setMaxIdleTime(Integer.valueOf(env.getProperty("c3p0.maxIdleTime")));
        dataSource.setIdleConnectionTestPeriod(Integer.valueOf(env.getProperty("c3p0.idleConnectionTestPeriod")));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(true);
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update");//validate,create,create-drop
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("org.exam.domain");
        emf.setJpaVendorAdapter(jpaVendorAdapter);
        emf.setJpaProperties(jpaProperties);
        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}
