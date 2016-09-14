package com.novelbio.analysis.comparegenomics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
//import org.apache.xmlbeans.soap.SOAPArrayType;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.annotation.blast.BlastNBC;
import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;

/**
 *  指定一条序列，将该序列比对到指定物种的dna或rna上，并获得序列 
 * @author zong0jie
 * 
 */
public class BlastGetSeq implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(BlastGetSeq.class);
 
	public static void main2(String[] args) {
		String refseqFile = "/home/novelbio/文档/resultzb";
 
		try {
			FileOperate.createFolders(FileOperate.getParentPathNameWithSep(refseqFile));
			GffChrAbs gffChrAbs = new GffChrAbs();
			gffChrAbs.setGffHash(new GffHashGene(GffType.NCBI, "/hdfs:/nbCloud/public/nbcplatform/genome/species/7955/Zv10/gff/ref_GRCz10_top_level.gff3", false));
			gffChrAbs.setSeqHash(new SeqHash("/home/novelbio/文档/chr_all.fa", " "));
			GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
			if (true) {
				gffChrSeq.setGeneStructure(GeneStructure.CDS);
				gffChrSeq.setGetAAseq(true);
			} else {
				gffChrSeq.setGeneStructure(GeneStructure.ALLLENGTH);
				gffChrSeq.setGetAAseq(false);
			}
			gffChrSeq.setGetAllIso(true);
			gffChrSeq.setGetIntron(false);
			gffChrSeq.setGetSeqGenomWide();
			gffChrSeq.setOutPutFile(refseqFile);
			gffChrSeq.run();
			gffChrAbs.close();
			gffChrAbs = null;
			gffChrSeq = null;
		} catch (Exception e) {
			logger.error("生成 RefRNA序列出错");
		}
		
	}
	public static void main(String[] args) {
		String path = "/home/novelbio/公共/liufei/slc44a.fa";
//		FileOperate.createFolders(FileOperate.getPathName(path));
//		BlastGetSeq blastGetSeq = new BlastGetSeq();
//		blastGetSeq.setBlastType(BlastType.blastp);
//		blastGetSeq.setResultFile(path);
//		List<Species> lsSpecies = new ArrayList<>();
//		Species species = new Species(9606);
//		species.setVersion("GRCh38");
//		lsSpecies.add(species);
//		
//		species = new Species(10090);
//		species.setVersion("mm10_GRCm38");
//		lsSpecies.add(species);
//		
//		species = new Species(10116);
//		species.setVersion("rnor6_NCBI");
//		lsSpecies.add(species);
//		
//		species = new Species(7227);
//		species.setVersion("dmel_r6_01");
//		lsSpecies.add(species);
//		
//		species = new Species(7955);
//		species.setVersion("Zv10");
//		lsSpecies.add(species);
//		
////		species = new Species(9913);
////		species.setVersion("Btau_4.6.1_NCBI");
////		lsSpecies.add(species);
//		
//		blastGetSeq.setLsSpeciesBlastTo(lsSpecies);
//		SeqFastaHash seqHash = new SeqFastaHash("/home/novelbio/公共/liufei/query.fa");
//		List<SeqFasta> ls = seqHash.getSeqFastaAll();
//		blastGetSeq.addQueryFasta(ls);
//		seqHash.close();
//		String seqFasta = blastGetSeq.blastAndGetSeq();
		AlignmentMuscle alignmentMuscle = new AlignmentMuscle();
		alignmentMuscle.setInputFasta(path);
		alignmentMuscle.setOutputFasta(FileOperate.changeFileSuffix(path, "_alignment", "fa"));
		alignmentMuscle.runAlignment();
	}
	
	
	/** 输入的序列
	 * 用map 的原因是为了去除重复序列，key为小写
	 */
	Map<String, SeqFasta> mapSeq2Fasta = new HashMap<>();
	
	/** 需要比对到的物种 */
	List<Species> lsSpeciesBlastTo;
	/** 是否提取所有转录本做blast，默认只提取最长转录本 */
	boolean isAllIso = true;
	
	double evalue = 1e-5;
	/** 最短长度比例，比对结果小于该长度比例就不认为是同源的基因 */
	double lengthMinProp = 0.2;
	/** 最短长度Bp，比对结果小于该长度(Bp)就不认为是同源的基因 */
	int lengthMinBp = 40;
	boolean bothCondition = false;
	
	BlastType blastType = BlastType.blastp;
	String resultFile;
	/** 输出文件是protein还是nr */
	boolean isGetProtein = true;
	
	List<String> lsCmd = new ArrayList<>();
	
	public void addQueryFasta(SeqFasta seqFasta) {
		this.mapSeq2Fasta.put(seqFasta.toString().toLowerCase(), seqFasta);
	}
	
	public void addQueryFasta(List<SeqFasta> lsSeqFasta) {
		for (SeqFasta seqFasta : lsSeqFasta) {
			this.mapSeq2Fasta.put(seqFasta.toString().toLowerCase(), seqFasta);
		}
	}
	
	public void setLsSpeciesBlastTo(List<Species> lsSpeciesBlastTo) {
		this.lsSpeciesBlastTo = lsSpeciesBlastTo;
	}
	/** 输出文件 */
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	/** 默认 1e-5 */
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	/** 输出结果是protein还是dna，默认protein */
	public void setOutputType(boolean isGetProtein) {
		this.isGetProtein = isGetProtein;
	}
	/** 最短长度Bp，比对结果小于该长度(Bp)就不认为是同源的基因
	 * 
	 * @param lengthMinBp 最短长度Bp，比对结果小于该长度(Bp)就不认为是同源的基因 默认50
	 * @param lengthMinProp 最短长度比例，比对结果小于该长度比例就不认为是同源的基因 默认0.3
	 * @param both 默认false <br> 
	 * true：两个条件都满足<br> 
	 * false：两个条件满足一个即可
	 */
	public void setLengthMin(int lengthMinBp, double lengthMinProp, boolean bothCondition) {
		this.lengthMinBp = lengthMinBp;
		this.lengthMinProp = lengthMinProp;
		this.bothCondition = bothCondition;
	}
	/** 务必在序列输入后设定 */
	public void setBlastType(BlastType blastType) {
		this.blastType = blastType;
	}
	
	/** 比对到指定物种上并获得比对到的序列
	 * @return 返回整理成fasta格式的序列文件
	 */
	public String blastAndGetSeq() {
		//物种与找到的基因数量
		String outNum = resultFile + "blastInfo.txt";
		TxtReadandWrite txtWriteNum = new TxtReadandWrite(outNum, true);
		//具体的序列
		String outSeq = resultFile;
		if (resultFile.endsWith("/") || resultFile.endsWith("\\")) {
			outSeq = outSeq + "blastSpeciesSeq.fa";
		}
		TxtReadandWrite txtWriteSeq = new TxtReadandWrite(outSeq, true);
		txtWriteNum.writefileln("Species\tNumber");
		for (Species species : lsSpeciesBlastTo) {
			Map<String, SeqFasta> mapName2Seqfasta = new HashMap<>();
			if (!checkSpecies(species)) {
				logger.info("no species: " + species.getNameLatin());
				continue;
			}
			String seqBlastTo = getBlastToSeq(species);
			
			GffChrAbs gffChrAbs = new GffChrAbs(species);
			for (SeqFasta seqFasta : mapSeq2Fasta.values()) {
				Map<String, SeqFasta> mapName2SeqfastaOnseq = getBlast2Species(seqFasta, seqBlastTo, species, gffChrAbs);
				mapName2Seqfasta.putAll(mapName2SeqfastaOnseq);
			}
			gffChrAbs.close();
			txtWriteNum.writefileln(species.getNameLatin() + "\t" + mapName2Seqfasta.size());
			writeToFile(species, mapName2Seqfasta.values(), txtWriteSeq);
			logger.info("finish get species: " + species.getNameLatin());
		}
		
		txtWriteNum.close();
		txtWriteSeq.close();
		return outSeq;
	}
	
	/**
	 * 将seqFasta比对到seqBlastTo上去
	 * @param seqFasta
	 * @param seqBlastTo
	 * @return
	 */
	private Map<String, SeqFasta> getBlast2Species(SeqFasta seqFasta, String seqBlastTo, Species species, GffChrAbs gffChrAbs) {
		BlastNBC blastNBC = new BlastNBC();
		blastNBC.setBlastType(blastType);
		blastNBC.setQueryFasta(seqFasta);
		blastNBC.setSubjectSeq(seqBlastTo);
		if (seqFasta.Length() < 50) {
			blastNBC.setShortQuerySeq(true);
		}
		blastNBC.setEvalue(evalue);
		blastNBC.setResultSeqNum(1000);
		blastNBC.setResultType(BlastNBC.ResultType_Simple);
		String resultBlast = FileOperate.getPathName(resultFile) + seqFasta.getSeqName().trim().split(" ")[0] + "_"+ blastType + "_"  + species.getNameLatin().replace(" ", "_");
		blastNBC.setResultFile(resultBlast);
		blastNBC.blast();
		lsCmd.addAll(blastNBC.getCmdExeStr());
		List<SeqFasta> lsSeqFastas = getSeq(seqFasta, blastNBC, species, gffChrAbs);
		Map<String, SeqFasta> mapName2SeqFasta = new HashMap<>();
		for (SeqFasta seqFasta2 : lsSeqFastas) {
			mapName2SeqFasta.put(seqFasta2.getSeqName(), seqFasta2);
		}
		return mapName2SeqFasta;
	}
	
	
	private boolean checkSpecies(Species species) {
		if (species == null || species.getTaxID() == 0) {
			return false;
		}
		String fileName = getBlastToSeq(species);
		if (!FileOperate.isFileExistAndBigThanSize(fileName, 0)) {
			return false;
		}
		return true;
	}
	
	private String getBlastToSeq(Species species) {
		if (blastType == BlastType.blastn || blastType == BlastType.tblastn || blastType == BlastType.tblastx) {
			return species.getRefseqFile(isAllIso);
		} else {
			return species.getRefseqProFile(isAllIso);
		}
	}
	
	/** 根据blast以及相应的参数，返回提取到的序列 */
	private List<SeqFasta> getSeq(SeqFasta seqFasta, BlastNBC blastNBC, Species species, GffChrAbs gffChrAbs) {
		List<BlastInfo> lsBlastInfos = BlastInfo.readBlastFile(blastNBC.getResultFile());
		if (lsBlastInfos.size() == 0) {
			return new ArrayList<>();
		}
		int seqQueryLen = seqFasta.Length();
		lsBlastInfos = BlastInfo.removeDuplicate(lsBlastInfos);
		List<String> lsGeneName = new ArrayList<>();
		for (BlastInfo blastInfo : lsBlastInfos) {
			//根据阈值做筛选，将名字保存到lsGeneName
			if (blastInfo.getEvalue() <= evalue && 
					(   ( bothCondition && blastInfo.getAlignLen() >= lengthMinBp && blastInfo.getAlignLen()/seqQueryLen >= lengthMinProp
						)
					|| 
						( !bothCondition && (blastInfo.getAlignLen() >= lengthMinBp || blastInfo.getAlignLen()/seqQueryLen >= lengthMinProp)
						)
					)
				) {
				lsGeneName.add(blastInfo.getSubjectID());
			}
		}
		if (lsGeneName.size() == 0) {
			return new ArrayList<>();
		}
		GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
		if (isGetProtein) {
			gffChrSeq.setGeneStructure(GeneStructure.CDS);
		} else {
			gffChrSeq.setGeneStructure(GeneStructure.ALLLENGTH);
		}
		gffChrSeq.setGetAllIso(false);
		gffChrSeq.setGetIntron(false);
		gffChrSeq.setGetSeqIsoRemoveSamGene(lsGeneName);
		gffChrSeq.setIsSaveToFile(false);
		gffChrSeq.run();
		List<SeqFasta> lsSeqfasta = gffChrSeq.getLsResult();
		return lsSeqfasta;
	}
		
	private void writeToFile(Species species, Collection<SeqFasta> lsSeqFastas, TxtReadandWrite txtWrite) {
		for (SeqFasta seqFasta : lsSeqFastas) {
			GeneID geneID = new GeneID(seqFasta.getSeqName(), species.getTaxID());
			String seqfastaNameNew = seqFasta.getSeqName();
			if (!geneID.getSymbol().toLowerCase().equals(seqfastaNameNew.toLowerCase())) {
				seqfastaNameNew = geneID.getSymbol().replace(" ", "_") + "_" + seqfastaNameNew;
			}
			seqFasta.setName(species.getCommonName().replace(" ", "_") + "_" + seqfastaNameNew);
			String result = null;
			if (isGetProtein) {
				result = seqFasta.toStringAAfasta();
			} else {
				result = seqFasta.toStringNRfasta();
			}
			txtWrite.writefileln(result);
		}
	}
	
	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
	
}
