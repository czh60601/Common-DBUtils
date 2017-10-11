package test;
import java.util.List;

import org.junit.Test;

import top.greysun.common.utils.CommonDao;
import top.greysun.common.utils.DBUtils;


public class DBUtilsTest {
	
	@Test
	public void t1() throws Exception{
		new DBUtils("db_book","root","123456");
		BookDao dao = new BookDao();
//		dao.insert(new Book(1,"钢铁是怎样炼成的",1));
//		dao.insert(new Book(2,"新华字典",2));
//		dao.insert(new Book(3,"悲惨世界",1));
		List<Book> books = dao.query();
		System.out.println(books);
		dao.delete(1);
		books = dao.query();
		System.out.println(books);
		dao.update(new Book(3,"巴黎圣母院",1));
		books = dao.query();
		System.out.println(books);
	}
}

class BookDao extends CommonDao<Book>{
	
}