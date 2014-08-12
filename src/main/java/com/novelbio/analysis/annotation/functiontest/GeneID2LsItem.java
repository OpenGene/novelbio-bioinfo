package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.cog.COGanno;
import com.novelbio.analysis.annotation.cog.CogInfo;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;

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

class GeneID2LsGo extends GeneID2LsItem {
	GOtype goType;
	
	protected GeneID2LsGo() {}
	
	public void setGOtype(GOtype goType) {
		this.goType  = goType;
	}
	@Override
	public void setGeneID(GeneID geneID, boolean blast) {
		this.geneID = geneID;
		this.geneUniID = geneID.getGeneUniID();
		List<AGene2Go> lsGo = null;
		if (blast) {
			lsGo = geneID.getGene2GOBlast(goType );
		} else {
			lsGo = geneID.getGene2GO(goType );
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
		addItemID(cogInfo.getCogId());
	}
	
	/** 正常的geneId2LsCog是geneId对应CogId，类似COG12345这种<br>
	 * 那么在做富集分析的时候需要将COG12345转变为F等单字母形式<br>
	 * 这样才可以做COG的富集分析，所以需要将COG12345等转变为F这种
	 * @return
	 */
	public GeneID2LsCog convert2Abbr(COGanno coGanno) {
		GeneID2LsCog geneID2LsCog = new GeneID2LsCog();
		geneID2LsCog.setGeneID(geneID, true);
		for (String cogId : geneID2LsCog.getSetItemID()) {
			String abbr = coGanno.queryCogInfoFromCogId(cogId).getCogAbbr();
			for (char charCog : abbr.toCharArray()) {
				geneID2LsCog.addItemID("COG:" + charCog);
			}
		}
		return geneID2LsCog;
	}
}
