package top.greysun.common.util;

import java.util.List;

/**
 * 增删改查操作
 * @author http://www.greysun.top
 * @version 1.0
 * @param <E>
 */
public interface QueryInterface<E> {

	/**
	 * 查询
	 * @return List<E>
	 * @throws Exception
	 */
	public List<E> query() throws Exception;

	/**
	 * 查询
	 * @param where 只有表达式部分，参数用'?'代替
	 * @param param
	 * @return List<E>
	 * @throws Exception
	 */
	public List<E> query(String where,Object...param) throws Exception;

	/**
	 * 添加
	 * @param e
	 * @return int
	 * @throws Exception
	 */
	public int insert(E e) throws Exception;

	/**
	 * 修改
	 * @param e
	 * @return int
	 * @throws Exception
	 */
	public int update(E e) throws Exception;

	/**
	 * 删除
	 * @param id
	 * @return int
	 * @throws Exception
	 */
	public int delete(int...id) throws Exception;
}
