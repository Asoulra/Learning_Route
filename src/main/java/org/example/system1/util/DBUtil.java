package org.example.system1.util;

import com.alibaba.druid.pool.DruidDataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * 数据库工具类 - 基于Druid连接池
 */
public class DBUtil {
    private static DruidDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (in != null) {
                props.load(in);
            }
            dataSource = new DruidDataSource();
            dataSource.setUrl(props.getProperty("db.url", "jdbc:mysql://localhost:3306/system1?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"));
            dataSource.setUsername(props.getProperty("db.username", "root"));
            dataSource.setPassword(props.getProperty("db.password", "123456"));
            dataSource.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
            dataSource.setInitialSize(5);
            dataSource.setMaxActive(20);
            dataSource.setMinIdle(5);
            dataSource.setMaxWait(60000);
            dataSource.setValidationQuery("SELECT 1");
            dataSource.setTestWhileIdle(true);
            dataSource.setTestOnBorrow(false);
            dataSource.setTestOnReturn(false);
        } catch (Exception e) {
            throw new RuntimeException("数据库连接池初始化失败", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        close(conn, (Statement) ps, rs);
    }

    public static void close(Connection conn, PreparedStatement ps) {
        close(conn, (Statement) ps, null);
    }
}
