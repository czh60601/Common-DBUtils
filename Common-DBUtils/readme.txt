首先确定使用的数据库
选择相应的数据库DBUtils更名
		MySqlDBUtils
		OracleBUtils
新建服务并继承CommonDao<E>类
	public class UserServiceImpl extends CommonDaoImpl<User> implements CommonService<User> {}