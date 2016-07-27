package com.devero.persistence.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CurrentHibernateSession {

  @Autowired
  static SessionFactory sessionFactory;

  private static Map<String, String> masterDatabase = new HashMap<>();

  private static Map<String, String> readWriteDatabase = new HashMap<>();

  private static final Logger logger = LoggerFactory.getLogger(CurrentHibernateSession.class);

  private static final String TOMCAT_CONF_DIRECTORY = "conf";

  private static final String TOMCAT_CATALINA_BASE = "catalina.base";

  private static final String DATASOURCE_PROPERTIES_FILE = "datasource.properties";

  private final Properties dateSourceProperties;

  public CurrentHibernateSession() {

    dateSourceProperties = new Properties();
    try {

      final InputStream dataSourceInputStream = new FileInputStream(new File(
          new File(System.getProperty(TOMCAT_CATALINA_BASE), TOMCAT_CONF_DIRECTORY), DATASOURCE_PROPERTIES_FILE));
      dateSourceProperties.load(dataSourceInputStream);

      for (String key : dateSourceProperties.stringPropertyNames()) {
        if ("default".equalsIgnoreCase(key)) {
          continue;
        }
          
        String lstDatasources = dateSourceProperties.getProperty(key);
        String[] arrDataSources = StringUtils.commaDelimitedListToStringArray(lstDatasources);
        masterDatabase.put(key, arrDataSources[0]);
        readWriteDatabase.put(key, arrDataSources[1]);
      }

    } catch (final IOException e) {
      logger.error("IOException while instantiating default datasource " + e.getMessage());
    }
  }

  public static Session getMasterSession() {

    final ApplicationContext applicationContext = SpringConfiguration.getSpringContext();
    if (null == applicationContext) {
      throw new IllegalStateException("Application Context is null");
    }

    final Site site = applicationContext.getBean(Site.class);
    String databaseKey = "";
    logger.info("Database key before retrieving from site " + databaseKey);
    if (!StringUtils.isEmpty(site.getSiteName())) {
      databaseKey = masterDatabase.get(site.getSiteName());
    }

    return sessionFactory.withOptions().tenantIdentifier(databaseKey).openSession();

  }

  public Session getReadWriteSession() {

    return null;

  }

}
