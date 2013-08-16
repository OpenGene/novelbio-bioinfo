package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.model.modgeneid.GeneID;
/**
 * 批量注释
 * @author zong0jie
 *
 */
public class AnnoQuery extends RunProcess<AnnoQuery.AnnoQueryDisplayInfo>{
//	public static void main(String[] args) {
//		String geneFile = "/media/winF/NBC/Project/RNA-Seq_CR_20111201/CR.xls";
//		String txtOut = FileOperate.changeFileSuffix(geneFile, "_annotest", null);
//		AnnoQuery annoQuery = new AnnoQuery();
//		annoQuery.setColAccIDFrom1(1);
//		annoQuery.setFirstLineFrom1(2);
//		annoQuery.setGeneIDFile(geneFile);
//		annoQuery.writeTo(txtFile);
//	}
	
	ArrayList<String[]> lsGeneID;
	ArrayList<String[]> lsResult;
	int colAccID = 0;
	String regex = "";
	boolean blast = false;
	double evalue = 1e-10;
	int taxIDthis = 0;
	int taxIDblastTo = 0;
	
	int firstLine = 0;
	AnnoAbs annoAbs;
	/**
	 * 可以输入txt或excel
	 * @param geneIDfile
	 */
	public void setGeneIDFile(String geneIDfile) {
		lsGeneID = ExcelTxtRead.readLsExcelTxt(geneIDfile, 1);
	}
	/** 输入待查找的gene列表信息 */
	public void setLsGeneID(ArrayList<String[]> lsGeneID) {
		this.lsGeneID = lsGeneID;
	}
	/** 第一行从哪里开始，实际行 */
	public void setFirstLineFrom1(int firstLine) {
		if (firstLine >= 1) {
			this.firstLine = firstLine - 1;
		}
	}
	public void setColAccIDFrom1(int colAccID) {
		this.colAccID = colAccID - 1;
	}
	public void setTaxIDthis(int taxIDthis) {
		this.taxIDthis = taxIDthis;
	}
	public void setTaxIDblastTo(int taxIDblastTo) {
		this.taxIDblastTo = taxIDblastTo;
	}
	public void setBlast(boolean blast) {
		this.blast = blast;
	}
	/**
	 * 设定annotation的种类
	 * @param annoType 主要是{@link AnnoAbs#ANNOTATION} {@link AnnoAbs#GO} 等
	 */
	public void setAnnoType(int annoType) {
		annoAbs = AnnoAbs.createAnnoAbs(annoType);
	}
	/** 只有当annoType为 {@link AnnoAbs#GO} 时，才有设置的必要 */
	public void setGOtype(GOtype gOtype) {
		if (annoAbs instanceof AnnoGO) {
			((AnnoGO) annoAbs).setgOtype(gOtype);
		}
	}
	public ArrayList<String[]> getLsResult() {
		return lsResult;
	}
	public void writeTo(String txtFile) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtFile, true);
		txtWrite.ExcelWrite(lsResult);
		txtWrite.close();
	}

	@Override
	protected void running() {
		anno();
	}
	
	private void anno() {
		annoAbs.setBlastToTaxID(taxIDblastTo, evalue);
		annoAbs.setTaxIDquery(taxIDthis);
		annoAbs.setBlast(blast);
		
		lsResult = new ArrayList<String[]>();
		if (firstLine >= 1) {
			lsResult.add(getTitle());
		}
		for (int i = firstLine; i < lsGeneID.size(); i++) {
			String accID = lsGeneID.get(i)[colAccID];
			if (regex != null && !regex.equals("")) {
				accID = accID.split(regex)[0];
			}
			List<String[]> lsTmpResult = null;
			try {
				lsTmpResult = annoAbs.getInfo(lsGeneID.get(i), accID);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (lsTmpResult != null) {
				lsResult.addAll(lsTmpResult);
				setRunInfo(i, lsTmpResult);
			}
			
			suspendCheck();
	
			if (flagStop) {
				break;
			}
		}
	}
	
	private void setRunInfo(int num, List<String[]> lsTmpInfo) {
		for (String[] tmpInfo : lsTmpInfo) {
			AnnoQueryDisplayInfo annoQueryDisplayInfo = new AnnoQueryDisplayInfo();
			annoQueryDisplayInfo.countNum = num;
			annoQueryDisplayInfo.tmpInfo = tmpInfo;
			if (runGetInfo != null) {
				runGetInfo.setRunningInfo(annoQueryDisplayInfo);
			}
			
		}
	}
	
	public String[] getTitle() {
		return annoAbs.getTitle(lsGeneID.get(firstLine - 1));
	}
	
	public static void addGeneID(String geneFile, String out, int colGeneID, int taxID) {
		TxtReadandWrite txtOut = new TxtReadandWrite(out, true);
		colGeneID--;
		ArrayList<String[]> lsGeneInfo = ExcelTxtRead.readLsExcelTxt(geneFile, 1);
		for (String[] string : lsGeneInfo) {
			String accID = string[colGeneID];
			GeneID copedID = new GeneID(accID, taxID);
			String[] resString = ArrayOperate.copyArray(string, string.length + 1);
			resString[resString.length - 1] = copedID.getGeneUniID();
			txtOut.writefileln(resString);
		}
		txtOut.close();
	}
	
	public static class AnnoQueryDisplayInfo {
		long countNum;
		String[] tmpInfo;
		public void setCountNum(long countNum) {
			this.countNum = countNum;
		}
		public void setTmpInfo(String[] tmpInfo) {
			this.tmpInfo = tmpInfo;
		}
		public long getCountNum() {
			return countNum;
		}
		public String[] getTmpInfo() {
			return tmpInfo;
		}
	}
}


