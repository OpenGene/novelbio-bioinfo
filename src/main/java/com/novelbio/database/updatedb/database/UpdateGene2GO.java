package com.novelbio.database.updatedb.database;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 常规的添加GeneID信息的类
 * 定义表格格式：
 * taxID \t geneID(refAccID) \t  goID \t evidence \t pubmedID(Num) \t qualifier \t dbinfo
 * 仅添加geneID信息
 * @author zong0jie
 *
 */
public class UpdateGene2GO extends ImportPerLine {
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		if (ss[0].trim().equals("") || ss.length < 3) {
			return true;
		}
		GeneID geneID = generateGeneID(ss[0], ss[1]);
		AGene2Go gene2Go = new Gene2Go();
		gene2Go.setGOID(ss[2].trim());
		if (ss.length >= 4) {
			String[] evidence = ss[3].split(";");
			for (String string : evidence) {
				gene2Go.addEvidence(string.trim());
			}
		}
		if (ss.length >= 5) {
			String[] pubmedIDs = ss[4].split(";");
			for (String string : pubmedIDs) {
				try {
					Integer.parseInt(string);
					string = "PMID:" + string;
				} catch (Exception e) {}
				gene2Go.addReference(string.trim());
			}
		}
		if (ss.length >= 6) {
			gene2Go.setQualifier(ss[5]);
		}
		if (ss.length >= 7) {
			gene2Go.addDBName(ss[6]);
		}
		geneID.addUpdateGO(gene2Go);
		return geneID.update(false);
	}
	
	private GeneID generateGeneID(String ss0, String ss1) {
		int taxID = Integer.parseInt(ss0.trim());
		GeneID geneID = null;
		if (ss1.toLowerCase().trim().startsWith("geneid")) {
			ss1 = ss1.split(";")[1].trim();
			try {
				Integer.parseInt(ss1);
				geneID = new GeneID(GeneID.IDTYPE_GENEID, ss1, taxID);
			} catch (Exception e) {
				geneID = new GeneID(GeneID.IDTYPE_UNIID, ss1, taxID);
			}
		} else {
			geneID = new GeneID("", taxID);
			geneID.addUpdateRefAccID(ss1.split(";"));
		}
		return geneID;
	}
}
