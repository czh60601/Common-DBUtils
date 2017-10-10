package top.greysun.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleUtils {	
	final private static String driver = "oracle.jdbc.OracleDriver";
	final private static String url = "jdbc:oracle:thin:@localhost:1521:orcl";
	final private static String user = "SCOTT";
	final private static String password = "tiger";
	final public static String NAME = "oracle";

	//��̬����飺�����������ʱִ��һ��
	static{
		//ע��
		try {
			Class.forName(driver);
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
