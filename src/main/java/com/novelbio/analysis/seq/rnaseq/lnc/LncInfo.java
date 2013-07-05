package com.novelbio.analysis.seq.rnaseq.lnc;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneID;

public class LncInfo {
	int taxID = 0;
	/**基因名称*/
	String lncName;
	GffGeneIsoInfo gffGeneIsoInfoLnc;

	/**重叠区域的mRna*/
	String mRna;
	/**前面最长转录本的基因*/
	String upGene;
	int upDistance;
	
	/**后面最长转录本的基因*/
	String downGene;
	int downDistance;;
	
	boolean cis5to3;
	
	public LncInfo(int taxID) {
		this.taxID = taxID;
	}
	/** 本lnc的方向 */
	public void setCis5to3(boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	public boolean isCis5to3() {
		return cis5to3;
	}
	/**基因名称*/
	public String getLncName() {
		return lncName;
	}
	/**基因名称*/
	public void setLncName(String lncName) {
		this.lncName = lncName;
	}
	
	public void setLncIso(GffGeneIsoInfo gffGeneIsoInfoLnc) {
		this.gffGeneIsoInfoLnc = gffGeneIsoInfoLnc;
	}

	/**重叠区域的mRna*/
	public String getmRna() {
		return mRna;
	}
	/**重叠区域的mRna*/
	public void setmRna(String mRna) {
		this.mRna = mRna;
	}
	/**前面最长转录本的基因*/
	public String getUpGene() {
		return upGene;
	}
	
	public void setUpDownGeneInfo(int upDownExtend, GffChrAbs gffChrAbs) {
		setUpGffDetailGene(upDownExtend, gffChrAbs);
		setDownGffDetailGene(upDownExtend, gffChrAbs);
	}
	
	
	/**
	 * 获取 前一个转录本信息
	 * 
	 * @param detailGene
	 * @return
	 */
	private void setUpGffDetailGene(int upDownExtend, GffChrAbs gffChrAbs) {
		int num = gffGeneIsoInfoLnc.getParentGffDetailGene().getItemNum();
		if (num != 0) {
			GffDetailGene detailGeneUp;
			try {
				detailGeneUp = gffChrAbs.getGffHashGene().getMapChrID2LsGff().get(gffGeneIsoInfoLnc.getRefID()).get(num - 1);
			} catch (Exception e) {
				return;
			}
			int space = detailGeneUp.getEndAbs() - gffGeneIsoInfoLnc.getStartAbs();
			if (space <= upDownExtend) {
				upGene = detailGeneUp.getLongestSplitMrna().getName();
				upDistance = space;
			} else {
				return;
			}
		} else {
			return;
		}
	}
	/**
	 * 获取 下一个转录本信息
	 * 
	 * @param detailGene
	 * @return
	 */
	private void setDownGffDetailGene(int upDownExtend, GffChrAbs gffChrAbs) {
		int num = gffGeneIsoInfoLnc.getParentGffDetailGene().getItemNum();
		GffDetailGene detailGeneDown = null;
		try {
			detailGeneDown = gffChrAbs.getGffHashGene().getMapChrID2LsGff().get(gffGeneIsoInfoLnc.getRefID()).get(num + 1);
		} catch (Exception e) {
			return;
		}
		int space = gffGeneIsoInfoLnc.getEndAbs() - detailGeneDown.getStartAbs();
		if (space <= upDownExtend) {
			downGene = detailGeneDown.getLongestSplitMrna().getName();
			downDistance = space;
		} else {
			return;
		}
	}
	/**
	 * 方便写入EXCEL
	 */
	@Override
	public String toString() {
		List<String> lsResult = new ArrayList<String>();
		lsResult.add(lncName);
		if (gffGeneIsoInfoLnc == null) {
			lsResult.add("");
		} else {
			GeneID geneID = gffGeneIsoInfoLnc.getGeneID();
			String geneType = gffGeneIsoInfoLnc.getGeneType().toString();
			if (geneID != null && geneID.getGeneInfo() != null && geneID.getGeneInfo().getTypeOfGene() != null) {
				String geneTypeGeneID = geneID.getGeneInfo().getTypeOfGene();
				if (!geneTypeGeneID.equals("") && !geneTypeGeneID.equalsIgnoreCase("mrna") && !geneTypeGeneID.toLowerCase().contains("protein")) {
					geneType = geneTypeGeneID;
				}
			}
			lsResult.add(geneType);
		}
	
		if (mRna != null && !mRna.equals("")) {
			lsResult.add(mRna);
			GeneID geneID = new GeneID(mRna, taxID);
			lsResult.add(geneID.getSymbol());
			lsResult.add(geneID.getDescription());
		} else {
			lsResult.add("");
			lsResult.add("");
			lsResult.add("");
		}
		
		if (upGene != null && !upGene.equals("")) {
			GeneID geneID = new GeneID(upGene, taxID);
			lsResult.add(geneID.getSymbol());
			lsResult.add(geneID.getDescription());
			
			lsResult.add(upDistance + "");
		} else {
			lsResult.add("");
			lsResult.add("");
			lsResult.add("");
		}
		
		if (downGene != null && !downGene.equals("")) {
			GeneID geneID = new GeneID(downGene, taxID);
			lsResult.add(geneID.getSymbol());
			lsResult.add(geneID.getDescription());
			
			lsResult.add(downDistance + "");
		} else {
			lsResult.add("");
			lsResult.add("");
		}
		
		return ArrayOperate.cmbString(lsResult.toArray(new String[0]), "\t");
	}
	
	public static String[] getTitle() {
		List<String> lsTitle = new ArrayList<String>();
		lsTitle.add("LncName");
		lsTitle.add("LncGeneType");
		lsTitle.add("mRNA_AccID");
		lsTitle.add("mRNA_Symbol");
		lsTitle.add("mRNA_Description");
		lsTitle.add("UpStream_GeneName");
		lsTitle.add("UpStream_GeneDescription");
		lsTitle.add("Distance_To_UpGene");
		lsTitle.add("DownStream_GeneName");
		lsTitle.add("DownStream_GeneDescription");
		lsTitle.add("Distance_To_DownGene");
		return lsTitle.toArray(new String[0]);
	}
	
}
