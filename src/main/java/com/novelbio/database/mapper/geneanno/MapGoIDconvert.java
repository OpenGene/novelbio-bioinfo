package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.mapper.MapperSql;

public interface MapGoIDconvert extends MapperSql{



	/**
	 * ��GoIDquery,GoID�����е��������ȥ����GoIDconvety��
	 * ��Ҫ�����������Ƿ��Ѿ������ˣ����ص���Go2Term
	 * @param Go2Term
	 * @return
	 */
	public Go2Term queryGoIDconvert(Go2Term queryGo2Term);
	
	/**
	 * ��GoIDquery,GoID�����е��������ȥ����GoIDconvety��
	 * ��Ҫ�����������Ƿ��Ѿ������ˣ����ص���ArrayList--Go2Term
	 * @param Go2Term
	 * @return
	 */
	public ArrayList<Go2Term> queryLsGoIDconvert(Go2Term queryGo2Term);
	
	public void insertGoIDconvert(Go2Term Go2Term);
	
	/**
	 * �� GOIDȥquery
	 * @param Go2Term
	 */
	public void updateGoIDconvertWhereGOID(Go2Term Go2Term);
	/**
	 * ��Query GOIDȥquery
	 * @param Go2Term
	 */
	public void updateGoIDconvertWhereQueryGOID(Go2Term Go2Term);
}
