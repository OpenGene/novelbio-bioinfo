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
			Class.forName("com.mysql.jdbc.Driver"); //����mysq����
		} catch (ClassNotFoundException e) {
			System.out.println("�������ش���");
			e.printStackTrace();// ��ӡ������ϸ��Ϣ
		}
		try {
			String url = "jdbc:mysql://localhost:"+port+"/";
			url=url+DBName+"?user="+usrNam+"&password="+pwd;
			url=url+"&useUnicode=true&&characterEncoding=utf8&autoReconnect = true";//��д����url = "jdbc:myqsl://localhost/test(���ݿ���)? user=root(�û�)&password=yqs2602555(����)";
			conn = DriverManager.getConnection(url);
			return conn;
		} catch (SQLException e) {
			System.out.println("���ݿ����Ӵ���");
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
	 * �ر����ݿ�����
	 */
	public static void clsDB() {
		//�ر����ݿ�
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
			System.out.println("���ݿ�رմ���");
			e.printStackTrace();
		}
	}
	
	
	
	
}
