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


		try {
			//获取主键名
			Connection conn = JDBCUtils.getConnection();
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getPrimaryKeys(conn.getCatalog(), null,tbName);

			while(rs.next()){
				primaryKey = rs.getString("COLUMN_NAME").toUpperCase();
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<E> query() throws Exception {
		QueryRunner qr = JDBCUtils.getQueryRunner();
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

		List<E> list = qr.query(sql,new BeanListHandler<E>(cls));

		return list;
	}

	@Override
	public List<E> query(String where,Object...param) throws Exception{
		QueryRunner qr = JDBCUtils.getQueryRunner();
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

		List<E> list = qr.query(sql,new BeanListHandler<E>(cls),param);

		return list;
	}

	@Override
	public int insert(E e) throws Exception {
		QueryRunner qr = JDBCUtils.getQueryRunner();
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
			//如果主键字段为空,且数据库软件为oracle,添加自增长
			if(key.equalsIgnoreCase(primaryKey) && JDBCUtils.NAME.equalsIgnoreCase("oracle")){
				//获取序列值实现自增长
				sql1 += key+",";
				sql2 += "seq_"+tbName+".nextval,";
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
		row = qr.update(sql1+sql2, param.values().toArray());

		return row;
	}

	@Override
	public int update(E e) throws Exception {
		QueryRunner qr = JDBCUtils.getQueryRunner();
		String sql = "update "+tbName+" set ";

		HashMap<String, Object> param = toParamList(e);
		ArrayList<Object> keyValue = new ArrayList<Object>();
		Object primaryKeyValue = new Object();

		//提取所有值,并单独提取主键值
		for(String key:param.keySet()){
			if(key.equalsIgnoreCase(primaryKey)){
				primaryKeyValue = param.get(key);
			}else{
				keyValue.add(param.get(key));
			}
		}

		//移除主键
		param.remove(primaryKey.toUpperCase());
		//将主键值放在最后
		keyValue.add(primaryKeyValue);

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
		row = qr.update(sql,keyValue.toArray());

		return row;
	}

	@Override
	public int delete(int... id) throws Exception {
		int row = 0;
		QueryRunner qr = JDBCUtils.getQueryRunner();
		String sql = "delete from "+tbName+" where "+primaryKey+"=?";
		for(int pid:id){
			row += qr.update(sql,pid);
		}

		return row;
	}

	@Override
	public int getCount() throws Exception{
		String sql = "SELECT COUNT(*) FROM "+tbName;
		int rowCount = 0;

		Connection conn = JDBCUtils.getConnection();  
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
