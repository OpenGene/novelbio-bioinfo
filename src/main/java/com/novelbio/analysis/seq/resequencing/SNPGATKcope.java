package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.util.SSCellRange;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteSnpIndelInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioConst;
/**
 * ��ȡ����GATK��vcf����ļ���Ȼ���ò���snp�������ÿ��snp����Ϣ�����ڻ���ȵ�
 * @author zong0jie
 */
public class SNPGATKcope {
	Logger logger = Logger.getLogger(SNPGATKcope.class);
	GffChrAbs gffChrAbs;
	
	int colChrID = -1, colSnpStart = -1, colRefsequence = -1, colThisSequence = -1;
	int colBaseInfo = -1, colQuality = -1, colFiltered = -1, colFlagTitle = -1, colFlagDetail = -1;
	int colSnpDBID = -1;
	
	ArrayList<String[]> lsSample2VcfFiles = new ArrayList<String[]>();
	/** 0��sampleName<br>
	 * 1��SampleFile
	 */
	ArrayList<String[]> lsSample2SamPileupFile = new ArrayList<String[]>();
	/**���vcf�ļ��Ĳ���snp */
	ArrayList<MapInfoSnpIndel> lsUnionSnp = new ArrayList<MapInfoSnpIndel>();
	/** ���ڶ��������snpȥ����ģ�����key��ʾ��snp���ڵ������Ϣ��value���Ǹ�λ������snp��� */
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel = new HashMap<String, MapInfoSnpIndel>();
	
