package com.common.dbutils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.proxy.DruidDriver;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DbUtil {

	private static final Logger logger = Logger.getLogger(DbUtil.class);

	private static final ConcurrentHashMap<String, DruidDataSource> DATASOURCE_MAP = new ConcurrentHashMap<String, DruidDataSource>();

	private String username;
	private String password;
	private String url;
	private String dbType;

	public DbUtil(String dbType, String username, String password, String url) {
		this.username = username;
		this.password = password;
		this.url = url;
		this.dbType = dbType;
		init();
	}

	public void init() {
		DuridDBUtil dbUtil = new DuridDBUtil();
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		if (dataSource == null || dataSource.isClosed()) {
			dataSource = dbUtil.getDruidDataSource(dbType, username, password, url);
			DATASOURCE_MAP.put(url, dataSource);
		}
	}

	/**
	 * 批量保存数据
	 *
	 * @param sql
	 * @param datas
	 * @param batch
	 */
	public void executeBatch(String sql, List<Map<Integer, Object>> datas, Integer batch) throws Exception{

		long start = System.currentTimeMillis();
		PreparedStatement pst = null;
		Connection conn = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		try {
			if (dataSource == null || dataSource.isClosed()) {
				init();
			}
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			pst = conn.prepareStatement(sql);
			int paramsSize = datas.size();
			for (int i = 0; i < paramsSize; i++) {
				Map<Integer, Object> data = datas.get(i);
				for (Integer key : data.keySet()) {
					pst.setObject(key, data.get(key));
				}
				pst.addBatch();
				if ((i != 0 && (i + 1) % batch == 0) || i == paramsSize - 1) {
					pst.executeBatch();
					pst.clearBatch();
				}
			}
			conn.commit();
		} finally {
			close(null, pst, conn);
		}
		long end = System.currentTimeMillis();
		logger.info("Batch execution time consuming " + (end - start) + "ss");
	}

	/**
	 * 批量保存数据
	 *
	 * @param tableName
	 * @param datas
	 */
	public int executeBatchByTable(String tableName, List<Map<String, Object>> datas, int batch) throws Exception{
		int affectRowCount = -1;
		Connection connection = null;
		PreparedStatement pst = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		try {
			connection = dataSource.getConnection();
			Map<String, Object> valueMap = datas.get(0);
			Set<String> keySet = valueMap.keySet();
			Iterator<String> iterator = keySet.iterator();
			StringBuilder columnSql = new StringBuilder();
			StringBuilder unknownMarkSql = new StringBuilder();
			Object[] keys = new Object[valueMap.size()];
			int i = 0;
			while (iterator.hasNext()) {
				String key = iterator.next();
				keys[i] = key;
				columnSql.append(i == 0 ? "" : ",");
				columnSql.append(key);
				unknownMarkSql.append(i == 0 ? "" : ",");
				unknownMarkSql.append("?");
				i++;
			}
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO ");
			sql.append(tableName);
			sql.append(" (");
			sql.append(columnSql);
			sql.append(" )  VALUES (");
			sql.append(unknownMarkSql);
			sql.append(" )");

			connection.setAutoCommit(false);
			pst = connection.prepareStatement(sql.toString());
			System.out.println(sql.toString());
			int paramsSize = datas.size();
			affectRowCount = paramsSize;
			for (int j = 0; j < paramsSize; j++) {
				for (int k = 0; k < keys.length; k++) {
					pst.setObject(k + 1, datas.get(j).get(keys[k]));
				}
				pst.addBatch();
				if ((j != 0 && (j + 1) % batch == 0) || j == paramsSize - 1) {
					pst.executeBatch();
					pst.clearBatch();
				}
			}
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			close(null, pst, connection);
		}
		return affectRowCount;
	}

	/**
	 * 查询数据
	 *
	 * @param sql
	 * @param params
	 * @return
	 */
	public List<Map<String, Object>> query(String sql, Object... params) throws Exception {
		PreparedStatement pst = null;
		ResultSet rs = null;
		Connection conn = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();
		try {
			if (dataSource == null || dataSource.isClosed()) {
				init();
			}
			conn = dataSource.getConnection();
			pst = conn.prepareStatement(sql);
			if (params != null && params.length > 0) {
				int paramsIndex = 1;
				for (Object p : params) {
					pst.setObject(paramsIndex++, p);
				}
			}
			rs = pst.executeQuery();
			ResultSetMetaData rst = rs.getMetaData();
			int column = rst.getColumnCount();

			while (rs.next()) {
				Map<String, Object> m = new HashMap<String, Object>();
				for (int i = 1; i <= column; i++) {
					m.put(rst.getColumnLabel(i), rs.getObject(i));
				}
				rstList.add(m);
			}
		} finally {
			close(rs, pst, conn);
		}
		return rstList;
	}

	public List<Map<String, Object>> query(String sql, List<Object> params) {
		PreparedStatement pst = null;
		ResultSet rs = null;
		Connection conn = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		try {
			if (dataSource == null || dataSource.isClosed()) {
				init();
			}
			conn = dataSource.getConnection();
			pst = conn.prepareStatement(sql);
			int paramsIndex = 1;
			for (Object p : params) {
				pst.setObject(paramsIndex++, p);
			}
			rs = pst.executeQuery();
			ResultSetMetaData rst = rs.getMetaData();
			int column = rst.getColumnCount();
			List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();
			while (rs.next()) {
				Map<String, Object> m = new HashMap<String, Object>();
				for (int i = 1; i <= column; i++) {
					m.put(rst.getColumnLabel(i), rs.getObject(i));
				}
				rstList.add(m);
			}
			return rstList;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			close(rs, pst, conn);
		}
	}

	public long queryLong(String sql, Object... params) {
		PreparedStatement pst = null;
		ResultSet rs = null;
		Connection conn = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		try {
			if (dataSource == null || dataSource.isClosed()) {
				init();
			}
			conn = dataSource.getConnection();
			pst = conn.prepareStatement(sql);
			int paramsIndex = 1;
			for (Object p : params) {
				pst.setObject(paramsIndex++, p);
			}
			rs = pst.executeQuery();
			while (rs.next()) {
				return Long.valueOf(rs.getLong(1));
			}
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		} finally {
			close(rs, pst, conn);
		}
	}

	// 插入
	public boolean insert(String sql, Object... params) {
		PreparedStatement pst = null;
		Connection conn = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		try {
			if (dataSource == null || dataSource.isClosed()) {
				init();
			}
			conn = dataSource.getConnection();
			pst = conn.prepareStatement(sql);
			// 处理将数据插入占位符
			int paramsIndex = 1;
			for (Object p : params) {
				pst.setObject(paramsIndex++, p);
			}
			// 执行sql语句
			pst.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			close(null, pst, conn);
		}
	}

	public int update(String sql, Map<Integer, Object> param) {
		int ret = 0;
		PreparedStatement pst = null;
		Connection conn = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		try {
			if (dataSource == null || dataSource.isClosed()) {
				init();
			}
			conn = dataSource.getConnection();
			pst = conn.prepareStatement(sql);
			for (Integer key : param.keySet()) {
				pst.setObject(key, param.get(key));
			}
			ret = pst.executeUpdate();
		} catch (Exception e) {
			logger.error("update error!", e);
		} finally {
			close(null, pst, conn);
		}
		return ret;
	}

	// 修改
	public boolean update(String sql, Object... params) {
		PreparedStatement pst = null;
		Connection conn = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		try {
			if (dataSource == null || dataSource.isClosed()) {
				init();
			}
			conn = dataSource.getConnection();
			pst = conn.prepareStatement(sql);
			// 处理将数据插入占位符
			int paramsIndex = 1;
			for (Object p : params) {
				pst.setObject(paramsIndex++, p);
			}
			// 执行sql语句
			int ret = pst.executeUpdate();
			System.out.println("======" + ret);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			close(null, pst, conn);
		}
	}

	// 删除
	public boolean delete(String sql, Object... params)  throws Exception {
		PreparedStatement pst = null;
		Connection conn = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		boolean retValue = false;
		try {
			if (dataSource == null || dataSource.isClosed()) {
				init();
			}
			conn = dataSource.getConnection();
			pst = conn.prepareStatement(sql);
			int paramsIndex = 1;
			for (Object p : params) {
				pst.setObject(paramsIndex++, p);
			}
			int retCode = pst.executeUpdate();
			if (retCode > 0) {
				retValue = true;
			}
		} finally {
			close(null, pst, conn);
		}
		return retValue;
	}

	// 关闭资源
	public void close(ResultSet rs, PreparedStatement pst, Connection conn) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			rs = null;
		}
		if (pst != null) {
			try {
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			pst = null;
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			conn = null;
		}
	}

	//执行insert、update、delete后带返回值
	public int executeWithAffectedRows(String sql, Object... params) {
		int ret = 0;
		PreparedStatement pst = null;
		Connection conn = null;
		DruidDataSource dataSource = DATASOURCE_MAP.get(url);
		try {
			if (dataSource == null || dataSource.isClosed()) {
				init();
			}
			conn = dataSource.getConnection();
			pst = conn.prepareStatement(sql);
			// 处理将数据插入占位符
			int paramsIndex = 1;
			for (Object p : params) {
				pst.setObject(paramsIndex++, p);
			}
			// 执行sql语句
			ret = pst.executeUpdate();
		} catch (SQLException e) {
			logger.error("执行sql异常：", e);
		} finally {
			close(null, pst, conn);
		}
		return ret;
	}
}
