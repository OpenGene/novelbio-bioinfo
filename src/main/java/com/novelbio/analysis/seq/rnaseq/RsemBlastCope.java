package com.novelbio.analysis.seq.rnaseq;

import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.modgeneid.GeneID;

/** 
 * trinity获得的结果会做blast，因为是用iso做的blast，
 * 所以得到的结果要进行整理成为表格
 * 仅将最同源的挑选出来
 * @author zong0jie
 *
 */
public class RsemBlastCope {
	public static void main(String[] args) {
		String blastFile = "/media/winF/NBC/Project/Project_WH/rnatBlastx2Ath";
		String rsemGeneResult = "/media/winF/NBC/Project/Project_WH/pet0/RSEM.genes.results";
		RsemBlastCope rsemBlastCope = new RsemBlastCope();
		rsemBlastCope.setBlastFile(blastFile);
		rsemBlastCope.setIso2GeneID(rsemGeneResult);
		rsemBlastCope.copeBlastFile();
		rsemBlastCope.writeToFile();
	}
	
	String blastFile;
	RsemGetGene2Iso rsemGetGene2Iso;
	HashMap<BlastInfoHash, BlastInfoHash> mapBlastInfo = new HashMap<BlastInfoHash, BlastInfoHash>();
	
	public void setBlastFile(String blastFile) {
		this.blastFile = blastFile;
	}
	public void setIso2GeneID(String rsemGeneResult) {
		rsemGetGene2Iso = new RsemGetGene2Iso();
		rsemGetGene2Iso.setRsemGeneResult(rsemGeneResult);
		rsemGetGene2Iso.calculateResult();
	}
	public void copeBlastFile() {
		TxtReadandWrite txtRead = new TxtReadandWrite(blastFile, false);
		BlastInfoHash.setRsemGetGene2Iso(rsemGetGene2Iso);
		for (String blastInfoLine : txtRead.readlines()) {
			BlastInfoHash blastInfoHash = new BlastInfoHash(blastInfoLine);
			if (blastInfoHash.getEvalue() > 1e-10) {
				continue;
			}
			if (mapBlastInfo.containsKey(blastInfoHash)) {
				BlastInfoHash blastInfoHashValue = mapBlastInfo.get(blastInfoHash);
				modifyBlastInfo(blastInfoHashValue, blastInfoHash);
			} else {
				mapBlastInfo.put(blastInfoHash, blastInfoHash);
			}
		}
		txtRead.close();
	}
	/** 
	 * 用blastInfoHashSample来修正blastInfoHashModify
	 * 主要就是如果发现evalue更高则取代原来的
	 * @param blastInfoHashModify
	 * @param blastInfoHashSample
	 */
	private void modifyBlastInfo(BlastInfoHash blastInfoHashModify, BlastInfoHash blastInfoHashSample) {
		if (blastInfoHashModify.getEvalue() > blastInfoHashSample.getEvalue()) {
			blastInfoHashModify.setSubjectID(blastInfoHashSample.getSubjectID());
			blastInfoHashModify.setEvalue(blastInfoHashSample.getEvalue());
			blastInfoHashModify.setIdentities(blastInfoHashSample.getIdentities());
		}
	}
	
	private void writeToFile() {
		String out = FileOperate.changeFileSuffix(blastFile, "_OutBlast", null);
		TxtReadandWrite txtWrite = new TxtReadandWrite(out, true);
		for (BlastInfoHash blastInfoHash : mapBlastInfo.values()) {
			txtWrite.writefileln(blastInfoHash.toString());
		}
	}
}
/** 实现hashCode和equals方法的blastInfo，方便放入hashset中用来去重复 */
class BlastInfoHash extends BlastInfo {
	static RsemGetGene2Iso rsemGetGene2Iso;
	/** 比对得分 */
	double score;
	String[] blastStr;
	
	public static void setRsemGetGene2Iso(RsemGetGene2Iso rsemGetGene2Iso) {
		BlastInfoHash.rsemGetGene2Iso = rsemGetGene2Iso;
	}
	public BlastInfoHash(String blastInfoLine) {
		String[] ss = blastInfoLine.split("\t");
		super.evalue = Double.parseDouble(ss[10]);
		super.identities = Double.parseDouble(ss[2]);
		String queryID = GeneID.removeDot(ss[0]);
		super.queryID = rsemGetGene2Iso.getGeneName(queryID);
		super.subjectID = GeneID.removeDot(ss[1]);
		score = Double.parseDouble(ss[11]);
		blastStr = ss;
		blastStr[0] = super.queryID;
	}
	/** 仅比较queryID，subjectID */
	public int hashCode() {
		String blastString = queryID;
		return blastString.hashCode();
	}
	/** 仅比较queryID */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		BlastInfoHash otherObj = (BlastInfoHash)obj;
		if (
		 queryID.equals(otherObj.queryID)

		)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ArrayOperate.cmbString(blastStr, "\t");
	}
}