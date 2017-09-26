package top.greysun.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlUtils{
	final private static String url = "jdbc:mysql://localhost:3306/pets";
	final private static String diver = "com.mysql.jdbc.Driver";
	final private static String user = "root";
	final private static String password = "123456";

	//��̬����飺�����������ʱִ��һ��
	static{
		//ע��
		try {
			Class.forName(diver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		Connection conn = null;
		
		//����
		try {
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return conn;
	}
}
