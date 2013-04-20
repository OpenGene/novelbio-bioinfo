package com.novelbio.database.updatedb.database;

import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 常规的添加GeneID信息的类 表格格式：
 * 
 * taxID \t accID \t geneIDgeneID(refAccID) \t dbinfo \t symbol \t synoms \t fullName \t description \t pubmedID(数字)
 * <br><br>
 * <b>taxID</b>: 物种ID，在NCBI的Taxonomy 中查找 <br>
 * <b>accID</b>: 待导入的accID，只能有一个<br>
 * <b>geneID(refAccID): 如果是geneID，则该列为NCBI的geneID或者uniprot的ID，该ID只能有一个。
         如果是refAccID，则该列为所对应到的其他refAccID，如NCBI的NM编号或NP编号。refAccID可以为多个，用英文分号";"隔开
 * <b>dbinfo</b>: 数据库名称，必须能在数据库中查到<br>
 * <b>symbol</b>: 基因学名，每个基因只有一个基因学名<br>
 * <b>synoms</b>: 基因别名，每个基因可以有多个别名，用英文分号";"隔开<br>
 * <b>fullName</b>: 基因全名，每个基因可以有多个全名，用英文分号";"隔开<br>
 * <b>description</b>: 基因描述，每个基因只有一个描述<br>
 * <b>pubmedID</b>: 文献编号，必须是数字。每个基因可以有多个文献编号，用英文分号";"隔 仅添加geneID信息<br>
 * @author zong0jie
 * 
 */
public class UpdateGeneInfoNorm extends ImportPerLine {
	/** 是否覆盖数据库 */
	boolean overlap = true;	
	/** 第二列为geneID，false第二列为refID并且用英文分号隔开 */
	boolean refGeneID = true;
	
	public void setRefGeneID(boolean refGeneID) {
		this.refGeneID = refGeneID;
	}
	/** 如果同一个ID在数据库中已经存在了不同的dbInfo，是否用新的代替老的信息 */
	public void setOverlap(boolean overlap) {
		this.overlap = overlap;
	}
	
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		GeneID geneID = null;
		if (ss[0].trim().equals("")) {
			return true;
		} else if (ss.length < 3 || ss[2].trim().equals("")) {
			geneID = new GeneID(ss[2], taxID);
		} else {
			geneID = generateGeneID(ss[0], ss[1], ss[2]);
		}
		
		GeneInfo geneInfo = new GeneInfo();
		if (ss.length >= 4) {
			geneID.setUpdateDBinfo(ss[3].trim(), overlap);
			geneInfo.setDBinfo(ss[3].trim());
		}
		if (ss.length >= 5) {
			geneInfo.setSymb(ss[4]);
		}
		if (ss.length >= 6) {
			String[] synoms = ss[5].split("\t");
			for (String string : synoms) {
				geneInfo.addSynonym(string);
			}
		}
		if (ss.length >= 7) {
			String[] fullNames = ss[6].split("\t");
			for (String string : fullNames) {
				geneInfo.addFullName(string);
			}
		}
		if (ss.length >= 8) {
			geneInfo.setDescrp(ss[7].trim());
		}
		if (ss.length >= 9) {
			String[] pubmeds = ss[8].split("\t");
			for (String string : pubmeds) {
				geneInfo.addPubID(string);
			}
		}
		if (ss.length >= 5) {
			geneID.setUpdateGeneInfo(geneInfo);
		}
		
		return geneID.update(true);
	}
	
	private GeneID generateGeneID(String ss0, String ss1, String ss2) {
		int taxID = Integer.parseInt(ss0.trim());
		GeneID geneID = null;
		if (refGeneID) {
			try {
				Integer.parseInt(ss2.trim());
				geneID = new GeneID(GeneID.IDTYPE_GENEID, ss2.trim(), taxID);
			} catch (Exception e) {
				geneID = new GeneID(GeneID.IDTYPE_UNIID, ss2.trim(), taxID);
			}
			geneID.setUpdateAccID(ss1);
		} else {
			geneID = new GeneID(ss1, taxID);
			geneID.addUpdateRefAccID(ss2.split(";"));
		}
		return geneID;
	}
	
}
