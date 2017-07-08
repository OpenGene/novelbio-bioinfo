package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.database.service.servgeneanno.ManageSpeciesDB;
import com.novelbio.database.service.servgeneanno.ManageSpeciesTxt;
import com.novelbio.generalConf.TitleFormatNBC;
//TODO 本类中的注释功能在RefSiteSnpIndel类中已经写过类似的tostring方法，考虑将两个合并起来
/** 
 * snp annotation的类，一般用不到，因为其他已经集成了该功能<br>
 * 待annotation的必须是txt文本
 * @author zong0jie
 */
public class SnpAnnotation extends RunProcess {
	Logger logger = Logger.getLogger(SnpAnnotation.class);
	
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
				try { tmpResult = annoSnp(snpInfo); } catch (Exception e) {
					e.printStackTrace();
				}
				txtWrite.writefileln(tmpResult);
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
			String anno = annoSnp(firstLine);
			txtWrite.writefileln(anno);
		} catch (Exception e) {
			txtWrite.writefileln(firstLine + "\t" + ArrayOperate.cmbString(ArrayOperate.converList2Array(getTitleLs()), "\t"));
		}
	}
	
	/** 注释结果 */
	public String annoSnp(String input) {
		input = input.trim();
		if (input.startsWith("#")) {
			return input;
		}
//		if (input.contains("120317577")) {
//			logger.debug("stop");
//		}
		ArrayList<String> lsInfo = ArrayOperate.converArray2List(input.split("\t"));
		int refStartSite = Integer.parseInt(lsInfo.get(colRefStartSite).trim());

		RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(gffChrAbs, lsInfo.get(colChrID), refStartSite);
		SiteSnpIndelInfo siteSnpIndelInfo = refSiteSnpIndel.getAndAddAllenInfo(lsInfo.get(colRefNr), lsInfo.get(colThisNr));
		GffGeneIsoInfo gffGeneIsoInfo = refSiteSnpIndel.getGffIso();
		if (siteSnpIndelInfo == null || gffGeneIsoInfo == null) {
			GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(lsInfo.get(colChrID), refStartSite);
			if (gffCodGene == null) {
				return input;
			}
			//TODO 5000bp以内的基因都注释起来
			GffDetailGene gffDetailGene = gffCodGene.getNearestGffGene(5000);
			if (gffDetailGene == null) {
				return input;
			}
			gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		}
		lsInfo.add(gffGeneIsoInfo.getName());
		if (ManageSpecies.getInstance() instanceof ManageSpeciesDB) {
			try {
				GeneID geneID = new GeneID(gffGeneIsoInfo.getName(), gffChrAbs.getTaxID());
				if (geneID.getIDtype() == GeneID.IDTYPE_ACCID) {
					geneID = new GeneID(gffGeneIsoInfo.getParentGffGeneSame().getNameSingle(), gffChrAbs.getTaxID());
				}
				if (geneID.getIDtype() != GeneID.IDTYPE_ACCID) {
					lsInfo.add(gffGeneIsoInfo.getParentGeneName());
					lsInfo.add(geneID.getDescription());
				} else {
					lsInfo.add(gffGeneIsoInfo.getParentGeneName());
					lsInfo.add("");
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		lsInfo.add(gffGeneIsoInfo.toStringCodLocStr(new int[]{0,0}, refStartSite));
		
		//如果snp落在了intron里面，本项目就不计数了
		double prop = refSiteSnpIndel.getProp();
		if (prop >= 0) {
			lsInfo.add(refSiteSnpIndel.getProp() + "");
		} else {
			lsInfo.add("");
		}
		
		lsInfo.addAll(siteSnpIndelInfo.toStrings());
		String[] result = ArrayOperate.converList2Array(lsInfo);
		return ArrayOperate.cmbString(result, "\t");
	}
	
	public RefSiteSnpIndel getSnpSite(String chrID, int site) {
		return new RefSiteSnpIndel(gffChrAbs, chrID, site);
	}
	/** tilte和annoSnp方法中一致 */
	public static ArrayList<String> getTitleLs() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		if (ManageSpecies.getInstance() instanceof ManageSpeciesDB) {
			lsTitle.add(TitleFormatNBC.Symbol.toString());
			lsTitle.add(TitleFormatNBC.Description.toString());
		}
	
		lsTitle.add("LocationDescription");
		lsTitle.add("PropToGeneStart");
		lsTitle.addAll(SiteSnpIndelInfo.getTitle());
		return lsTitle;
	}
}
