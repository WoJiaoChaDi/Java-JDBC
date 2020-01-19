package com.atguigu.jdbc;

import org.junit.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
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
}
