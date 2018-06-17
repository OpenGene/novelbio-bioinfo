package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffoperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.snphgvs.SnpAnnoFactory;
import com.novelbio.analysis.seq.snphgvs.SnpInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.database.service.servgeneanno.ManageSpeciesDB;
import com.novelbio.database.service.servgeneanno.ManageSpeciesTxt;
import com.novelbio.generalconf.TitleFormatNBC;
//TODO 本类中的注释功能在RefSiteSnpIndel类中已经写过类似的tostring方法，考虑将两个合并起来
/** 
 * snp annotation的类，一般用不到，因为其他已经集成了该功能<br>
 * 待annotation的必须是txt文本
 * @author zong0jie
 */
public class SnpAnnotation extends RunProcess {
	Logger logger = Logger.getLogger(SnpAnnotation.class);
	
	SnpAnnoFactory snpAnnoFactory = new SnpAnnoFactory();
	int colChrID;
	int colRefStartSite;
	int colRefNr;
	int colThisNr;
	
	long readLines;
	long readByte;
	
	ArrayList<String[]> lsTxtFile = new ArrayList<String[]>();
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		snpAnnoFactory.setGffChrAbs(gffChrAbs);
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
	/**返回以K为单位的估计文件的总和，gz的文件就会加倍估计
	 * @return
	 */
	public double getFileSizeEvaluateK() {
		ArrayList<String> lsFileName = new ArrayList<String>();
		for (String[] sample2PileupFile : lsTxtFile) {
			lsFileName.add( sample2PileupFile[0]);
		} 
		return FileOperate.getFileSizeEvaluateK(lsFileName);
	}
	/** 清空 */
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
			setTitle(txtRead, txtWrite);
			for (String snpInfo : txtRead.readlines(2)) {
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
				try {
					List<String> lsResult = annoSnp(snpInfo);
					for (String anno : lsResult) {
						txtWrite.writefileln(anno);
					}
				} catch (Exception e) {
					e.printStackTrace();
					txtWrite.writefileln(tmpResult);
				}
				readLines ++;
				readByte = readByte + snpInfo.getBytes().length;
			}
			txtRead.close();
			txtWrite.close();
		}
	}
	
	/** 设定输出文件的title，如果没有title则将第一行进行注释 */
	private void setTitle(TxtReadandWrite txtRead, TxtReadandWrite txtWrite) {
		String firstLine = txtRead.readFirstLine();
		try {
			List<String> lsAnno = annoSnp(firstLine);
			for (String anno : lsAnno) {
				txtWrite.writefileln(anno);
			}
		} catch (Exception e) {
			txtWrite.writefileln(firstLine + "\t" + ArrayOperate.cmbString(ArrayOperate.converList2Array(getTitleLs()), "\t"));
		}
	}
	
	/** 注释结果 */
	public List<String> annoSnp(String input) {
		input = input.trim();
		if (input.startsWith("#")) {
			return Lists.newArrayList(input);
		}
//		if (input.contains("173469559")) {
//			logger.info("stop");
//		}
		ArrayList<String> lsInfo = ArrayOperate.converArray2List(input.split("\t"));
		int refStartSite = Integer.parseInt(lsInfo.get(colRefStartSite).trim());
//		if (refStartSite==27529644) {
//			logger.info("stop");
//		}
		SnpInfo snpInfo = snpAnnoFactory.generateSnpInfo(lsInfo.get(colChrID), refStartSite, lsInfo.get(colRefNr), lsInfo.get(colThisNr));
		List<List<String>> lsLsAnno = snpAnnoFactory.getLsAnnotation(snpInfo);
		List<String> lsResult = new ArrayList<>();
		if (lsLsAnno.isEmpty()) {
			for (int i = 0; i < 5; i++) {
				lsInfo.add("");
			}
			String result = ArrayOperate.cmbString(lsInfo, "\t");
			lsResult.add(result);
			return lsResult;
		}
		for (List<String> lsAnno : lsLsAnno) {
			List<String> lsResultUnit = new ArrayList<>();
			lsResultUnit.addAll(lsInfo);
			lsResultUnit.addAll(lsAnno);
			String result = ArrayOperate.cmbString(lsResultUnit, "\t");
			lsResult.add(result);
		}
		return lsResult;
	}
	
	/** tilte和annoSnp方法中一致 */
	public static List<String> getTitleLs() {
		return SnpAnnoFactory.getLsTitle();
	}
}
