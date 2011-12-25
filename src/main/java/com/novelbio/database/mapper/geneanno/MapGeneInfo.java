package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.mapper.MapperSql;

public interface MapGeneInfo extends MapperSql{
	/**
	 * ��GeneInfo��ȥ����GeneInfo��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��geneIDȥ�������ݿ�
	 * @param geneInfo
	 * @return
	 */
	public GeneInfo queryGeneInfo(GeneInfo geneInfo);
	
	/**
	 * ��GeneInfo��ȥ����GeneInfo��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��geneIDȥ�������ݿ�
	 * 	@param geneInfo
	 * @return
	 */
	public ArrayList<GeneInfo> queryLsGeneInfo(GeneInfo geneInfo);
	
	public void insertGeneInfo(GeneInfo geneInfo);
	
	/**
	 * ��geneID���ң�����ȫ����Ŀ��
	 * @param geneInfo
	 */
	public void updateGeneInfo(GeneInfo geneInfo);

}
