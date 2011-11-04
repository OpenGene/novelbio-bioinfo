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
	 * 从gffDetailGene产生本类
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
	 * 是否在该基因内，具体情况
	 * @return
	 * 返回anno[4]
	 * 0：accID
	 * 1：symbol
	 * 2：description
	 * 3：location
	 * 没有就返回“”
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
     * 给定编号(从0开始，编号不是转录本的具体ID)<br>
     * 返回某个转录本的具体信息
     */
    public GffGeneIsoInfoCod getIsolist(int splitnum)
    {
    	return getLsCodSplit().get(splitnum);
    }
    /**
     * 给定转录本名(UCSC里实际上是基因名)<br>
     */
    public GffGeneIsoInfoCod getIsolist(String splitID)
    {
    	int id =  gffDetail.getIsolistID(splitID);
    	return getLsCodSplit().get(id);
    }
    
	/**
	 * 获得该基因中最长的一条转录本的信息
	 * 
	 * @return <br>
	 */
	public GffGeneIsoInfoCod getLongestSplit()
	{
	 	int id =  gffDetail.getLongestSplitID();
    	return getLsCodSplit().get(id);
	}
	/**
	 * 用坐标查找具体的转录本信息，如果坐标信息相同，则返回以前的信息
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
