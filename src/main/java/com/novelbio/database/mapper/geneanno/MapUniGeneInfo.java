package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.mapper.MapperSql;

public interface MapUniGeneInfo extends MapperSql {
	
	/**
	 * ��Gene2GoInfo��ȥ����UniGeneInfo��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��geneIDȥ�������ݿ�
	 * @param GeneID
	 * @return
	 */
	public UniGeneInfo queryUniGeneInfo(UniGeneInfo uniGeneInfo);
	
	
	public void insertUniGeneInfo(UniGeneInfo uniGeneInfo);
	
	/**
	 * ��geneID���ң�����ȫ����Ŀ��
	 * @param geneInfo
	 */
	public void updateUniGeneInfo(UniGeneInfo uniGeneInfo);

}
