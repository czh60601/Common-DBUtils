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
//		dao.insert(new Book(1,"�������������ɵ�",1));
//		dao.insert(new Book(2,"�»��ֵ�",2));
//		dao.insert(new Book(3,"��������",1));
		List<Book> books = dao.query();
		System.out.println(books);
		dao.delete(1);
		books = dao.query();
		System.out.println(books);
		dao.update(new Book(3,"����ʥĸԺ",1));
		books = dao.query();
		System.out.println(books);
	}
}

class BookDao extends CommonDao<Book>{
	
}