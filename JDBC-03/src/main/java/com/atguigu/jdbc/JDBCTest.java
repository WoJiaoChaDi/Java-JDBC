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
	 * ��  Oracle �� customers ���ݱ��в��� 10 ������¼
	 * ������β���, ��ʱ���.
	 * 1. ʹ�� Statement.
	 * 		��ʱ��39567   ÿ��ִ�ж�Ҫִ�в�ͬ��sql�������ݿ���˵��
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
						+ ", 'name_" + i + "', '29-6�� -13')";
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
	 * ��  Oracle �� customers ���ݱ��в��� 10 ������¼
	 * ������β���, ��ʱ���.
	 * 2. ʹ�� preparedStatement.executeUpdate.
	 * 		��ʱ��9819   ��Ϊsql��Ԥ����ģ�ֻ��ֵ��һ���ģ�sql��������
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
	 * ��  Oracle �� customers ���ݱ��в��� 10 ������¼
	 * ������β���, ��ʱ���.
	 * 2. ʹ�� preparedStatement.addBatch.
	 * 		��ʱ��569   �������ܵ�һ���̶�һ��ִ��
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

				//"����" SQL
				preparedStatement.addBatch();

				//�� "����" ��һ���̶�, ��ͳһ��ִ��һ��. ���������ǰ "����" �� SQL
				if((i + 1) % 300 == 0){
					preparedStatement.executeBatch();
					preparedStatement.clearBatch();
				}
			}

			//������������������ֵ��������, ����Ҫ�ٶ����ִ��һ��.
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
     * ʹ�� DBCP ���ݿ����ӳ�
     * 1. ���� jar ��(2 ��jar ��). ������ Commons Pool
     * 2. �������ݿ����ӳ�
     * 3. Ϊ����Դʵ��ָ�����������
     * 4. ������Դ�л�ȡ���ݿ�����
     * @throws SQLException
     */
    @Test
    public void testDBCP() throws SQLException{
        final BasicDataSource dataSource = new BasicDataSource();

        //2. Ϊ����Դʵ��ָ�����������
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setUrl("jdbc:mysql://192.168.198.144/atguigu");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

        //3. ָ������Դ��һЩ��ѡ������.
        //1). ָ�����ݿ����ӳ��г�ʼ���������ĸ���
        dataSource.setInitialSize(5);

        //2). ָ������������: ͬһʱ�̿���ͬʱ�����ݿ������������
        dataSource.setMaxActive(5);

        //3). ָ��С������: �����ݿ����ӳ��б�������ٵĿ������ӵ�����
        dataSource.setMinIdle(2);

        //4).�ȴ����ݿ����ӳط������ӵ��ʱ��. ��λΪ����. ������ʱ�佫�׳��쳣.
        dataSource.setMaxWait(1000 * 5);

        //4. ������Դ�л�ȡ���ݿ�����
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
	 * 1. ���� dbcp �� properties �����ļ�: �����ļ��еļ���Ҫ���� BasicDataSource
	 * ������.
	 * 2. ���� BasicDataSourceFactory �� createDataSource �������� DataSource
	 * ʵ��
	 * 3. �� DataSource ʵ���л�ȡ���ݿ�����.
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

		//����
//		BasicDataSource basicDataSource =
//				(BasicDataSource) dataSource;
//
//		System.out.println(basicDataSource.getMaxWait());
	}
}
