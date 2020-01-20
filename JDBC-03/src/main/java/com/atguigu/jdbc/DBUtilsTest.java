package com.atguigu.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.junit.Test;

import java.sql.Connection;

/**
 * ���� DBUtils ������
 *
 */
public class DBUtilsTest {

	/**
	 * ���� QueryRunner ��� update ����
	 * �÷��������� INSERT, UPDATE �� DELETE
	 */
	@Test
	public void testQueryRunnerUpdate() {
		//1. ���� QueryRunner ��ʵ����
		QueryRunner queryRunner = new QueryRunner();
		
		String sql = "DELETE FROM customers " +
				"WHERE id IN (?,?)";
		
		Connection connection = null;
		
		try {
			connection = JDBCTools.getConnection();
			//2. ʹ���� update ����
			queryRunner.update(connection, 
					sql, 12, 13);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			JDBCTools.releaseDB(null, null, connection);
		}
		
	}

}
