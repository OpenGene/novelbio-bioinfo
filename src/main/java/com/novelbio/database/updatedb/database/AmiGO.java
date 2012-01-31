package com.novelbio.database.updatedb.database;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.Patternlocation;
import com.novelbio.database.domain.geneanno.Go2Term;

public class AmiGO {
	/**
	 * http://www.geneontology.org/GO.downloads.files.shtml
	 * GO.terms_alt_ids
	 */
	
	/**
	 * http://www.geneontology.org/GO.downloads.annotations.shtml
	 * annotation
	 */
	
	
	
}


class ImpGOExtObo extends ImportPerLine
{
	private static Logger logger = Logger.getLogger(ImpGOExtObo.class);
	/**
	 * 因为需要多行的导入
	 * 所以覆盖方法
	 */
	@Override
	public void importInfoPerLine(String gene2AccFile, boolean gzip) {
		TxtReadandWrite txtGene2Acc;
		if (gzip)
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, gene2AccFile);
		else 
			txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);
		//从第二行开始读取，第一次导入
		String tmpContent = "";
		for (String content : txtGene2Acc.readlines(2)) {
			if (content.startsWith("[Term]")) {
				tmpContent = content;
			}
			if (content.equals("")) {
				impPerLine(tmpContent);
			}
			tmpContent = tmpContent + "\r\n" + content;
		}
		//从第二行开始读取，第二次导入
		for (String content : txtGene2Acc.readlines(2)) {
			if (content.startsWith("[Term]")) {
				tmpContent = content;
			}
			if (content.equals("")) {
				impPerLineObsolete(tmpContent);
				impPerLineChild(tmpContent);
			}
			tmpContent = tmpContent + "\r\n" + content;
		}
	}
	
	/**
	 * 第一次先倒入已有的信息。
	 */
	@Override
	void impPerLine(String lineContent) {
		String[] ss = lineContent.split("\r\n");
		if (lineContent.contains("is_obsolete: true")) {
			return;
		}
		ArrayList<String> lsQueryID = new ArrayList<String>();
		Go2Term go2Term = new Go2Term();
		for (String string : ss) {
			//GOID
			if (string.startsWith("id:")) {
				go2Term.setGoID(string.replace("id:", "").trim());
			}
			//GOterm
			if (string.startsWith("name:")) {
				go2Term.setGoTerm(string.replace("name:", "").trim());
			}
			//GO Function
			if (string.startsWith("namespace: ")) {
				if (string.equals("namespace: biological_process")) {
					go2Term.setGoFunction(Go2Term.GO_BP);
				}
				if (string.equals("namespace: molecular_function")) {
					go2Term.setGoFunction(Go2Term.GO_MF);
				}
				if (string.equals("namespace: cellular_component")) {
					go2Term.setGoFunction(Go2Term.GO_CC);
				}
			}
			//GO Definition
			if (string.startsWith("def:")) {
				go2Term.setDefinition(string.replace("def:", "").trim());
			}
			//Parent
			if (string.startsWith("is_a:")) {
				String parentGOID = extractGOID(string);
				if (parentGOID == null) {
					logger.error("is_a 中没有对应的GOID：" + string);
				}
				else {
					go2Term.setParent(parentGOID, Go2Term.RELATION_IS);
				}
			}
			if (string.startsWith("is_a:")) {
				String parentGOID = extractGOID(string);
				if (parentGOID == null) {
					logger.error("is_a 中没有对应的GOID：" + string);
				}
				else {
					go2Term.setParent(parentGOID, Go2Term.RELATION_IS);
				}
			}
			if (string.startsWith("relationship:")) {
				String parentGOID = extractGOID(string);
				if (parentGOID == null) {
					logger.error("is_a 中没有对应的GOID：" + string);
					return;
				}
				if (string.contains("part_of")) {
					go2Term.setParent(parentGOID, Go2Term.RELATION_PARTOF);
				}
				else if (string.contains("negatively_regulates")) {
					go2Term.setParent(parentGOID, Go2Term.RELATION_REGULATE_NEG);
				}
				else if (string.contains("regulates")) {
					go2Term.setParent(parentGOID, Go2Term.RELATION_REGULATE);
				}
				else if (string.contains("positively_regulates")) {
					go2Term.setParent(parentGOID, Go2Term.RELATION_REGULATE_POS);
				}
			}
			if (string.startsWith("alt_id:")) {
				lsQueryID.add(extractGOID(string));
			}
		}
		go2Term.update();
		for (String string2 : lsQueryID) {
			go2Term.setGoIDQuery(string2);
			go2Term.update();
		}
	}
	
	/**
	 * 第二次才导入 过时GO 信息
	 */
	void impPerLineObsolete(String lineContent) {
		String[] ss = lineContent.split("\r\n");
		if (!lineContent.contains("is_obsolete: true")) {
			return;
		}
		ArrayList<String> lsGOIDConsider = new ArrayList<String>();
		ArrayList<String> lsGOIDReplace = new ArrayList<String>();
		String GOID = "";
		for (String string : ss) {
			//Parent
			if (string.startsWith("is_a:")) {
				GOID = extractGOID(string);
				break;
			}
		}
			
		for (String string : ss) {
			if (string.startsWith("consider:")) {
				lsGOIDConsider.add(string.replace("consider:", "").trim());
			}
			if (string.startsWith("replaced_by:")) {
				lsGOIDReplace.add(string.replace("replaced_by:", "").trim());
			}
		}
		if (lsGOIDReplace.size() > 0) {
			importReplaceAndConsider(lsGOIDReplace, GOID);			
		}
		else if (lsGOIDConsider.size() > 0) {
			importReplaceAndConsider(lsGOIDConsider, GOID);			
		}
	}
	/**
	 * 首先导入是BP的信息，如果没有BP信息，才导入常规信息
	 * 从后向前导入，因为越后面的分类越细
	 * @param lsReplaceAndConsider
	 * @param GOID
	 */
	private void importReplaceAndConsider(ArrayList<String> lsReplaceAndConsider, String GOID)
	{
		for (int i = lsReplaceAndConsider.size() - 1; i >= 0; i--) {
			Go2Term go2Term = Go2Term.queryGo2Term(lsReplaceAndConsider.get(i));
			if (go2Term.getGoFunction().equals(Go2Term.FUN_SHORT_BIO_P)) {
				go2Term.setGoIDQuery(GOID);
				go2Term.update();
				return;
			}
		}
		Go2Term go2Term = Go2Term.queryGo2Term(lsReplaceAndConsider.get(lsReplaceAndConsider.size() - 1));
		go2Term.setGoIDQuery(GOID);
		go2Term.update();
		return;
	
	}
	/**
	 * 第二次才导入  子类信息
	 */
	void impPerLineChild(String lineContent) {
		String[] ss = lineContent.split("\r\n");
		if (lineContent.contains("is_obsolete: true")) {
			return;
		}
		String childID = "";
		for (String string : ss) {
			if (string.startsWith("id:")) {
				childID = string.replace("id:", "").trim();
			}
		}
		for (String string : ss) {
			//Parent
			if (string.startsWith("is_a:")) {
				String GOID = extractGOID(string);
				if (GOID == null) {
					logger.error("is_a 中没有对应的GOID：" + string);
				}
				else {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.setChild(childID, Go2Term.RELATION_IS);
					go2Term.update();
				}
			}
			if (string.startsWith("relationship:")) {
				String GOID = extractGOID(string);
				if (GOID == null) {
					logger.error("is_a 中没有对应的GOID：" + string);
					return;
				}
				if (string.contains("part_of")) {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.setChild(childID, Go2Term.RELATION_PARTOF);
					go2Term.update();
				}
				else if (string.contains("negatively_regulates")) {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.setChild(childID, Go2Term.RELATION_REGULATE_NEG);
					go2Term.update();
				}
				else if (string.contains("relationship: regulates")) {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.setChild(childID, Go2Term.RELATION_REGULATE);
					go2Term.update();
				}
				else if (string.contains("positively_regulates")) {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.setChild(childID, Go2Term.RELATION_REGULATE_POS);
					go2Term.update();
				}
			}
		}
	}
	private String extractGOID(String GOIDstring)
	{
		ArrayList<String[]> lsResult = Patternlocation.getPatLoc(GOIDstring, "GO:\\d+", false);
		if (lsResult == null || lsResult.size() == 0) {
			return null;
		}
		return lsResult.get(0)[0];
	}
}
