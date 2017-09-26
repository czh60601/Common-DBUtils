package top.greysun.common.dao.impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import top.greysun.common.dao.CommonDao;
import top.greysun.common.util.DBUtils;

/**
 * ����ʵ����
 * @author http://www.greysun.top
 * @version 1.0
 * @param <E>
 */
public abstract class CommonDaoImpl<E> implements CommonDao<E> {
	private Class <E> cls;
	private String tbName;
	private String primaryKey;

	public CommonDaoImpl(String tbName){
		this.tbName = tbName;
		iniDate();
	}

	/**
	 * ʵ����������ն�����һֱ
	 */
	public CommonDaoImpl(){
		iniDate();
	}

	@SuppressWarnings("unchecked")
	private void iniDate(){
		cls = (Class<E>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		tbName = cls.getSimpleName().toUpperCase();

		//��ȡ������
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

		List<E> list = qr.query(conn,sql,new BeanListHandler<E>(cls));
		conn.close();

		return list;
	}

	@Override
	public List<E> query(String where,Object...param) throws Exception{
		Connection conn = DBUtils.getConnection();
		QueryRunner qr = new QueryRunner();
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

		List<E> list = qr.query(conn,sql,new BeanListHandler<E>(cls),param);
		conn.close();

		return list;
	}

	@Override
	public int insert(E e) throws Exception {
		Connection conn = DBUtils.getConnection();
		QueryRunner qr = new QueryRunner();
		//ƴ�Ӳ�ѯ���
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

			//��������ֶ�
			if(key.equalsIgnoreCase(primaryKey) && DBUtils.isOrcl()){
				//��ȡ����ֵʵ��������
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
		//����
		row = qr.update(conn,sql1+sql2, param.values().toArray());

		conn.close();

		return row;
	}

	@Override
	public int update(E e) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(int... id) throws Exception {
		// TODO Auto-generated method stub
		return 0;
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
