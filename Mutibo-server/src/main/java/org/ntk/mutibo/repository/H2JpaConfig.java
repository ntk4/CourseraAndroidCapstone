package org.ntk.mutibo.repository;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

@Configuration
//@EnableTransactionManagement
@ComponentScan(basePackages = { "org.ntk.mutibo.repository" })
@PropertySource("classpath:application.properties")
public class H2JpaConfig {

	private static final String DB_FILE_NAME = "db/mutibo_db";

	/**
	 * 
	 * This is used to setup the database. It will load the schema.sql file which does a create table so we have a table
	 * to work with in the project
	 */
	@Bean
	public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
		ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
		// if (!new File(DB_FILE_NAME + ".lck").exists())
		// resourceDatabasePopulator.addScript(new ClassPathResource("/Mutibo_create.sql"));

		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDataSource(dataSource);
		dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
		return dataSourceInitializer;
	}

	/**
	 * 
	 * This will be setting up a datasource using HyperSQL (hsqldb) in file
	 */
	@Bean
	public DataSource hsqlDataSource() {
		BasicDataSource basicDataSource = new BasicDataSource();
		basicDataSource.setDriverClassName(org.h2.Driver.class.getName());
		basicDataSource.setUsername("sa");
		basicDataSource.setPassword("");
		// basicDataSource.setUrl("jdbc:h2:mem:mutibo_db");
		basicDataSource.setUrl("jdbc:h2:file:" + DB_FILE_NAME);

		return basicDataSource;
	}

	/**
	 * 
	 * This setups the session factory
	 */
	@Bean
	public LocalSessionFactoryBean sessionFactory(Environment environment, DataSource dataSource) {

		/**
		 * 
		 * Getting packageOfModelBean from package of message bean
		 * 
		 */
		// String packageOfModelBeans = Message.class.getPackage().getName();

		LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();

		factoryBean.setDataSource(dataSource);
		factoryBean.setHibernateProperties(buildHibernateProperties(environment));
		factoryBean.setPackagesToScan("org.ntk.mutibo.repository");
		return factoryBean;
	}

	/**
	 * 
	 * Loading all the hibernate properties from a properties file
	 */
	protected Properties buildHibernateProperties(Environment env) {
		Properties hibernateProperties = new Properties();

		hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
		hibernateProperties.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
		hibernateProperties.setProperty("hibernate.use_sql_comments", env.getProperty("hibernate.use_sql_comments"));
		hibernateProperties.setProperty("hibernate.format_sql", env.getProperty("hibernate.format_sql"));
		hibernateProperties.setProperty("spring.jpa.hibernate.ddl-auto",
				env.getProperty("spring.jpa.hibernate.ddl-auto"));

		hibernateProperties.setProperty("hibernate.generate_statistics",
				env.getProperty("hibernate.generate_statistics"));

		hibernateProperties.setProperty("javax.persistence.validation.mode",
				env.getProperty("javax.persistence.validation.mode"));

		// Audit History flags
		hibernateProperties.setProperty("org.hibernate.envers.store_data_at_delete",
				env.getProperty("org.hibernate.envers.store_data_at_delete"));
		hibernateProperties.setProperty("org.hibernate.envers.global_with_modified_flag",
				env.getProperty("org.hibernate.envers.global_with_modified_flag"));

		return hibernateProperties;
	}

	/**
	 * 
	 * This is setting up the hibernate transaction manager
	 * 
	 */
	// @Bean
	// public HibernateTransactionManager hibernateTransactionManager(SessionFactory sessionFactory) {
	// return new HibernateTransactionManager(sessionFactory);
	// }

}