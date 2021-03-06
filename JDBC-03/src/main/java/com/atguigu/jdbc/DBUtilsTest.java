package com.atguigu.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * 1. ResultSetHandler 的作用: QueryRunner 的 query 方法的返回值最终取决于
     * query 方法的 ResultHandler 参数的 hanlde 方法的返回值.
     *
     * 2. BeanListHandler: 把结果集转为一个 Bean 的 List, 并返回. Bean 的类型在
     * 创建 BeanListHanlder 对象时以 Class 对象的方式传入. 可以适应列的别名来映射
     * JavaBean 的属性名:
     * String sql = "SELECT id, name customerName, email, birth " +
     *			"FROM customers WHERE id = ?";
     *
     * BeanListHandler(Class<T> type)
     *
     * 3. BeanHandler: 把结果集转为一个 Bean, 并返回. Bean 的类型在创建 BeanHandler
     * 对象时以 Class 对象的方式传入
     * BeanHandler(Class<T> type)
     *
     * 4. MapHandler: 把结果集转为一个 Map 对象, 并返回. 若结果集中有多条记录, 仅返回
     * 第一条记录对应的 Map 对象. Map 的键: 列名(而非列的别名), 值: 列的值
     *
     * 5. MapListHandler: 把结果集转为一个 Map 对象的集合, 并返回.
     * Map 的键: 列名(而非列的别名), 值: 列的值
     *
     * 6. ScalarHandler: 可以返回指定列的一个值或返回一个统计函数的值.
     */

    @Test
    public void testScalarHandler(){
        Connection connection = null;
        QueryRunner queryRunner = new QueryRunner();

        String sql = "SELECT name FROM customers " +
                "WHERE id = ?";

        try {
            connection = JDBCTools.getConnection();
            Object count = queryRunner.query(connection, sql,
                    new ScalarHandler(), 6);

            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JDBCTools.releaseDB(null, null, connection);
        }
    }

    @Test
    public void testMapListHandler(){
        Connection connection = null;
        QueryRunner queryRunner = new QueryRunner();

        String sql = "SELECT id, name, email, birth " +
                "FROM customers";

        try {
            connection = JDBCTools.getConnection();
            List<Map<String, Object>> mapList = queryRunner.query(connection,
                    sql, new MapListHandler());

            System.out.println(mapList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JDBCTools.releaseDB(null, null, connection);
        }
    }

    @Test
    public void testMapHandler(){
        Connection connection = null;
        QueryRunner queryRunner = new QueryRunner();

        String sql = "SELECT id, name customerName, email, birth " +
                "FROM customers WHERE id = ?";

        try {
            connection = JDBCTools.getConnection();
            Map<String, Object> map = queryRunner.query(connection,
                    sql, new MapHandler(), 4);

            System.out.println(map);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JDBCTools.releaseDB(null, null, connection);
        }
    }

    /**
     * 测试 ResultSetHandler 的 BeanListHandler 实现类
     * BeanListHandler: 把结果集转为一个 Bean 的 List. 该 Bean
     * 的类型在创建 BeanListHandler 对象时传入:
     *
     * new BeanListHandler<>(Customer.class)
     *
     */
    @Test
    public void testBeanListHandler(){
        String sql = "SELECT id, name customerName, email, birth " +
                "FROM customers";

        //1. 创建 QueryRunner 对象
        QueryRunner queryRunner = new QueryRunner();

        Connection conn = null;

        try {
            conn = JDBCTools.getConnection();

            Object object = queryRunner.query(conn, sql,
                    new BeanListHandler<>(Customer.class));

            System.out.println(object);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JDBCTools.releaseDB(null, null, conn);
        }
    }

    /**
     * 如何使用JDBC 调用存储在数据库中的函数或存储过程
     */
    @Test
    public void testCallableStatment(){
        Connection connection = null;
        CallableStatement callableStatement = null;

        try {
            connection = JDBCTools.getConnection();

            // 1. 通过Connection 对象的 prepareCall() 方法创建一个 CallableStatement 对象的实例。
            // 在使用Connection 对象的 preparedCall() 方法时，需要传入一个String类型的字符串，该字符串用于指明如何调用存储过程
            String sql = "{?= call sum_salary(?, ?)}";//函数
            //String sql = "{call sum_salary(?, ?)}";//存储过程
            callableStatement = connection.prepareCall("");

            // 2. 通过CallableStatement 对象的 registerOutParameter() 方法注册OUT 对象
            callableStatement.registerOutParameter(1, Types.NUMERIC);
            callableStatement.registerOutParameter(3, Types.NUMERIC);

            // 3. 通过 CallableStatement 对象的 setXxx() 方法设定 IN 或 IN OUT 参数 ，若想将参数默认值设定为null，可以使用setNull() 方法
            callableStatement.setInt(2, 80);

            // 4. 通过 CallableStatement 对象的 execute() 方法执行存储过程
            callableStatement.execute();

            // 5. 如果所调用的是待返回参数的次数过程，还需要通过 callableStatement 对象的 getXxx() 方法获取其返回值
            double sumSalary = callableStatement.getDouble(1);
            long empCount = callableStatement.getLong(3);

            System.out.println(sumSalary);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }
}
