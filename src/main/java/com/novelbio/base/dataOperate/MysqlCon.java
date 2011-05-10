package com.novelbio.base.dataOperate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlCon {
	static Connection conn=null;
	static Statement stmt =null;
	public static Connection  getCon(int port,String DBName,String usrNam,String pwd) 
	{
		
		try {
			Class.forName("com.mysql.jdbc.Driver"); //加载mysq驱动
		} catch (ClassNotFoundException e) {
			System.out.println("驱动加载错误");
			e.printStackTrace();// 打印出错详细信息
		}
		try {
			String url = "jdbc:mysql://localhost:"+port+"/";
			url=url+DBName+"?user="+usrNam+"&password="+pwd;
			url=url+"&useUnicode=true&&characterEncoding=utf8&autoReconnect = true";//简单写法：url = "jdbc:myqsl://localhost/test(数据库名)? user=root(用户)&password=yqs2602555(密码)";
			conn = DriverManager.getConnection(url);
			return conn;
		} catch (SQLException e) {
			System.out.println("数据库链接错误");
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public static Statement getStmt(Connection conn) throws SQLException 
	{
		
		stmt = conn.createStatement();
		return stmt;
	}
	
	/**
	 * 关闭数据库连接
	 */
	public static void clsDB() {
		//关闭数据库
		try {
			/**
			if(rs != null) {
				rs.close();
				rs = null;
			}
			*/
			if(stmt != null) {
				stmt.close();
				stmt = null;
			}
			if(conn != null) {
				conn.close();
				conn = null;
			}
		} catch(Exception e) {
			System.out.println("数据库关闭错误");
			e.printStackTrace();
		}
	}
	
	
	
	
}
