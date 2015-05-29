package com.novelbio.analysis.seq.rnaseq.lnc;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.database.model.species.Species;

public class LncSiteInfo {
	/** 输入的一列Lnc */
	List<String> lsLncName;
	List<Align> lsLncAligns;
	/** 向前后扩展 ,默认10000*/
	int upDownExtend = 10000;
	/** 物种对应的Gene信息 */
	GffChrAbs gffChrAbs = new GffChrAbs();

	/**
	 * 
	 * 输入的一个Excel表格
	 * @param lslsExcel
	 * @param colNum 从0开始计数
	 */
	public void setLsLncName(List<List<String>> lslsExcel, int colNum) {
		lsLncName = new ArrayList<String>();
		for (List<String> list : lslsExcel) {
			lsLncName.add(list.get(colNum));
		}
	}

	/** 输入的一列Lnc */
	public void setLsLncName(List<String> lsLncs) {
		this.lsLncName = lsLncs;
	}

	/** 输入的一列Lnc */
	public List<String> getLsLncs() {
		return lsLncName;
	}
	/**
	 * 
	 * 输入的一个Excel表格 colNum从0开始计数，第一列colNum = 0
	 * 
	 * @param lslsExcel
	 * @param colChrID 从0开始
	 * @param colStart 从0开始
	 * @param colEnd 从0开始
	 */
	public void setLsLncAligns(List<List<String>> lslsExcel, int colChrID, int colStart, int colEnd) {
		lsLncAligns = new ArrayList<>();
		for (List<String> list : lslsExcel) {
			try {
				String chrID = list.get(colChrID);
				int startLoc = Integer.parseInt(list.get(colStart));
				int endLoc = Integer.parseInt(list.get(colEnd));
				Align align = new Align(chrID, startLoc, endLoc);
				lsLncAligns.add(align);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	public void setLsLncAligns(List<Align> lsLncAligns) {
		this.lsLncAligns = lsLncAligns;
	}
	
	/** 物种 */
	public void setSpecies(Species species) {
		gffChrAbs.setSpecies(species);
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}

	/** 向前后扩展，意思前后范围内的mRNA都会被抓出来，默认2000bp */
	public void setUpDownExtend(int upDownExtend) {
		this.upDownExtend = upDownExtend;
	}
	/**
	 * 根据一列lnc查询lnc信息
	 * @return
	 */
	public List<LncInfo>  findLncInfoByLoc() {
		List<LncInfo> lsLncInfos = new ArrayList<LncInfo>();
		for (Align align : lsLncAligns) {
			LncInfo lncInfo = new LncInfo(gffChrAbs.getTaxID(), gffChrAbs.getGffHashGene(), upDownExtend);
			lncInfo.setLncCoord(align);
			lncInfo.searchLnc();
			lsLncInfos.add(lncInfo);
		}
		return lsLncInfos;
	}
	
	public List<LncInfo> findLncInfoByName() {
		List<LncInfo> lsLncInfos = new ArrayList<LncInfo>();
		for (String lncName : lsLncName) {
			LncInfo lncInfo = new LncInfo(gffChrAbs.getTaxID(), gffChrAbs.getGffHashGene(), upDownExtend);
			lncInfo.setLncName(lncName);
			lncInfo.searchLnc();
			lsLncInfos.add(lncInfo);
		}
		return lsLncInfos;
	}


}
