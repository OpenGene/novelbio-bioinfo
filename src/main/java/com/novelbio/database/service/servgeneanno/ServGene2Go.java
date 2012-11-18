package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.mapper.geneanno.MapGeneInfo;
import com.novelbio.database.mapper.geneanno.MapGo2Term;
import com.novelbio.database.service.SpringFactory;
@Service
public class ServGene2Go implements MapGene2Go {
	private static Logger logger = Logger.getLogger(ServGene2Go.class);
	@Inject
	private MapGene2Go mapGene2Go;
	public ServGene2Go() {
		mapGene2Go = (MapGene2Go)SpringFactory.getFactory().getBean("mapGene2Go");
	}
	
	public ArrayList<Gene2Go> queryLsGene2Go(int geneID, int taxID) {
		Gene2Go gene2Go = new Gene2Go();
		gene2Go.setGeneUniID(geneID + "");
		gene2Go.setTaxID(taxID);
		return mapGene2Go.queryLsGene2Go(gene2Go);
	}
	public Gene2Go queryGene2Go(String geneID, int taxID,String GOID) {
		if (GOID == null) {
			return null;
		}
		Gene2Go gene2Go = new Gene2Go();
		gene2Go.setGeneUniID(geneID);
		gene2Go.setTaxID(taxID);
		gene2Go.setGOID(GOID);
		return mapGene2Go.queryGene2Go(gene2Go);
	}
	@Override
	public ArrayList<Gene2Go> queryLsGene2Go(Gene2Go gene2Go) {
		return mapGene2Go.queryLsGene2Go(gene2Go);
	}

	@Override
	public Gene2Go queryGene2Go(Gene2Go gene2Go) {
		return mapGene2Go.queryGene2Go(gene2Go);
	}

	@Override
	public void insertGene2Go(Gene2Go gene2Go) {
		// TODO Auto-generated method stub
		mapGene2Go.insertGene2Go(gene2Go);
	}

	@Override
	public void updateGene2Go(Gene2Go gene2Go) {
		// TODO Auto-generated method stub
		mapGene2Go.updateGene2Go(gene2Go);
	}
	/**
	 * 
	 * ����geneUniID�Լ���������ݣ����Ƿ���Ҫ����
	 * �ܲ���Ͳ��룬�Ѿ����˾��ж������ݿ����Ƿ�һ�£���һ�¾�����
	 * @param genUniID
	 * @param taxID �Ը�taxIDΪ׼
	 * @param gene2Go
	 */
	public boolean updateGene2Go(String genUniID, int taxID, AGene2Go gene2Go) {
		gene2Go.setTaxID(taxID);
		Gene2Go gene2GoOld = queryGene2Go(genUniID, taxID, gene2Go.getGOID());
		if (gene2GoOld != null) {
			if (gene2GoOld.addInfo(gene2Go)) {
				updateGene2Go(gene2GoOld);
			}
		}
		else {
			Gene2Go gene2GoNew = new Gene2Go();
			gene2GoNew.copyInfo(gene2Go);
			if (gene2GoNew.getGOID() == null) {
				logger.error("����δ֪GOID��" + gene2Go.getGOID());
				return false;
			}
			gene2GoNew.setGeneUniID(genUniID);
			try {
				insertGene2Go(gene2GoNew);
			} catch (Exception e) {//����ԭ�����������������һ����goID��Ȼ�����������ʱ���һ��goID��û���ü������������µڶ���ֱ�ӾͲ�����Ȼ�����
				System.out.println(gene2Go.getGOID() + " " + gene2GoNew.getGeneUniId() + " " + gene2Go.getTaxID());
				return false;
			}
		}
		return true;
	}
	
}
