package top.greysun.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

/**
 * 增删改查
 * @author http://www.greysun.top
 * @version 1.0
 * @param <E>
 */
public abstract class CommonDao<E> implements QueryInterface<E> {
	private Class <E> cls;
	private String tbName;
	private String primaryKey;

	public CommonDao(String tbName){
		this.tbName = tbName;
		iniDate();
	}

	/**
	 * 实体类名必须赫尔表名一直
	 */
	public CommonDao(){
		iniDate();
	}

	@SuppressWarnings("unchecked")
	private void iniDate(){
		cls = (Class<E>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		tbName = cls.getSimpleName().toUpperCase();

		//获取主键名
		Connection conn = DBUtils.getConnection();
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getPrimaryKeys(conn.getCatalog(), null,tbName);

			while(rs.next()){
				primaryKey = rs.getString("COLUMN_NAME");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<E> query() throws Exception {
		Connection conn = DBUtils.getConnection();
		QueryRunner qr = new QueryRunner();
		//拼接查询语句
		String sql = "select ";
		Object[] keys = toParamList();

		for(int i=0;i<keys.length;i++){
			if(i == keys.length-1){
				sql += keys[i]+" from "+tbName;
			}else{
				sql += keys[i]+",";
			}
		}

		List<E> list = qr.query(conn,sql,new BeanListHandler<E>(cls));
		conn.close();

		return list;
	}

	@Override
	public List<E> query(String where,Object...param) throws Exception{
		Connection conn = DBUtils.getConnection();
		QueryRunner qr = new QueryRunner();
		//构造查询语句
		String sql = "select ";
		Object[] keys = toParamList();

		for(int i=0;i<keys.length;i++){
			if(i == keys.length-1){
				sql += keys[i]+" from "+tbName+" where "+where;
			}else{
				sql += keys[i]+",";
			}
		}

		List<E> list = qr.query(conn,sql,new BeanListHandler<E>(cls),param);
		conn.close();

		return list;
	}

	@Override
	public int insert(E e) throws Exception {
		Connection conn = DBUtils.getConnection();
		QueryRunner qr = new QueryRunner();
		//拼接插入语句
		String sql1 = "insert into "+tbName+" (";
		String sql2 = "values(";

		HashMap<String, Object> param = toParamList(e);
		ArrayList<String> remove = new ArrayList<>();

		//去除空键值对
		for(String key:param.keySet()){
			if(key.equalsIgnoreCase(primaryKey)){
				//如果主键字段值不为空
				if(param.get(key)!=null){
					continue;
				}

				//如果主键字段值为空-添加到移除集
				remove.add(key);
			}else if(param.get(key)== null){
				//如果字段值为空-添加到移除集
				remove.add(key);
			}
		}

		for(String key : remove){

			//添加主键字段
			if(key.equalsIgnoreCase(primaryKey) && DBUtils.NAME.equalsIgnoreCase("oracle")){
				//获取序列值实现自增长
				sql1 += key+",";
				sql2 += "seq_"+tbName+"_id.nextval,";
			}
			param.remove(key);
		}

		int n=0;
		for(String key:param.keySet()){
			n++;

			if(n == param.size()){
				sql1 += key+")";
				sql2 += "?)";
			}else{
				sql1 += key+",";
				sql2 += "?,";
			}
		}

		int row;
		//插入
		row = qr.update(conn,sql1+sql2, param.values().toArray());

		conn.close();

		return row;
	}

	@Override
	public int update(E e) throws Exception {
		Connection conn = DBUtils.getConnection();
		QueryRunner qr = new QueryRunner();
		String sql = "update "+tbName+" set ";

		HashMap<String, Object> param = toParamList(e);
		Object primaryKeyValue = param.remove(primaryKey);

		int n=0;
		for(String key:param.keySet()){
			n++;
			if(n!=param.size()){
				sql += key+"=?,";
			}else{
				sql += key+"=? where "+primaryKey+"=?";
			}
		}

		int row;
		//修改
		row = qr.update(conn,sql, param.values().toArray(),primaryKeyValue);

		conn.close();

		return row;
	}

	@Override
	public int delete(int... id) throws Exception {
		int row = 0;
		Connection conn = DBUtils.getConnection();
		QueryRunner qr = new QueryRunner();
		String sql = "delete from "+tbName+" where "+primaryKey+"=?";
		for(int pid:id){
			row += qr.update(conn,sql,pid);
		}
		conn.close();
		
		return row;
	}

	@Override
	public int getCount() throws Exception{
		String sql = "SELECT COUNT(*) FROM "+tbName;
		int rowCount = 0;

		Connection conn = DBUtils.getConnection();  
		PreparedStatement psmt = conn.prepareStatement(sql);  
		ResultSet res = psmt.executeQuery();  
		while(res.next()){  
			rowCount = res.getInt(1);  
		}  
		res.close();
		psmt.close();
		conn.close();

		return rowCount;
	}

	private HashMap<String, Object> toParamList(E entity){
		HashMap<String, Object> paramList = new HashMap<>();

		try {
			//获取E的属性集
			Field[] fields = cls.getDeclaredFields();

			//遍历属性集
			for(Field field:fields){
				//允许访问
				field.setAccessible(true);
				//获取值
				paramList.put(field.getName().toUpperCase(), field.get(entity));
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return paramList;
	}

	private Object[] toParamList(){
		HashSet<String> paramList = new HashSet<String>();

		try {
			//获取E的属性集
			Field[] fields = cls.getDeclaredFields();

			//遍历属性集
			for(Field field:fields){
				//获取Key
				paramList.add(field.getName().toUpperCase());
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return paramList.toArray();
	}

	public String getTbName() {
		return tbName;
	}

	public Class<E> getCls(){
		return cls;
	}

}
