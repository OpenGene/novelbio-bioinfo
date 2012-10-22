package com.novelbio.analysis.seq.rnaseq;

import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.modgeneid.GeneID;

/** 
 * trinity��õĽ������blast����Ϊ����iso����blast��
 * ���Եõ��Ľ��Ҫ���������Ϊ���
 * ������ͬԴ����ѡ����
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
	 * ��blastInfoHashSample������blastInfoHashModify
	 * ��Ҫ�����������evalue������ȡ��ԭ����
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
/** ʵ��hashCode��equals������blastInfo���������hashset������ȥ�ظ� */
class BlastInfoHash extends BlastInfo {
	static RsemGetGene2Iso rsemGetGene2Iso;
	/** �ȶԵ÷� */
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
	/** ���Ƚ�queryID��subjectID */
	public int hashCode() {
		String blastString = queryID;
		return blastString.hashCode();
	}
	/** ���Ƚ�queryID */
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