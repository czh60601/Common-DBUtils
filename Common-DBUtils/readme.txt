首先确定使用的数据库
选择相应的数据库
	JDBCUtils.java更改name属性
	修改/Common-DBUtils/src/main/resources/c3p0-config.xml
	注：oracle默认序列为seq_tbname
新建服务并继承CommonDao<E>类
	public class UserServiceImpl extends CommonDaoImpl<User> implements CommonService<User> {}