package util;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * 打开链接的类
 * @author zong0jie
 *
 */
public class Util {
	
	private static SqlSessionFactory sqlSesFactFriceDB=null;
	private static SqlSessionFactory sqlSesFactKEGG=null;

	
	public static SqlSessionFactory getSqlSesFactFriceDB()
	{
		if (sqlSesFactFriceDB == null) {
			String friceResource="XML/FriceConf.xml";
			Reader friceReader=null;
			try
			{
				friceReader=Resources.getResourceAsReader(friceResource);
			}
			catch(IOException e)
			{
				//e.printStackTrace();
			}
			sqlSesFactFriceDB=new SqlSessionFactoryBuilder().build(friceReader);
		}	
		return sqlSesFactFriceDB;
	}
	public static SqlSessionFactory getSqlSesFactKEGG()
	{
		if (sqlSesFactKEGG == null) {
			String KEGGresource="XML/KEGGconf.xml";
			Reader keggReader=null;
			try
			{
				keggReader=Resources.getResourceAsReader(KEGGresource);
			}
			catch(IOException e)
			{
				//e.printStackTrace();
			}
			sqlSesFactKEGG=new SqlSessionFactoryBuilder().build(keggReader);
		}
		return sqlSesFactKEGG;
	}
	
	
}
