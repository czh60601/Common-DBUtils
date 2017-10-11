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
 * ��ɾ�Ĳ�
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
	 * ʵ����������ն�����һֱ
	 */
	public CommonDao(){
		iniDate();
	}

	@SuppressWarnings("unchecked")
	private void iniDate(){
		cls = (Class<E>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		tbName = cls.getSimpleName().toUpperCase();


		try {
			//��ȡ������
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
		//ƴ�Ӳ�ѯ���
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
		//�����ѯ���
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
		//ƴ�Ӳ������
		String sql1 = "insert into "+tbName+" (";
		String sql2 = "values(";

		HashMap<String, Object> param = toParamList(e);
		ArrayList<String> remove = new ArrayList<>();

		//ȥ���ռ�ֵ��
		for(String key:param.keySet()){
			if(key.equalsIgnoreCase(primaryKey)){
				//��������ֶ�ֵ��Ϊ��
				if(param.get(key)!=null){
					continue;
				}

				//��������ֶ�ֵΪ��-��ӵ��Ƴ���
				remove.add(key);
			}else if(param.get(key)== null){
				//����ֶ�ֵΪ��-��ӵ��Ƴ���
				remove.add(key);
			}
		}

		for(String key : remove){
			//��������ֶ�Ϊ��,�����ݿ����Ϊoracle,���������
			if(key.equalsIgnoreCase(primaryKey) && JDBCUtils.NAME.equalsIgnoreCase("oracle")){
				//��ȡ����ֵʵ��������
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
		//����
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

		//��ȡ����ֵ,��������ȡ����ֵ
		for(String key:param.keySet()){
			if(key.equalsIgnoreCase(primaryKey)){
				primaryKeyValue = param.get(key);
			}else{
				keyValue.add(param.get(key));
			}
		}

		//�Ƴ�����
		param.remove(primaryKey.toUpperCase());
		//������ֵ�������
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
		//�޸�
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
			//��ȡE�����Լ�
			Field[] fields = cls.getDeclaredFields();

			//�������Լ�
			for(Field field:fields){
				//�������
				field.setAccessible(true);
				//��ȡֵ
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
			//��ȡE�����Լ�
			Field[] fields = cls.getDeclaredFields();

			//�������Լ�
			for(Field field:fields){
				//��ȡKey
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
