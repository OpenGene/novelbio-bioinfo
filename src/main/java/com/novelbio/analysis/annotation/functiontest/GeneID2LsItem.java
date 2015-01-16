package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.annotation.cog.COGanno;
import com.novelbio.analysis.annotation.cog.CogInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageGo2Term;

public abstract class GeneID2LsItem {
	private static final Logger logger = Logger.getLogger(GeneID2LsItem.class);
	
	String geneUniID;
	/** 内部为GO:00001 或 PATHID:00001
	 * 均为大写
	 * */
	Set<String> setItemID = new HashSet<String>();
	GeneID geneID;
	
	public abstract void setGeneID(GeneID geneID, boolean blast);
	
	public void setGeneUniID(String geneUniID) {
		if (geneUniID == null) {
			return;
		}
		this.geneUniID = geneUniID.trim();
	}
	
	public String getGeneUniID() {
		return geneUniID;
	}
	
	public void addItemID(String itemID) {
		if (itemID == null) {
			return;
		}
		setItemID.add(itemID.toUpperCase());
	}
	
	public Set<String> getSetItemID() {
		return setItemID;
	}
	/**
	 * 是否有效
	 * @return
	 */
	public boolean isValidate() {
		if (setItemID.size() == 0 || geneUniID == null || geneUniID.equals("")) {
			return false;
		}
		return true;
	}
	/**
	 * 返回类似 LOCOs01g11110  GO:10001, GO:10002 这种
	 */
	public String toString() {
		if (setItemID.size() == 0 || geneUniID == null || geneUniID.equals("")) {
			return "";
		}
		String result = geneUniID + "\t";
		int i = 0;
		for (String itemID : setItemID) {
			if (i == 0) {
				result = result + itemID;
			} else {
				result = result + "," + itemID;
			}
			i++;
		}
		return result;
	}

}

/** 从txt文件中读取go信息，而不是从数据库中读取 */
class GeneID2LsGo extends GeneID2LsItem {
	GOtype goType;
	/** key为小写基因名 */
	HashMultimap<String, String> mapGene2LsItem;
	/** 是否与已有数据库取并集 */
	boolean isCombine;
	
	protected GeneID2LsGo() {}
	
	public void setGOtype(GOtype goType) {
		this.goType  = goType;
	}
	/** key为小写基因名
	 * 
	 * @param mapGene2LsItem key为小写的基因名，注意输入的基因名应该与检验的基因名一样。不区分大小写。
	 * @param isCombine 是否与已有数据库取并集
	 */
	public void setMapGene2LsItem(HashMultimap<String, String> mapGene2LsItem, boolean isCombine) {
		this.mapGene2LsItem = mapGene2LsItem;
		this.isCombine = isCombine;
	}
	
	@Override
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGeneUniID();
		List<AGene2Go> lsGo = new ArrayList<>();
		if (mapGene2LsItem == null || mapGene2LsItem.isEmpty() || isCombine) {
			if (blast) {
				lsGo = geneID.getGene2GOBlast(goType);
			} else {
				lsGo = geneID.getGene2GO(goType);
			}
		}
		if (mapGene2LsItem != null && !mapGene2LsItem.isEmpty()) {
			Set<String> setGOId = mapGene2LsItem.get(geneID.getAccID().toLowerCase());
			for (String string : setGOId) {
				Go2Term go2Term = ManageGo2Term.getInstance().queryGo2Term(string);
				if (go2Term.getGOtype() != goType) {
					continue;
				}
				AGene2Go aGene2Go = new Gene2Go();
				aGene2Go.setGOID(string);
				lsGo.add(aGene2Go);
			}
		}

		for (AGene2Go aGene2Go : lsGo) {
			addItemID(aGene2Go.getGOID());
		}
	}
	
	public static GeneID2LsGo getInstance(int goLevel) {
		if (goLevel < 0) {
			return new GeneID2LsGo();
		} else {
			GeneID2LsGoLevel geneID2LsGo = new GeneID2LsGoLevel();
			geneID2LsGo.setGoLevel(goLevel);
			return geneID2LsGo;
		}
	}
	
	/** 读取GO注释信息，必须第一列为GeneName, 第二列为GOID */
	public static HashMultimap<String, String> readGoTxtFile(String gene2GoTxt) {
		if (!FileOperate.isFileExistAndBigThanSize(gene2GoTxt, 0)) {
			return null;
		}
		HashMultimap<String, String> mapGene2LsGO = HashMultimap.create();
		TxtReadandWrite txtRead = new TxtReadandWrite(gene2GoTxt);
		int colGeneName = 0;
		int colGOITem = 1;
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			mapGene2LsGO.put(ss[colGeneName].trim().toLowerCase(), ss[colGOITem].trim().toUpperCase());
		}
		txtRead.close();
		return mapGene2LsGO;
	}
}

class GeneID2LsPath extends GeneID2LsItem {
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGeneUniID();
		ArrayList<KGpathway> lsPath = geneID.getKegPath(blast);
		for (KGpathway kGpathway : lsPath) {
			addItemID("PATH:" + kGpathway.getMapNum());
		}
	}
}

class GeneID2LsCog extends GeneID2LsItem {
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGeneUniID();
	}
	
	public void setCogInfo(CogInfo cogInfo) {
		if (cogInfo != null) {
			addItemID(cogInfo.getCogId());
		}
	}
	
	/** 正常的geneId2LsCog是geneId对应CogId，类似COG12345这种<br>
	 * 那么在做富集分析的时候需要将COG12345转变为F等单字母形式<br>
	 * 这样才可以做COG的富集分析，所以需要将COG12345等转变为F这种
	 * @return
	 */
	public GeneID2LsCog convert2Abbr(COGanno coGanno) {
		GeneID2LsCog geneID2LsCog = new GeneID2LsCog();
		geneID2LsCog.geneID = geneID;
		geneID2LsCog.geneUniID = geneUniID;
		for (String cogId : getSetItemID()) {
			String abbr = coGanno.queryCogInfoFromCogId(cogId).getCogAbbr();
			for (char charCog : abbr.toCharArray()) {
				geneID2LsCog.addItemID("COG:" + charCog);
			}
		}
		return geneID2LsCog;
	}
}
