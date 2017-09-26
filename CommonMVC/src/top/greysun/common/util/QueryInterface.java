package top.greysun.common.util;

import java.util.List;

/**
 * ��ɾ�Ĳ����
 * @author http://www.greysun.top
 * @version 1.0
 * @param <E>
 */
public interface QueryInterface<E> {

	/**
	 * ��ѯ
	 * @return List<E>
	 * @throws Exception
	 */
	public List<E> query() throws Exception;

	/**
	 * ��ѯ
	 * @param where ֻ�б��ʽ���֣�������'?'����
	 * @param param
	 * @return List<E>
	 * @throws Exception
	 */
	public List<E> query(String where,Object...param) throws Exception;

	/**
	 * ���
	 * @param e
	 * @return int
	 * @throws Exception
	 */
	public int insert(E e) throws Exception;

	/**
	 * �޸�
	 * @param e
	 * @return int
	 * @throws Exception
	 */
	public int update(E e) throws Exception;

	/**
	 * ɾ��
	 * @param id
	 * @return int
	 * @throws Exception
	 */
	public int delete(int...id) throws Exception;
}
