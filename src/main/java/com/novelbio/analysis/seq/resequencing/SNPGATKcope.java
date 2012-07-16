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
import com.novelbio.analysis.seq.genomeNew.GffChrSnpIndel;
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
 * 读取几个GATK的vcf结果文件，然后获得并集snp，并标记每个snp的信息，所在基因等等
 * @author zong0jie
 */
public class SNPGATKcope {
	Logger logger = Logger.getLogger(SNPGATKcope.class);
	GffChrAbs gffChrAbs;
	int colChrID, colSnpStart, colRefsequence, colThisSequence;
	ArrayList<String[]> lsSample2VcfFiles = new ArrayList<String[]>();
	/**多个vcf文件的并集snp */
	ArrayList<MapInfoSnpIndel> lsUnionSnp = new ArrayList<MapInfoSnpIndel>();
	/** 用于多个样本的snp去冗余的，其中key表示该snp所在的起点信息，value就是该位点具体的snp情况 */
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel = new HashMap<String, MapInfoSnpIndel>();
	
	public static void main(String[] args) {
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setGffChrAbs(new GffChrAbs(9606));
		snpgatKcope.setColInfo(0, 1, 3, 4);
		snpgatKcope.addVcfFile("2A", "/media/winF/NBC/Project/Project_HXW/20120705/2A_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfFile("2B", "/media/winF/NBC/Project/Project_HXW/20120705/2B_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfFile("3A", "/media/winF/NBC/Project/Project_HXW/20120705/3A_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfFile("3B", "/media/winF/NBC/Project/Project_HXW/20120705/3B_SNPrecal_IndelFiltered.vcf");
		
		
		
		snpgatKcope.execute();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/result.test");
	}
	public static void main22(String[] args) {
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setDomainInfo("/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/snpFinalNew/AllsnpCoped.txt",
				"/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/pfam/pfamInfo.txt", "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/snpFinalNew/Allsnp_pfam.xls");
	}
	
	public SNPGATKcope() {
	}
	public void addVcfFile(String sampleName, String vcfFile) {
		lsSample2VcfFiles.add(new String[]{sampleName, vcfFile});
	}
	public void execute() {
		for (String[] sample2vcf : lsSample2VcfFiles) {
			addVcfToLsSnpIndel(sample2vcf[0], sample2vcf[1]);
		}
	}
	public void setColInfo(int colChrID, int colSnpStart, int colRefsequence, int colThisSequence) {
		this.colChrID = colChrID;
		this.colSnpStart = colSnpStart;
		this.colRefsequence = colRefsequence;
		this.colThisSequence = colThisSequence;
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 将gatk里面vcf文件中，random的chr全部删除
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
			SiteSnpIndelInfo siteSnpIndelInfo = mapInfoSnpIndel.addAllenInfo(ss[colRefsequence], ss[colThisSequence]);
			mapInfoSnpIndel.setBaseInfo(ss[7]);
			siteSnpIndelInfo.setQuality(ss[5]);
			siteSnpIndelInfo.setFiltered(ss[6]);
			mapInfoSnpIndel.setFlag(ss[8], ss[9]);
			setDepthAlt(siteSnpIndelInfo, ss[8], ss[9]);
			if (!ss[2].equals(".")) {
				siteSnpIndelInfo.setDBSnpID(ss[2]);
			}
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

	private void setDepthAlt(SiteSnpIndelInfo sampleRefReadsInfo, String flagTitle, String flagDetail) {
			//TODO 这里我删除了一个Allelic_depths_Alt的项目，考虑如何很好的添加进去
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
		for (MapInfoSnpIndel mapInfoSnpIndel : lsUnionSnp) {
			ArrayList<String[]> lsResult = mapInfoSnpIndel.toStringLsSnp(lsSample);
			for (String[] strings : lsResult) {
				txtOut.writefileln(strings);
			}
		}
	}
	/**
	 * 给定文本，和domain信息，获得具体domain的信息
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
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(0, ss[0], Integer.parseInt(ss[1]), ss[3], ss[4]);
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
