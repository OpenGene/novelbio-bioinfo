package com.novelbio.analysis.seq.genomeNew2.gffOperate;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.database.model.modcopeid.CopedID;

public class GffDetailGeneCod extends GffDetailAbsCod<GffDetailGene>{
	ArrayList<GffGeneIsoInfoCod> lsGffInfoCod = null;
	public GffDetailGeneCod(GffDetailGene gffDetail, int coord) {
		super(gffDetail, coord);
		getLsCodSplit();
	}
//	public ArrayList<GffGeneIsoInfoCod> getLsGffInfoCod()
//	{
//		return lsGffInfoCod;
//	}
	/**
	 * ��gffDetailGene��������
	 */
	@Deprecated
	public void setCoord(int coord) {
		super.setCoord(coord);
//		if (gffDetail.getLsCodSplit() == null) {
//			return;
//		}
//		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetail.getLsCodSplit()) {
//			lsGffInfoCod.add(gffGeneIsoInfo.setCod(coord));
//		}
	}
	
	/**
	 * �Ƿ��ڸû����ڣ��������
	 * @return
	 * ����anno[4]
	 * 0��accID
	 * 1��symbol
	 * 2��description
	 * 3��location
	 * û�оͷ��ء���
	 */
	public String[] getInfo() {
		String[] anno = new String[4];
		for (int i = 0; i < anno.length; i++) {
			anno[i] = "";
		}
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		if (isCodInGenExtend()) {
			for (GffGeneIsoInfoCod gffGeneIsoInfoCod : getLsCodSplit()) {
				if (gffGeneIsoInfoCod.isCodInIsoExtend()) {
					hashCopedID.add(gffGeneIsoInfoCod.getGffGeneIso().getCopedID());
				}
			}
			for (CopedID copedID : hashCopedID) {
				if (anno.equals("")) {
					anno[0] = copedID.getAccID();
					anno[1] = copedID.getSymbo();
					anno[2] = copedID.getDescription();
				}
				else {
					anno[0] = anno[0]+"//"+copedID.getAccID();
					anno[1] = anno[1]+"//"+copedID.getSymbo();
					anno[2] = anno[2]+"//"+copedID.getDescription();
				}
			}
			if (getLongestSplit().isCodInIsoExtend()) {
				anno[4] = getLongestSplit().getCodLocStr();
			}
			else {
				for (GffGeneIsoInfoCod gffGeneIsoInfo : getLsCodSplit()) {
					if (gffGeneIsoInfo.isCodInIsoExtend()) {
						anno[4] = gffGeneIsoInfo.getCodLocStr();
						break;
					}
				}
			}
		}
		return anno;
	}
	
    /**
     * �������(��0��ʼ����Ų���ת¼���ľ���ID)<br>
     * ����ĳ��ת¼���ľ�����Ϣ
     */
    public GffGeneIsoInfoCod getIsolist(int splitnum)
    {
    	return getLsCodSplit().get(splitnum);
    }
    /**
     * ����ת¼����(UCSC��ʵ�����ǻ�����)<br>
     */
    public GffGeneIsoInfoCod getIsolist(String splitID)
    {
    	int id =  gffDetail.getIsolistID(splitID);
    	return getLsCodSplit().get(id);
    }
    
	/**
	 * ��øû��������һ��ת¼������Ϣ
	 * 
	 * @return <br>
	 */
	public GffGeneIsoInfoCod getLongestSplit()
	{
	 	int id =  gffDetail.getLongestSplitID();
    	return getLsCodSplit().get(id);
	}
	/**
	 * ��������Ҿ����ת¼����Ϣ�����������Ϣ��ͬ���򷵻���ǰ����Ϣ
	 * @param coord
	 */
	public ArrayList<GffGeneIsoInfoCod> getLsCodSplit() {
		if (gffDetail.getLsCodSplit() == null) {
			return null;
		}
		if (lsGffInfoCod == null) {
			lsGffInfoCod = new ArrayList<GffGeneIsoInfoCod>();
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetail.getLsCodSplit()) {
				lsGffInfoCod.add(gffGeneIsoInfo.setCod(coord));
			}
		}
		return lsGffInfoCod;
	}
	

}
