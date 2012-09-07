package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneID;

/** 
 * snp annotation���࣬һ���ò�������Ϊ�����Ѿ������˸ù���<br>
 * ��annotation�ı�����txt�ı�
 * @author zong0jie
 */
public class SnpAnnotation extends RunProcess<SnpFilterDetailInfo>{
	GffChrAbs gffChrAbs;
	
	int colChrID;
	int colRefStartSite;
	int colRefNr;
	int colThisNr;
	
	long readLines;
	long readByte;
	
	ArrayList<String[]> lsTxtFile = new ArrayList<String[]>();
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void addTxtSnpFile(String txtFile, String txtOut) {
		lsTxtFile.add(new String[]{txtFile, txtOut});
	}
	public void setCol(int colChrID, int colRefStartSite, int colRefNr, int colThisNr) {
		this.colChrID = colChrID - 1;
		this.colRefStartSite = colRefStartSite - 1;
		this.colRefNr = colRefNr - 1;
		this.colThisNr = colThisNr - 1;
	}
	/**������KΪ��λ�Ĺ����ļ����ܺͣ�gz���ļ��ͻ�ӱ�����
	 * @return
	 */
	public double getFileSizeEvaluateK() {
		ArrayList<String> lsFileName = new ArrayList<String>();
		for (String[] sample2PileupFile : lsTxtFile) {
			lsFileName.add( sample2PileupFile[0]);
		} 
		return FileOperate.getFileSizeEvaluateK(lsFileName);
	}
	/** ��� */
	public void clearSnpFile() {
		lsTxtFile.clear();
		readLines = 0;
		readByte = 0;
	}
	@Override
	protected void running() {
		annotation();
	}
	
	private void annotation() {
		int rows = 0;
		for (String[] txtfile : lsTxtFile) {
			SnpFilterDetailInfo snpFilterDetailInfo = new SnpFilterDetailInfo();
			snpFilterDetailInfo.allLines = readLines;
			snpFilterDetailInfo.allByte = readByte;
			snpFilterDetailInfo.showMessage = "reading file " + txtfile[0];
			setRunInfo(snpFilterDetailInfo);
			if (flagStop)
				break;
			
			TxtReadandWrite txtRead = new TxtReadandWrite(txtfile[0], false);
			TxtReadandWrite txtWrite = new TxtReadandWrite(txtfile[1], true);
			
			for (String snpInfo : txtRead.readlines()) {
				//////
				suspendCheck();
				if (flagStop)
					break;
				if (rows % 100 == 0) {
					snpFilterDetailInfo= new SnpFilterDetailInfo();
					snpFilterDetailInfo.allLines = readLines;
					snpFilterDetailInfo.allByte = readByte;
					setRunInfo(snpFilterDetailInfo);
				}
				//////
				String tmpResult = snpInfo;
				try { tmpResult = annoSnp(snpInfo); } catch (Exception e) {}
				txtWrite.writefileln(tmpResult);
				readLines ++;
				readByte = readByte + snpInfo.getBytes().length;
			}
			txtRead.close();
			txtWrite.close();
			

		}
	}
	/** ע�ͽ�� */
	private String annoSnp(String input) {
		input = input.trim();
		if (input.startsWith("#")) {
			return input;
		}
		ArrayList<String> lsInfo = ArrayOperate.converArray2List(input.split("\t"));
		int refStartSite = Integer.parseInt(lsInfo.get(colRefStartSite));	
		MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, lsInfo.get(colChrID), refStartSite);
		SiteSnpIndelInfo siteSnpIndelInfo = mapInfoSnpIndel.addAllenInfo(lsInfo.get(colRefNr), lsInfo.get(colThisNr));
		GffGeneIsoInfo gffGeneIsoInfo = mapInfoSnpIndel.getGffIso();
		GeneID geneID = gffGeneIsoInfo.getGeneID();
		lsInfo.add(geneID.getSymbol());
		lsInfo.add(geneID.getDescription());
		lsInfo.add(mapInfoSnpIndel.getProp() + "");
		lsInfo.add(siteSnpIndelInfo.getRefAAnr().toString());
		lsInfo.add(siteSnpIndelInfo.getRefAAnr().toStringAA3());
		lsInfo.add(siteSnpIndelInfo.getThisAAnr().toString());
		lsInfo.add(siteSnpIndelInfo.getThisAAnr().toStringAA3());
		lsInfo.add(siteSnpIndelInfo.getSplitTypeEffected());
		lsInfo.add(siteSnpIndelInfo.getAAchamicalConvert());
		String[] result = ArrayOperate.converList2Array(lsInfo);
		return ArrayOperate.cmbString(result, "\t");
	}
}