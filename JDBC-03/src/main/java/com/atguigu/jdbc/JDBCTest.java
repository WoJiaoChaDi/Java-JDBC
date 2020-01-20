package com.atguigu.jdbc;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

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

    /**
     * 使用 DBCP 数据库连接池
     * 1. 加入 jar 包(2 个jar 包). 依赖于 Commons Pool
     * 2. 创建数据库连接池
     * 3. 为数据源实例指定必须的属性
     * 4. 从数据源中获取数据库连接
     * @throws SQLException
     */
    @Test
    public void testDBCP() throws SQLException{
        final BasicDataSource dataSource = new BasicDataSource();

        //2. 为数据源实例指定必须的属性
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setUrl("jdbc:mysql://192.168.198.144/atguigu");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

        //3. 指定数据源的一些可选的属性.
        //1). 指定数据库连接池中初始化连接数的个数
        dataSource.setInitialSize(5);

        //2). 指定最大的连接数: 同一时刻可以同时向数据库申请的连接数
        dataSource.setMaxActive(5);

        //3). 指定小连接数: 在数据库连接池中保存的最少的空闲连接的数量
        dataSource.setMinIdle(2);

        //4).等待数据库连接池分配连接的最长时间. 单位为毫秒. 超出该时间将抛出异常.
        dataSource.setMaxWait(1000 * 5);

        //4. 从数据源中获取数据库连接
        Connection connection = dataSource.getConnection();
        System.out.println(connection.getClass());

        connection = dataSource.getConnection();
        System.out.println(connection.getClass());

        connection = dataSource.getConnection();
        System.out.println(connection.getClass());

        connection = dataSource.getConnection();
        System.out.println(connection.getClass());

        Connection connection2 = dataSource.getConnection();
        System.out.println(">" + connection2.getClass());

        new Thread(){
            public void run() {
                Connection conn;
                try {
                    conn = dataSource.getConnection();
                    System.out.println(conn.getClass());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            };
        }.start();

        try {
            Thread.sleep(5500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        connection2.close();
    }

	/**
	 * 1. 加载 dbcp 的 properties 配置文件: 配置文件中的键需要来自 BasicDataSource
	 * 的属性.
	 * 2. 调用 BasicDataSourceFactory 的 createDataSource 方法创建 DataSource
	 * 实例
	 * 3. 从 DataSource 实例中获取数据库连接.
	 */
	@Test
	public void testDBCPWithDataSourceFactory() throws Exception{

		Properties properties = new Properties();
		InputStream inStream = JDBCTest.class.getClassLoader()
				.getResourceAsStream("dbcp.properties");
		properties.load(inStream);

		DataSource dataSource =
				BasicDataSourceFactory.createDataSource(properties);

		System.out.println(dataSource.getConnection());

		//测试
//		BasicDataSource basicDataSource =
//				(BasicDataSource) dataSource;
//
//		System.out.println(basicDataSource.getMaxWait());
	}
}
