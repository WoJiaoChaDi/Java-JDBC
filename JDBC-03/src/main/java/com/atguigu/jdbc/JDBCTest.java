package com.atguigu.jdbc;

import org.junit.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class JDBCTest {

	/**
	 * 向  Oracle 的 customers 数据表中插入 10 万条记录
	 * 测试如何插入, 用时最短.
	 * 1. 使用 Statement.
	 * 		用时：39567   每次执行都要执行不同的sql（对数据库来说）
	 */
	@Test
	public void testBatchWithStatement(){
		Connection connection = null;
		Statement statement = null;
		String sql = null;

		try {
			connection = JDBCTools.getConnection();
			JDBCTools.beginTx(connection);

			statement = connection.createStatement();

			long begin = System.currentTimeMillis();
			for(int i = 0; i < 100000; i++){
				sql = "INSERT INTO customers VALUES(" + (i + 1)
						+ ", 'name_" + i + "', '29-6月 -13')";
				statement.addBatch(sql);
			}
			long end = System.currentTimeMillis();

			System.out.println("Time: " + (end - begin)); //39567

			JDBCTools.commit(connection);
		} catch (Exception e) {
			e.printStackTrace();
			JDBCTools.rollback(connection);
		} finally{
			JDBCTools.releaseDB(null, statement, connection);
		}
	}

	/**
	 * 向  Oracle 的 customers 数据表中插入 10 万条记录
	 * 测试如何插入, 用时最短.
	 * 2. 使用 preparedStatement.executeUpdate.
	 * 		用时：9819   因为sql是预编译的，只是值不一样的，sql可以重用
	 */
	@Test
	public void testBatchWithPreparedStatement(){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		String sql = null;

		try {
			connection = JDBCTools.getConnection();
			JDBCTools.beginTx(connection);
			sql = "INSERT INTO customers VALUES(?,?,?)";
			preparedStatement = connection.prepareStatement(sql);
			Date date = new Date(new java.util.Date().getTime());

			long begin = System.currentTimeMillis();
			for(int i = 0; i < 100000; i++){
				preparedStatement.setInt(1, i + 1);
				preparedStatement.setString(2, "name_" + i);
				preparedStatement.setDate(3, date);

				preparedStatement.executeUpdate();
			}
			long end = System.currentTimeMillis();

			System.out.println("Time: " + (end - begin)); //9819

			JDBCTools.commit(connection);
		} catch (Exception e) {
			e.printStackTrace();
			JDBCTools.rollback(connection);
		} finally{
			JDBCTools.releaseDB(null, preparedStatement, connection);
		}
	}

	/**
	 * 向  Oracle 的 customers 数据表中插入 10 万条记录
	 * 测试如何插入, 用时最短.
	 * 2. 使用 preparedStatement.addBatch.
	 * 		用时：569   批处理，攒到一定程度一起执行
	 */
	@Test
	public void testBatch(){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		String sql = null;

		try {
			connection = JDBCTools.getConnection();
			JDBCTools.beginTx(connection);
			sql = "INSERT INTO customers VALUES(?,?,?)";
			preparedStatement = connection.prepareStatement(sql);
			Date date = new Date(new java.util.Date().getTime());

			long begin = System.currentTimeMillis();
			for(int i = 0; i < 100000; i++){
				preparedStatement.setInt(1, i + 1);
				preparedStatement.setString(2, "name_" + i);
				preparedStatement.setDate(3, date);

				//"积攒" SQL
				preparedStatement.addBatch();

				//当 "积攒" 到一定程度, 就统一的执行一次. 并且清空先前 "积攒" 的 SQL
				if((i + 1) % 300 == 0){
					preparedStatement.executeBatch();
					preparedStatement.clearBatch();
				}
			}

			//若总条数不是批量数值的整数倍, 则还需要再额外的执行一次.
			if(100000 % 300 != 0){
				preparedStatement.executeBatch();
				preparedStatement.clearBatch();
			}

			long end = System.currentTimeMillis();

			System.out.println("Time: " + (end - begin)); //569

			JDBCTools.commit(connection);
		} catch (Exception e) {
			e.printStackTrace();
			JDBCTools.rollback(connection);
		} finally{
			JDBCTools.releaseDB(null, preparedStatement, connection);
		}
	}

}
