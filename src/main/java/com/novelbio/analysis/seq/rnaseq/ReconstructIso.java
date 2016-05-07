package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class ReconstructIso implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(CufflinksGTF.class);
	public static final String tmpFolder = "tmpCufflinks/";
	
	private int filterIsoLen = 200;
	private int filterIsoFPKM = 10;
	
	/** 重新计算是否使用以前的结果 */
	private boolean isUseOldResult = true;
	
	private ArrayListMultimap<String, String> mapPrefix2SamFilePaths = ArrayListMultimap.create();
	private Set<String> setPrefix= new LinkedHashSet<String>();
	
	private List<String> lsCmd = new ArrayList<>();
	
	private IntReconstructIsoUnit reconstructIsoInt;
	
	private String outPath;
	
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	public void setFilterIsoFPKM(int filterIsoFPKM) {
		this.filterIsoFPKM = filterIsoFPKM;
	}
	public void setFilterIsoLen(int filterIsoLen) {
		this.filterIsoLen = filterIsoLen;
	}
	
	/** 设定重建转录本的方法 */
	public void setReconstructIsoInt(IntReconstructIsoUnit reconstructIsoInt) {
		this.reconstructIsoInt = reconstructIsoInt;
	}
	
	/** 是否使用以前跑出来的结果，默认为ture<br>
	 * 意思就是如果以前跑出来过结果，这次就直接跳过
	 * @param isUseOldResult
	 */
	public void setIsUseOldResult(boolean isUseOldResult) {
		this.isUseOldResult = isUseOldResult;
	}
	
	/**
	 * 设置bam文件-prefix的表，这里实际上我们不使用prefix
	 * @param lsSamfiles2Prefix
	 */
	public void setLsBamFile2Prefix(ArrayList<String[]> lsSamfiles2Prefix) {
		mapPrefix2SamFilePaths.clear();
		for (String[] strings : lsSamfiles2Prefix) {
			mapPrefix2SamFilePaths.put(strings[1].trim(), strings[0]);
			setPrefix.add(strings[1].trim());
		}
	}
	
	private List<String> getSamFileSeperate(String prefix) {
		List<String> lsSamFiles = mapPrefix2SamFilePaths.get(prefix);
		List<String> lsResult = new ArrayList<String>();
		for (String filename : lsSamFiles) {
			lsResult.add(filename);
		}
		return lsResult;
	}

	/**
	 * 参数设定不能用于solid 还没加入gtf的选项，也就是默认没有gtf
	 */
	public List<String> runReconstructIso() {
		List<String> lsCufflinksResult = new ArrayList<String>();
		lsCmd.clear();
		for (String prefix : setPrefix) {
			lsCufflinksResult.addAll(runReconstructIsoByPrefix(prefix));
		}
		return lsCufflinksResult;
	}
	
	/**
	 * 返回合并好的bam文件
	 * @param prefix
	 * @return
	 */
	private List<String> runReconstructIsoByPrefix(String prefix) {
		reconstructIsoInt.setOutPath(outPath);
		List<String> lsGtf = new ArrayList<>();
		List<String> lsSamfiles = getSamFileSeperate(prefix);
		for (String bamFile : lsSamfiles) {
			String tmpGtf = runReconstructIso(bamFile);
			if (tmpGtf != null) {
				lsGtf.add(tmpGtf);
			}
		}
		return lsGtf;
	}
	
	private String runReconstructIso(String bamFile) {
		String tmpGtf = reconstructIsoInt.getOutGtfName(bamFile);
		if (!isUseOldResult || !FileOperate.isFileExistAndBigThan0(tmpGtf)) {
			reconstructIsoInt.reconstruct(bamFile);
			lsCmd.addAll(reconstructIsoInt.getCmdExeStr());
		}
		String outGtfModify = getOutFilteredGtf(bamFile, filterIsoFPKM);
		filterGtfFile(tmpGtf, outGtfModify, filterIsoFPKM, filterIsoLen);
		return outGtfModify;
	}
	
	/** 过滤后的gtf文件 */
	private String getOutFilteredGtf(String bamFile, int fpkmFilter) {
		return FileOperate.changeFileSuffix(reconstructIsoInt.getOutGtfName(bamFile), "_filterWithFPKMlessThan" + fpkmFilter, null);
	}

	/** 设定好本类后，不进行计算，直接返回输出的结果文件名 */
	public List<String> getLsGtfFileName() {
		reconstructIsoInt.setOutPath(outPath);
		List<String> lsResult = new ArrayList<>();
		for (String prefix : setPrefix) {
			List<String> lsSamfiles = getSamFileSeperate(prefix);
			for (String bamFile : lsSamfiles) {
				String subGtf = getOutFilteredGtf(bamFile, filterIsoFPKM);
				lsResult.add(subGtf);
			}
		}
		return lsResult;
	}
	
	public static void filterGtfFile(String outGtf, String outFinal, double fpkmFilter, int isoLenFilter) {
		TxtReadandWrite txtRead = new TxtReadandWrite(outGtf);
		
		/** 基因名字的正则，可以改成识别人类或者其他,这里是拟南芥，默认  NCBI的ID  */
		String transIdreg = "(?<=transcript_id \")[\\w\\-%\\:\\.]+";
		String fpkmreg = "(?<=FPKM \")[\\d\\.]+";
		String geneNamereg = "(?<=gene_id \")[\\w\\-%\\:\\.]+";
		PatternOperate patTransId = new PatternOperate(transIdreg, false);
		PatternOperate patFpkm = new PatternOperate(fpkmreg, false);
		PatternOperate patGeneName = new PatternOperate(geneNamereg, false);
		Map<String, Double> mapIso2Fpkm = new HashMap<String, Double>();
		Map<String, String> mapIso2GeneName = new HashMap<String, String>();
		
		double fpkm = 0.0;
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			String transId = patTransId.getPatFirst(content);
			String TranFpkm = patFpkm.getPatFirst(content);
			if ((TranFpkm != null)) {
				fpkm = Double.parseDouble(TranFpkm);
			}
			String geneName = patGeneName.getPatFirst(content);
			mapIso2Fpkm.put(transId, fpkm);
			if (geneName != null) {
				mapIso2GeneName.put(transId, geneName);
			}
		}
		txtRead.close();
		
		GffHashGene gffHashGene = new GffHashGene(GffType.GTF, outGtf);
		GffHashGene gffHashGeneNew = new GffHashGene();
		for (GffDetailGene gffDetailGene : gffHashGene.getGffDetailAll()) {
			GffDetailGene gffDetailGeneNew = gffDetailGene.clone();
			gffDetailGeneNew.clearIso();
			for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
				
				if (mapIso2GeneName.containsKey(iso.getName())
						||  iso.size() > 1 || (mapIso2Fpkm.containsKey(iso.getName()) && mapIso2Fpkm.get(iso.getName()) >= fpkmFilter && (iso.getLen() >= isoLenFilter))
						) {
					gffDetailGeneNew.addIsoSimple(iso);
				}

			}
			if (gffDetailGeneNew.getLsCodSplit().size() > 0) {
				gffHashGeneNew.addGffDetailGene(gffDetailGeneNew);
			}
		}
		logger.info("before filter iso with only one exon have " + getIsoHaveOneExonNum(gffHashGene));
		logger.info("after filter iso with only one exon have " + getIsoHaveOneExonNum(gffHashGeneNew));
		logger.info("before filter geneNum " + gffHashGene.getGffDetailAll().size());
		logger.info("after filter geneNum " + gffHashGeneNew.getGffDetailAll().size());
		gffHashGeneNew.writeToGTF(outFinal);
	}
	
	/** 仅含一个exon的iso的数量 */
	private static int getIsoHaveOneExonNum(GffHashGene gffHashGene) {
		int isoExon1NumNew = 0;
		for (GffDetailGene gene : gffHashGene.getGffDetailAll()) {
			for (GffGeneIsoInfo isoInfo : gene.getLsCodSplit()) {
				if (isoInfo.size() == 1) {
					isoExon1NumNew++;
				}
			}
		}
		return isoExon1NumNew;
	}

	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
	
	/**
	 * key 是展示信息
	 * value 是实际的名字
	 * @return
	 */
	public static Map<String, SoftWare> getRNAsoftInfo2Value() {
		Map<String, SoftWare> mapInfo2Value = new HashMap<>();
		mapInfo2Value.put("cufflinks", SoftWare.cufflinks);
		mapInfo2Value.put("stringtie", SoftWare.stringtie);
		return mapInfo2Value;
	}

}
