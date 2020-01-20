package com.atguigu.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.junit.Test;

import java.sql.Connection;

/**
 * 测试 DBUtils 工具类
 *
 */
public class DBUtilsTest {

	/**
	 * 测试 QueryRunner 类的 update 方法
	 * 该方法可用于 INSERT, UPDATE 和 DELETE
	 */
	@Test
	public void testQueryRunnerUpdate() {
		//1. 创建 QueryRunner 的实现类
		QueryRunner queryRunner = new QueryRunner();
		
		String sql = "DELETE FROM customers " +
				"WHERE id IN (?,?)";
		
		Connection connection = null;
		
		try {
			connection = JDBCTools.getConnection();
			//2. 使用其 update 方法
			queryRunner.update(connection, 
					sql, 12, 13);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			JDBCTools.releaseDB(null, null, connection);
		}
		
	}

}
