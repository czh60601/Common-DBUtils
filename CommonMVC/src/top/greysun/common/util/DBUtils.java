package top.greysun.common.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class DBUtils extends OracleUtils {

	public static int getCount(String tbName){
		String sql = "SELECT COUNT(*) FROM "+tbName;
		int rowCount = 0;  
		try {  
			Connection conn = DBUtils.getConnection();  
			PreparedStatement psmt = conn.prepareStatement(sql);  
			ResultSet res = psmt.executeQuery();  
			while(res.next()){  
				rowCount = res.getInt(1);  
			}  
			res.close();
			psmt.close();
			conn.close();

		} catch (Exception e) {  
			// TODO: handle exception  
		}  

		return rowCount;
	}
}
