package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.service.servgeneanno.ServGo2Term;

public class Go2Term {

    private String GoIDquery;
	private String GoID;
	private String GoTerm;
	private String GoFunction;
	
	public String getGoIDQuery() {
		return GoIDquery;
	}
	public void setGoIDQuery(String GoIDQuery) {
		this.GoIDquery = GoIDQuery;
	}

	public String getGoID() {
		return GoID;
	}
	public void setGoID(String GoID) {
		this.GoID = GoID;
	}
	
	public String getGoTerm() {
		return GoTerm;
	}
	public void setGoTerm(String GoTerm) {
		this.GoTerm = GoTerm;
	}  
	
	public String getGoFunction() {
		return GoFunction;
	}
	public void setGoFunction(String GoFunction) {
		this.GoFunction = GoFunction;
	}
	/**
	 * 存储Go2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, Go2Term> hashGo2Term = new HashMap<String, Go2Term>();
	
	/**
	 * 将所有GO信息提取出来放入hash表中，方便查找
	 * 存储Go2Term的信息
	 * key:GoID
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 * 如果已经查过了一次，自动返回
	 */
	public static HashMap<String, Go2Term> getHashGo2Term() {
		ServGo2Term servGo2Term = new ServGo2Term();
		if (hashGo2Term != null && hashGo2Term.size() > 0) {
			return hashGo2Term;
		}
		Go2Term go2Term = new Go2Term();
		ArrayList<Go2Term> lsGo2Terms = servGo2Term.queryLsGo2Term(go2Term);
		for (Go2Term go2Term2 : lsGo2Terms) 
		{
			hashGo2Term.put(go2Term2.getGoIDQuery(), go2Term2);
		}
		return hashGo2Term;
	}	
	
}
