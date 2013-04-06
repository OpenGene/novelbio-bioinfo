package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.mapper.MapperSql;

public interface MapGoIDconvert extends MapperSql {



	/**
	 * 用GoIDquery,GoID两个中的任意组合去查找GoIDconvety表
	 * 主要是来看本列是否已经存在了，返回单个Go2Term
	 * @param Go2Term
	 * @return
	 */
	public Go2Term queryGoIDconvert(Go2Term queryGo2Term);
	
	/**
	 * 用GoIDquery,GoID两个中的任意组合去查找GoIDconvety表
	 * 主要是来看本列是否已经存在了，返回单个ArrayList--Go2Term
	 * @param Go2Term
	 * @return
	 */
	public ArrayList<Go2Term> queryLsGoIDconvert(Go2Term queryGo2Term);
	
	public void insertGoIDconvert(Go2Term Go2Term);
	
	/**
	 * 用 GOID去query
	 * @param Go2Term
	 */
	public void updateGoIDconvertWhereGOID(Go2Term Go2Term);
	/**
	 * 用Query GOID去query
	 * @param Go2Term
	 */
	public void updateGoIDconvertWhereQueryGOID(Go2Term Go2Term);
}
