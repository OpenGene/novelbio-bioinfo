package com.novelbio.analysis.tools;

import java.util.HashMap;

import org.apache.log4j.TTCCLayout;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * illumina�ļ׻���������������
 * @author zong0jie
 */
public class IlluminaMethy {
	HashMap<String, String[]> hashProbe2Gene = new HashMap<String, String[]>();
	public static void main(String[] args) {
		String geneInfoFile = "/media/winF/NBC/Project/Methy_DR_120522/DiffMethylation/TableControl all 6 samples none normalization.txt";
		String dmpOutFile = "/media/winF/NBC/Project/Methy_DR_120522/RawData/dmpFinal";
		String finalFile = "/media/winF/NBC/Project/Methy_DR_120522/RawData/dmpFinalInfo";
		IlluminaMethy illuminaMethy = new IlluminaMethy();
//		illuminaMethy.setInfo(geneInfoFile, 2, 45, 46);
		illuminaMethy.modifyMethyResult("/media/winF/NBC/Project/Methy_DR_120522/RawData/dmpFinalInfo_filter.txt");
	}
	/**
	 * 
	 * ���ÿ��̽���Ӧ�Ļ����λ��
	 * col����ʵ������
	 * @param geneInfoFile
	 * @param colProbID
	 * @param colGeneName
	 * @param colDescription
	 */
	public void setInfo(String geneInfoFile, int colProbID, int colGeneName, int colDescription) {
		colProbID --; colGeneName --; colDescription --;
		TxtReadandWrite txtGene = new TxtReadandWrite(geneInfoFile, false);
		for (String string : txtGene.readlines()) {
			String[] ss = string.split("\t");
			try {
				String[] tmpInfo = new String[]{ss[colGeneName], ss[colDescription]};
				hashProbe2Gene.put(ss[colProbID], tmpInfo);
			} catch (Exception e) {
				continue;
			}

		}
	}
	/**
	 * ����R�����Ĳ���׻����ļ�
	 * @param dmpOutFile
	 */
	public void readDmpOut(String dmpOutFile, String finalFile) {
		TxtReadandWrite txtDmp = new TxtReadandWrite(dmpOutFile, false);
		TxtReadandWrite txtFinal = new TxtReadandWrite(finalFile, true);
		for (String string : txtDmp.readlines()) {
			String[] ss = string.split("\t");
			String[] info = hashProbe2Gene.get(ss[0]);
			if (info == null) {
				continue;
			}
			String[] tmpFinal = ArrayOperate.combArray(ss, info, 0);
			txtFinal.writefileln(tmpFinal);
		}
		txtFinal.close();
	}
	
	/**
	 * ����ɸѡ�õĽ���ļ������ļ�����ɿ��Կ�����ʽ
	 * @param finalFile
	 */
	public void modifyMethyResult(String finalFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(finalFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(finalFile, "_final", null), true);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			//ȥ��ÿһ�е��������
			String[] tmpResult = new String[ss.length - 2];
			for (int i = 0; i < tmpResult.length; i++) {
				tmpResult[i] = ss[i];
			}
			String[] GeneID = ss[3].split(";");
			String[] GeneLoc = ss[4].split(";");
			for (int i = 0; i < GeneID.length; i ++) {
				String[] tmpResultInfo = new String[2];
				tmpResultInfo[0] = GeneID[i];
				tmpResultInfo[1] = GeneLoc[i];
				String[] tmpInfo = ArrayOperate.combArray(tmpResult, tmpResultInfo, 0);
				txtOut.writefileln(tmpInfo);
			}
		}
		txtRead.close();
		txtOut.close();
	}
}
