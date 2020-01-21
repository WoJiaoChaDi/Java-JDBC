package com.atguigu.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 测试 QueryRunner 的 query 方法
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testResultSetHandler(){
        String sql = "SELECT id, name, email, birth " +
                "FROM customers";

        //1. 创建 QueryRunner 对象
        QueryRunner queryRunner = new QueryRunner();

        Connection conn = null;

        try {
            conn = JDBCTools.getConnection();
            /**
             * 2. 调用 query 方法:
             * ResultSetHandler 参数的作用: query 方法的返回值直接取决于
             * ResultSetHandler 的 hanlde(ResultSet rs) 是如何实现的. 实际上, 在
             * QueryRunner 类的 query 方法中也是调用了 ResultSetHandler 的 handle()
             * 方法作为返回值的。
             */
            Object object = queryRunner.query(conn, sql,
                new ResultSetHandler(){
                    @Override
                    public Object handle(ResultSet rs) throws SQLException {
                        List<Customer> customers = new ArrayList<>();

                        while(rs.next()){
                            int id = rs.getInt(1);
                            String name = rs.getString(2);
                            String email = rs.getString(3);
                            Date birth = rs.getDate(4);

                            Customer customer =
                                    new Customer(id, name, email, birth);
                            customers.add(customer);
                        }
                        return customers;
                    }
                }
            );
            System.out.println(object);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JDBCTools.releaseDB(null, null, conn);
        }
    }

}
