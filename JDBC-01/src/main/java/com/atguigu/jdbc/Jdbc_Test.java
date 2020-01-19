package com.atguigu.jdbc;

import org.junit.Test;

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
}
