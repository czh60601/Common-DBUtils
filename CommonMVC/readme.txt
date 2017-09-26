首先确定使用的数据库
选择相应的数据库DBUtils更名
		MySqlDBUtils
		OracleBUtils
新建服务接口并继承QueryInterface<E>接口
新建服务并继承CommonDaoImpl<E>类
	public class UserServiceImpl extends CommonDaoImpl<User> implements CommonService<User> {}
剩下的就是实现没有实现的方法