	public static void main(String[] args) {
		String parentPath = "/media/winF/NBC/Project/Project_HXW/20120705/";
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setGffChrAbs(new GffChrAbs(9606));
		snpgatKcope.setColInfo(1, 2, 4, 5);
		snpgatKcope.setColAttr(8, 6, 7, 9, 10);
		snpgatKcope.setColDBsnp(3);
		snpgatKcope.addVcfFile("2A", parentPath + "2A_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfFile("2B", parentPath + "2B_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfFile("3A", parentPath + "3A_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfFile("3B", parentPath + "3B_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addSampileupFile("2A", parentPath + "2A_detailmpileup.txt");
		snpgatKcope.addSampileupFile("2B", parentPath + "2B_detailmpileup.txt");
		snpgatKcope.addSampileupFile("3A", parentPath + "3A_detailmpileup.txt");
		snpgatKcope.addSampileupFile("3B", parentPath + "3B_detailmpileup.txt");
		
		snpgatKcope.execute();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/result_withoutSampileup.xls");
	}
	
	public SNPGATKcope() {
		colChrID = 0; colSnpStart = 1; colRefsequence = 3; colThisSequence = 4;
	}
	public void addVcfFile(String sampleName, String vcfFile) {
		lsSample2VcfFiles.add(new String[]{sampleName, vcfFile});
	}
	public void addSampileupFile(String sampleName, String sampileupFile) {
		lsSample2SamPileupFile.add(new String[]{sampleName, sampileupFile});
	}
	/**
	 * @param colChrID vcf��1
	 * @param colSnpStart vcf��2
	 * @param colRefsequence vcf��4
	 * @param colThisSequence vcf��5
	 */
	public void setColInfo(int colChrID, int colSnpStart, int colRefsequence, int colThisSequence) {
		this.colChrID = colChrID - 1;
		this.colSnpStart = colSnpStart - 1;
		this.colRefsequence = colRefsequence - 1;
		this.colThisSequence = colThisSequence - 1;
	}
	/**
	 * @param colBaseInfo vcf��8
	 * @param colQuality vcf��6
	 * @param colFiltered vcf��7
	 * @param colFlagTitle vcf��9
	 * @param colFlagDetail vcf��10
	 */
	public void setColAttr(int colBaseInfo, int colQuality, int colFiltered, int colFlagTitle, int colFlagDetail) {
		this.colBaseInfo = colBaseInfo - 1;
		this.colQuality = colQuality - 1;
		this.colFiltered = colFiltered - 1;
		this.colFlagTitle = colFlagTitle - 1;
		this.colFlagDetail = colFlagDetail - 1;
	}
	/**
	 * @param colDBsnp vcf��3
	 */
	public void setColDBsnp(int colDBsnp) {
		this.colSnpDBID = colDBsnp - 1;
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void execute() {
		for (String[] sample2vcf : lsSample2VcfFiles) {
			addVcfToLsSnpIndel(sample2vcf[0], sample2vcf[1]);
		}
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapInfoSnpIndel = MapInfoSnpIndel.sort_MapChrID2InfoSnpIndel(lsUnionSnp);
		for (String[] sample2PileUp : lsSample2SamPileupFile) {
			MapInfoSnpIndel.getSiteInfo(sample2PileUp[0], mapInfoSnpIndel, sample2PileUp[1], gffChrAbs);
		}
	}
	/**
	 * ��gatk����vcf�ļ��е�snp��Ϣ����mapSiteInfo2MapInfoSnpIndel��
	 */
	private void addVcfToLsSnpIndel(String sampleName, String vcfFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(vcfFile, false);
		for (String string : txtRead.readlines()) {
			if (string.startsWith("#")) continue;
			String[] ss = string.split("\t");
			
			int snpStart;
			try { snpStart = Integer.parseInt(ss[colSnpStart]); } catch (Exception e) { continue; }
			
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs,  ss[colChrID], snpStart);
			mapInfoSnpIndel.setSampleName(sampleName);
			setMapInfoSnpIndel(mapInfoSnpIndel, ss);
			
			String key = mapInfoSnpIndel.getRefID() + SepSign.SEP_ID + mapInfoSnpIndel.getRefSnpIndelStart();
			if (mapSiteInfo2MapInfoSnpIndel.containsKey(key)) {
				MapInfoSnpIndel maInfoSnpIndelExist = mapSiteInfo2MapInfoSnpIndel.get(mapInfoSnpIndel.getRefID() + SepSign.SEP_ID + mapInfoSnpIndel.getRefSnpIndelStart());
				maInfoSnpIndelExist.addAllenInfo(mapInfoSnpIndel);
				continue;
			}
			else {
				mapSiteInfo2MapInfoSnpIndel.put(key, mapInfoSnpIndel);
				lsUnionSnp.add(mapInfoSnpIndel);
			}
		}
	}
	/**
	 * �����趨������Ϣ�����mapinfosnpindel��Ϣ
	 */
	private void setMapInfoSnpIndel(MapInfoSnpIndel mapInfoSnpIndel, String[] inputLines) {
		SiteSnpIndelInfo siteSnpIndelInfo = mapInfoSnpIndel.addAllenInfo(inputLines[colRefsequence], inputLines[colThisSequence]);
		if (colBaseInfo >= 0)
			mapInfoSnpIndel.setBaseInfo(inputLines[colBaseInfo]);
		if (colQuality >= 0)
			siteSnpIndelInfo.setQuality(inputLines[colQuality]);
		if (colFiltered >= 0)
			siteSnpIndelInfo.setFiltered(inputLines[colFiltered]);
		if (colFlagTitle >= 0 && colFlagDetail >= 0) {
			mapInfoSnpIndel.setFlag(inputLines[colFlagTitle], inputLines[colFlagDetail]);
			setDepthAlt(siteSnpIndelInfo, inputLines[colFlagTitle], inputLines[colFlagDetail]);
		}
		if (colSnpDBID>=0) {
			if (!inputLines[colSnpDBID].equals(".")) {
				siteSnpIndelInfo.setDBSnpID(inputLines[colSnpDBID]);
			}
		}
	}
	/** �趨vcf�е�reads depth�Ǹ��У���Ҫ���趨��vcf�ж�ȡ��reads depth��Ϣ */
	private void setDepthAlt(SiteSnpIndelInfo sampleRefReadsInfo, String flagTitle, String flagDetail) {
		//TODO ������ɾ����һ��Allelic_depths_Alt����Ŀ��������κܺõ���ӽ�ȥ
		String[] ssFlag = flagTitle.split(":");
		String[] ssValue = flagDetail.split(":");
		for (int i = 0; i < ssFlag.length; i++) {
			if (ssFlag[i].equals("AD")) {
				String[] info = ssValue[i].split(",");
				sampleRefReadsInfo.setThisReadsNum(Integer.parseInt(info[1]));
			}
		}
	}
	
	public void writeToFile(String txtFile) {
		ArrayList<String> lsSample = new ArrayList<String>();
		for (String[] strings : lsSample2VcfFiles) {
			lsSample.add(strings[0]);
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(txtFile, true);
		txtOut.writefileln(MapInfoSnpIndel.getTitleFromSampleName(lsSample));
		for (MapInfoSnpIndel mapInfoSnpIndel : lsUnionSnp) {
			ArrayList<String[]> lsResult = mapInfoSnpIndel.toStringLsSnp(lsSample, true);
			for (String[] strings : lsResult) {
				txtOut.writefileln(strings);
			}
		}
		txtOut.close();
	}
	
	public void filterSnp() {
		for (MapInfoSnpIndel mapInfoSnpIndel : lsUnionSnp) {
			
		}
		//TODO ���˲�ƽ���snp
	}
	/**
	 * �����ı�����domain��Ϣ����þ���domain����Ϣ
	 * @param txtExcelSNP
	 * @param domainFile
	 * @param outFile
	 */
	public void setDomainInfo(String txtExcelSNP, String domainFile, String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		DomainPfam.readDomain(domainFile);
		ArrayList<String[]> lsSnp = ExcelTxtRead.readLsExcelTxt(txtExcelSNP, 1);
		for (int i = 1; i < lsSnp.size(); i++) {
			String[] ss = lsSnp.get(i);
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, chrID, refSnpIndelStart)//(0, ss[0], Integer.parseInt(ss[1]), ss[3], ss[4]);
			GffCodGene gffcod = gffHashGene.searchLocation(mapInfoSnpIndel.getRefID(), mapInfoSnpIndel.getRefSnpIndelStart());
			String tmp = "";
			if (gffcod.isInsideLoc()) {
				GffDetailGene gffDetailGene = gffcod.getGffDetailThis();
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					DomainPfam domainPfam = DomainPfam.getDomainPfam(gffGeneIsoInfo.getName());
					if (domainPfam == null) {
						continue;
					}
					domainPfam.setAALoc(gffGeneIsoInfo.getCod2ATGmRNA(gffcod.getCoord())/3);
					tmp = domainPfam.toString();
					break;
				}
				
			}
			String result = ArrayOperate.cmbString(ss, "\t");
			result = result + "\t" + tmp;
			txtOut.writefileln(result);
		}
		txtOut.close();
	}
	
}
