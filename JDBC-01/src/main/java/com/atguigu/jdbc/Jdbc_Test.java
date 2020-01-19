package com.atguigu.jdbc;

import org.junit.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Jdbc_Test {

    /**
     * Driver 是一个接口: 数据库厂商必须提供实现的接口. 能从其中获取数据库连接.
     * 可以通过 Driver 的实现类对象获取数据库连接.
     *
     * 1. 加入 mysql 驱动
     * 1). 解压 mysql-connector-java-5.1.7.zip
     * 2). 在当前项目下新建 lib 目录
     * 3). 把 mysql-connector-java-5.1.7-bin.jar 复制到 lib 目录下
     * 4). 右键 build-path , add to buildpath 加入到类路径下.s
     * @throws SQLException
     */
    @Test
    public void testDriver() throws SQLException {
        //1. 创建一个 Driver 实现类的对象
        //(这里有个问题，跟mysql耦合太严重，所以一般用Class.forName("com.mysql.jdbc.Driver"))
        Driver driver = new com.mysql.jdbc.Driver();

        //2. 准备连接数据库的基本信息: url, user, password
        String url = "jdbc:mysql://192.168.198.147:3306/test";
        Properties info = new Properties();
        info.put("user", "root");
        info.put("password", "root");

        //3. 调用 Driver 接口的　connect(url, info) 获取数据库连接
        Connection connection = driver.connect(url, info);
        System.out.println(connection);
    }

    /**
     * 编写一个通用的方法, 在不修改源程序的情况下, 可以获取任何数据库的连接
     * 解决方案: 把数据库驱动 Driver 实现类的全类名、url、user、password 放入一个
     * 配置文件中, 通过修改配置文件的方式实现和具体的数据库解耦.
     * @throws Exception
     */
    public Connection getConnection() throws Exception{
        String driverClass = null;
        String jdbcUrl = null;
        String user = null;
        String password = null;

        //读取类路径下的 jdbc.properties 文件
        InputStream in =
                getClass().getClassLoader().getResourceAsStream("jdbc.properties");
        Properties properties = new Properties();
        properties.load(in);
        driverClass = properties.getProperty("driver");
        jdbcUrl = properties.getProperty("jdbcUrl");
        user = properties.getProperty("user");
        password = properties.getProperty("password");

        //通过反射常见 Driver 对象.
        Driver driver =
                (Driver) Class.forName(driverClass).newInstance();

        Properties info = new Properties();
        info.put("user", user);
        info.put("password", password);

        //通过 Driver 的 connect 方法获取数据库连接.
        Connection connection = driver.connect(jdbcUrl, info);

        return connection;
    }

    @Test
    public void testGetConnection() throws Exception{
        System.out.println(getConnection());
    }

    /**
     * DriverManager 是驱动的管理类.
     * 1). 可以通过重载的 getConnection() 方法获取数据库连接. 较为方便
     * 2). 可以同时管理多个驱动程序: 若注册了多个数据库连接, 则调用 getConnection()
     * 方法时传入的参数不同, 即返回不同的数据库连接。
     * @throws Exception
     */
    @Test
    public void testDriverManager() throws Exception{
        //1. 准备连接数据库的 4 个字符串.
        //驱动的全类名.
        String driverClass = "com.mysql.jdbc.Driver";
        String driverClass2 = "oracle.jdbc.driver.OracleDriver";

        String jdbcUrl = "jdbc:mysql://192.168.198.151:3306/test";
        String user = "root";
        String password = "root";

        String jdbcUrl2 = "jdbc:oracle:thin:@localhost:1521:orcl";
        String user2 = "scott";
        String password2 = "scott";

        //2. 加载数据库驱动程序
        //实际上应该这么写
        //DriverManager.registerDriver((Driver) Class.forName(driverClass).newInstance());
        //（因为对应的 Driver 实现类中有注册驱动的静态代码块.)
        Class.forName(driverClass);
        //Class.forName(driverClass2);

        //3. 通过 DriverManager 的 getConnection() 方法获取数据库连接. 用DriverManager可以管理多个驱动程序
        Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
        //Connection connection2 = DriverManager.getConnection(jdbcUrl2, user2, password2);
        System.out.println(connection);
        //System.out.println(connection2);
    }

    /**
     * 封装DriverManager获取数据库连接
     * @return
     * @throws Exception
     */
    public Connection getConnection2() throws Exception{
        //1. 准备连接数据库的 4 个字符串.
        //1). 创建 Properties 对象
        Properties properties = new Properties();

        //2). 获取 jdbc.properties 对应的输入流
        InputStream in =
                this.getClass().getClassLoader().getResourceAsStream("jdbc.properties");

        //3). 加载 2） 对应的输入流
        properties.load(in);

        //4). 具体决定 user, password 等4 个字符串.
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");
        String jdbcUrl = properties.getProperty("jdbcUrl");
        String driver = properties.getProperty("driver");

        //2. 加载数据库驱动程序(对应的 Driver 实现类中有注册驱动的静态代码块.)
        Class.forName(driver);

        //3. 通过 DriverManager 的 getConnection() 方法获取数据库连接.
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    /**
     * 通过 JDBC 向指定的数据表中插入一条记录.
     *
     * 1. Statement: 用于执行 SQL 语句的对象
     * 1). 通过 Connection 的 createStatement() 方法来获取
     * 2). 通过 executeUpdate(sql) 可以执行 SQL 语句.
     * 3). 传入的 SQL 可以是 INSRET, UPDATE 或 DELETE. 但不能是 SELECT
     *
     * 2. Connection、Statement 都是应用程序和数据库服务器的连接资源. 使用后一定要关闭.
     * 需要在 finally 中关闭 Connection 和 Statement 对象.
     *
     * 3. 关闭的顺序是: 先关闭后获取的. 即先关闭 Statement 后关闭 Connection
     */
    @Test
    public void testStatement() throws Exception{
        Connection conn = null;
        Statement statement = null;

        try {
            //1. 获取数据库连接
            conn = getConnection2();

            //3. 准备插入的 SQL 语句
            String sql = null;

			//sql = "INSERT INTO customers (NAME, EMAIL, BIRTH) " +
			//		"VALUES('XYZ', 'xyz@atguigu.com', '1990-12-12')";
            //sql = "DELETE FROM customers WHERE id = 2";
            sql = "UPDATE customers SET name = 'TOM' " +
                    "WHERE id = 3";
            System.out.println(sql);

            //4. 执行插入.
            //1). 获取操作 SQL 语句的 Statement 对象:
            //调用 Connection 的 createStatement() 方法来获取
            statement = conn.createStatement();

            //2). 调用 Statement 对象的 executeUpdate(sql) 执行 SQL 语句进行插入
            statement.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                //5. 关闭 Statement 对象.
                if(statement != null)
                    statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                //2. 关闭连接
                if(conn != null)
                    conn.close();
            }
        }
    }

    /**
     * 通用的更新的方法: 包括 INSERT、UPDATE、DELETE
     * 版本 1.
     */
    public void update(String sql){
        Connection conn = null;
        Statement statement = null;

        try {
            conn = JDBCTools.getConnection();
            statement = conn.createStatement();
            statement.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JDBCTools.release(statement, conn);
        }
    }

    @Test
    public void testUpdate(){
        String sql = "delete from customers where id = 3";
        update(sql);
    }


}
