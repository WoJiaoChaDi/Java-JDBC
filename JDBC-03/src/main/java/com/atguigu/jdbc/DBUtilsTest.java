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

    /**
     * ���� QueryRunner �� query ����
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testResultSetHandler(){
        String sql = "SELECT id, name, email, birth " +
                "FROM customers";

        //1. ���� QueryRunner ����
        QueryRunner queryRunner = new QueryRunner();

        Connection conn = null;

        try {
            conn = JDBCTools.getConnection();
            /**
             * 2. ���� query ����:
             * ResultSetHandler ����������: query �����ķ���ֱֵ��ȡ����
             * ResultSetHandler �� hanlde(ResultSet rs) �����ʵ�ֵ�. ʵ����, ��
             * QueryRunner ��� query ������Ҳ�ǵ����� ResultSetHandler �� handle()
             * ������Ϊ����ֵ�ġ�
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
