package com.common.dbutils;

/**
 * @Title DataBaseDrive.java
 * @author tz
 * @Todo TODO
 * @version V1.0
 * @Date 2017年6月21日下午1:51:12
 */
public enum DataBaseDrive {
//	SQLSERVER2000("com.microsoft.jdbc.sqlserver.SQLServerDriver"), 
//	SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
	MYSQL("com.mysql.cj.jdbc.Driver"),
	ORACLE("oracle.jdbc.OracleDriver"), 
	DB2("com.ibm.db2.jcc.DB2Driver"),
	SYBASE("net.sourceforge.jtds.jdbc.Driver"),
	SYBASEJCONN3("com.sybase.jdbc3.jdbc.SybDriver"),
	SQLSERVER("net.sourceforge.jtds.jdbc.Driver"),
	POSTGRESQL("org.postgresql.Driver"),
	ODBC("sun.jdbc.odbc.JdbcOdbcDriver"),
	DM("dm.jdbc.driver.DmDriver");
    /**
     * 数据库驱动
     */
    public String DRIVE;

    /**
     * [构造函数]
     *
     * @param drive
     */
    private DataBaseDrive( String drive) {
        this.DRIVE = drive;
    }

    /**
     * 获取数据库驱动
     *
     * @return
     */
    public String getDrive() {
        return this.DRIVE;
    }
    
    public static String getDriverClassName (String code) {
//    	String dString
		switch (code) {
		case "MYSQL":
			return DataBaseDrive.MYSQL.DRIVE;
		//		case "SQLSERVER2000":
//			return DataBaseDrive.SQLSERVER2000.DRIVE;
		case "SQLSERVER":
			return DataBaseDrive.SQLSERVER.DRIVE;
		case "ORACLE":
			return DataBaseDrive.ORACLE.DRIVE;
		case "DB2":
			return DataBaseDrive.DB2.DRIVE;
		case "SYBASE":
			return DataBaseDrive.SYBASE.DRIVE;
		case "SYBASEJCONN3":
			return DataBaseDrive.SYBASEJCONN3.DRIVE;
		case "ODBC":
			return DataBaseDrive.ODBC.DRIVE;
		case "POSTGRESQL":
			return DataBaseDrive.POSTGRESQL.DRIVE;
		case "DM":
			return DataBaseDrive.DM.DRIVE;
		default:
			return null;
		}
    }

}
