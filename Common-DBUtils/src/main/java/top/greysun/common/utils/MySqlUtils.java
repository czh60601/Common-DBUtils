package top.greysun.common.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlUtils{
	final private static String url = "jdbc:mysql://localhost:3306/";
	final private static String diver = "com.mysql.jdbc.Driver";

	/**
	 * 数据库软件名
	 */
	final public static String NAME = "mysql";

	protected static String dbname = "pet";
	protected static String user = "root";
	protected static String password = "123456";

	//静态代码块：仅加载这个类时执行一次
	static{
		//注册
		try {
			Class.forName(diver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		Connection conn = null;

		//连接
		try {
			conn = DriverManager.getConnection(url+dbname, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return conn;
	}
}
