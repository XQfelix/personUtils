package com.common.dbutils;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class DuridDBUtil {
	
	private static final Logger logger = Logger.getLogger(DuridDBUtil.class);
	
	public DuridDBUtil() {
	}

	public  DruidDataSource getDruidDataSource (String dbType, String username, String password, String url) {
		return creatDataSource(dbType, username, password, url);
	}
	
	private DruidDataSource creatDataSource(String dbType, String username, String password, String url) {
		try {
			if (StringUtils.isEmpty(url.trim())) {
				throw new Exception("OUT DB param-url is empty!");
			}
			if (StringUtils.isEmpty(username)) {
				throw new Exception("OUT DB param-username is empty!");
			}
			if (StringUtils.isEmpty(password)) {
				throw new Exception("OUT DB param-password is empty!");
			}
			if (StringUtils.isEmpty(dbType)) {
				throw new Exception("OUT DB param-dbType is empty!");
			}
			String deviceClassName = DataBaseDrive.getDriverClassName(dbType);
			if (StringUtils.isEmpty(deviceClassName)) {
				throw new Exception("OUT DB param-dbType is not exist!");
			}
			
			DruidDataSource dataSource = new DruidDataSource();
			dataSource.setUrl(url);
			dataSource.setUsername(username);
			dataSource.setPassword(password);
			dataSource.setDriverClassName(deviceClassName);
			dataSource.setInitialSize(2);
			dataSource.setMaxActive(10);// 设置最大链接数
			dataSource.setMinIdle(2);// 最新连接数
			dataSource.setRemoveAbandoned(true);
			dataSource.setRemoveAbandonedTimeout(1800);
			dataSource.setMaxWait(60000);
			dataSource.setPoolPreparedStatements(true);
			dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
			dataSource.setConnectionErrorRetryAttempts(0);
			dataSource.setBreakAfterAcquireFailure(true);
			switch (dbType) {
			case "MYSQL":
				dataSource.setValidationQuery("select 1");
				break;
			case "ORACLE":
				dataSource.setValidationQuery("select 1 from dual");
				break;
			case "DB2":
				dataSource.setValidationQuery("select 1 from sysibm.sysdummy1");
				break;
			case "SYBASE":
//				dataSource.setValidationQuery("");
				break;
			case "SQLSERVER":
				dataSource.setValidationQuery("select 1");
				break;
			case "POSTGRESQL":
				dataSource.setValidationQuery("select version()");
				break;
			case "DM":
//				dataSource.setValidationQuery("");
				break;
			default:
				logger.error("Unsupported types");
				break;
			}
			logger.info("数据库连接---->"+dataSource.toString());
			return dataSource;
		} catch (Exception e) {
			logger.error("Create datasource error!", e);
			return null;
		}
	}

}
