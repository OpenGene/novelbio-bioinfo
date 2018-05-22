package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.gffoperate.MiRNAList;
import com.novelbio.analysis.seq.genome.gffoperate.MirMature;
import com.novelbio.analysis.seq.genome.gffoperate.MirPre;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.species.Species;
/**
 * 读取miRNA.dat的信息，构建listabs表，方便给定mirID和loc，从而查找到底是5p还是3p
 * @author zong0jie
 *
 */
public class ListMiRNAdat extends MiRNAList {
	private static final Logger logger = Logger.getLogger(ListMiRNAdat.class);
	/** 物种的拉丁名 */
	String speciesLatinName;


	/**
	 * 为miRNA.dat中的物种名
	 * 设定物种，默认为人类：HSA
	 * 具体要检查RNA.data文件
	 * @param species 输入的物种名是<b>两个单词的拉丁名</b>，也就是说不考虑亚种，譬如不考虑水道indica和japanica的区别
	 */
	public void setSpecies(Species species) {
		speciesLatinName = species.getNameLatin_2Word();
	}
	public void setSpeciesLatinName(String speciesLatinName) {
		this.speciesLatinName = speciesLatinName;
	}
	/**
	 * 读取miRNA.data文件，同时读取和它一起的整理好的taxID2species文件
	 */
	protected void ReadGffarrayExcep(String rnadataFile) {
		ReadGffarrayExcepRNADat(rnadataFile);
	}
	
	/**
	 * <b>提取序列，用于SpeciesFile里面</b><p>
	 * 
	 * 如果设定了lsMiRNAName，则直接写入rnaMatureOut这个文本
	 * 从miRBase的RNAdata文件中提取miRNA序列
	 * @param setMirnaToBeExtract 需要提取的miRNA的名字，null或者为空表示提取全部miRNA
	 * @param speciesName 物种的英文全名，无所谓大小写
	 * @param rnaDataFile
	 * @param rnaHairpinOut
	 * @param rnaMatureOut
	 */
	public void extractMiRNASeqFromRNAdata(Set<String> setMirnaToBeExtract, String speciesName, 
			String rnaDataFile, String rnaHairpinOut, String rnaMatureOut) {
		ExtractMirSeq extractMirSeq = new ExtractMirSeq(setMirnaToBeExtract, speciesName, rnaDataFile, rnaHairpinOut, rnaMatureOut);
		extractMirSeq.writeFile();
	}

	/**
	 * 读取RNA.dat，获得每个小RNA的序列信息
	 * ID   hsa-mir-1539      standard; RNA; HSA; 50 BP.
XX
AC   MI0007260;
XX
DE   Homo sapiens miR-1539 stem-loop
XX
RN   [1]
RX   PUBMED; 18524951.
RA   Azuma-Mukai A, Oguri H, Mituyama T, Qian ZR, Asai K, Siomi H, Siomi MC;
RT   "Characterization of endogenous human Argonautes and their miRNA partners
RT   in RNA silencing";
RL   Proc Natl Acad Sci U S A. 105:7964-7969(2008).
XX
DR   HGNC; 35383; MIR1539.
DR   ENTREZGENE; 100302257; MIR1539.
XX
FH   Key             Location/Qualifiers
FH
FT   miRNA           30..50
FT                   /accession="MIMAT0007401"
FT                   /product="hsa-miR-1539"
FT                   /evidence=experimental
FT                   /experiment="ChIP-seq [1]"
XX
SQ   Sequence 50 BP; 7 A; 18 C; 17 G; 0 T; 8 other;
     ggcucugcgg ccugcaggua gcgcgaaagu ccugcgcguc ccagaugccc                   50
//
	 */
	protected void ReadGffarrayExcepRNADat(String rnadataFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(rnadataFile, false);
		List<String> lsMirnaBlock = new ArrayList<>();
		for (String string : txtRead.readlines()) {
			if (string.startsWith("//")) {				
				copeMirBlock(lsMirnaBlock, speciesLatinName);
				lsMirnaBlock = new ArrayList<>();
				continue;
			}
			lsMirnaBlock.add(string);
		}
		copeMirBlock(lsMirnaBlock, speciesLatinName);
		txtRead.close();
	}
	
	private void copeMirBlock(List<String> lsMirnaBlock, String speciesName) {
		if (!isFindSpecies(lsMirnaBlock, speciesName)) {
			return;
		}
		MirPre mirPre = new MirPre();
		String[] sepInfo = lsMirnaBlock.get(0).split(" +");
		mirPre.setName(sepInfo[1]);
		mirPre.setCis5to3(true);
		if (isGetSeq) {
			mirPre.setMirPreSeq(getHairpinSeq(lsMirnaBlock));
		}
		
		//装入chrHash
		getMapChrID2LsGff().put(mirPre.getName().toLowerCase(), mirPre);
		List<MirMature> lsMiRNABin = getLsMatureMirnaLocation(lsMirnaBlock);
		for (MirMature miRNAbin : lsMiRNABin) {
			miRNAbin.setParentListAbs(mirPre);
			mirPre.add(miRNAbin);
		}
	}
	
	protected static String getHairpinSeq(List<String> lsMirBlock) {
		String finalSeq = "";
		boolean seqFlag = false;
		for (String string : lsMirBlock) {
			if (string.startsWith("SQ")) {
				seqFlag = true;
				continue;
			}
			if (seqFlag) {
				String[] ssA = string.trim().split(" +");
				finalSeq = finalSeq + string.replace(ssA[ssA.length - 1], "").replace(" ", "");
			}
		}
		return finalSeq;
	}
	
	protected static ArrayList<MirMature> getLsMatureMirnaLocation(List<String> lsMirBlock) {
		ArrayList<MirMature> lsResult = new ArrayList<>();
		MirMature mirMature = null;
		for (String string : lsMirBlock) {
			String[] sepInfo = string.split(" +");
			if (sepInfo[0].equals("FT")) {
				if (sepInfo[1].equals("miRNA")) {
					mirMature = new MirMature();
					mirMature.setCis5to3(true);
					String[] loc = sepInfo[2].split("\\.\\.");
					mirMature.setStartAbs(Integer.parseInt(loc[0]));
					mirMature.setEndAbs(Integer.parseInt(loc[1]));
					lsResult.add(mirMature);
				}
				
				if (sepInfo[1].contains("accession")) {
					String accID = sepInfo[1].split("=")[1];
					accID = accID.replace("\"", "");
					mirMature.setMirAccID(accID);
				} else if (sepInfo[1].contains("product")) {
					String accID = sepInfo[1].split("=")[1];
					accID = accID.replace("\"", "");
					mirMature.addItemName(accID);
				} else if (sepInfo[1].contains("evidence")) {
					String evidence = sepInfo[1].split("=")[1];
					mirMature.setEvidence(evidence);
				}
			}
		}
		return lsResult;
	}
	
	protected static boolean isFindSpecies(List<String> lsMirBlock, String speciesName) {
		if (lsMirBlock.size() == 0 || !lsMirBlock.get(0).startsWith("ID")) {
			return false;
		}
		boolean findSpecies = false;
		for (String string : lsMirBlock) {
			string = string.toLowerCase();
			if (string.startsWith("de")) {
				if (string.contains(speciesName.toLowerCase())) {
					findSpecies = true;
					break;
				}
			}
		}
		return findSpecies;
	}
	
	/**
	 * 如果没有找到，则返回null
	 * @param mirName mir的名字
	 * @param start 具体的
	 * @param end
	 * @return
	 */
	public String searchMirName(String mirName, int start, int end) {
		MirMature element = searchElement(mirName, start, end);
		if (element == null) {
			logger.debug("cannot find miRNA on：" + mirName + " " + start + " " + end);

			return null;
		}
		return element.getNameSingle();
	}
	
	public static boolean isContainMiRNA(String speciesName, String rnaDataFile) {
		ExtractMirSeq extractMirSeq = new ExtractMirSeq(null, speciesName, rnaDataFile, null, null);
		return extractMirSeq.isContainMiRNA(speciesName);
	}

}

class ExtractMirSeq {
	Set<String> setMirnaToBeExtract;
	String speciesName;
	
	String rnaDataFile;
	String rnaHairpinOut;
	String rnaMatureOut;
	
	TxtReadandWrite txtRead;
	TxtReadandWrite txtHairpin;
	TxtReadandWrite txtMature;
	Set<String> setUniqueMirMatureName = new HashSet<>();
	
	/** 看mirDat文件中是否存在该物种的miRNA */
	public boolean isContainMiRNA(String speciesName) {
		boolean findMiRNA = false;
		txtRead = new TxtReadandWrite(rnaDataFile, false);
		setUniqueMirMatureName.clear();
		List<String> lsBlock = new ArrayList<>();
		for (String string : txtRead.readlines()) {
			if (string.contains(speciesName)) {
				System.out.println("sss");
			}
			if (string.startsWith("//")) {
				List<SeqFasta> lsseqFastas = getSeqFromRNAdata(lsBlock, speciesName);
				lsBlock.clear();
				if (!lsseqFastas.isEmpty()) {
					findMiRNA = true;
					break;
				}
			} else {
				lsBlock.add(string);
			}
		}
		return findMiRNA;
	}
	
	/**
	 * @param setMirnaToBeExtract 待提取的序列名，务必都小写
	 * @param speciesName
	 * @param rnaDataFile
	 * @param rnaHairpinOut
	 * @param rnaMatureOut
	 */
	public ExtractMirSeq(Set<String> setMirnaToBeExtract, String speciesName, 
			String rnaDataFile, String rnaHairpinOut, String rnaMatureOut) {
		if (setMirnaToBeExtract != null && setMirnaToBeExtract.size() > 0) {
			this.setMirnaToBeExtract = setMirnaToBeExtract;
		}
		this.speciesName = speciesName;
		this.rnaDataFile = rnaDataFile;
		this.rnaHairpinOut = rnaHairpinOut;
		this.rnaMatureOut = rnaMatureOut;
	}
	
	/** 内部关闭流 */
	public void writeFile() {
		txtRead = new TxtReadandWrite(rnaDataFile, false);
		txtHairpin = new TxtReadandWrite(rnaHairpinOut, true);
		txtMature = new TxtReadandWrite(rnaMatureOut, true);
		
		setUniqueMirMatureName.clear();
		List<String> lsBlock = new ArrayList<>();
		for (String string : txtRead.readlines()) {
			if (string.startsWith("//")) {
				List<SeqFasta> lsseqFastas = getSeqFromRNAdata(lsBlock, speciesName);
				writeToFile(lsseqFastas);
				lsBlock = new ArrayList<>();
				continue;
			}
			lsBlock.add(string);
		}
		txtRead.close();
		txtHairpin.close();
		txtMature.close();
	}
	
	private void writeToFile(List<SeqFasta> lsseqFastas) {
		if (lsseqFastas.size() == 0) return;
		
		for (int i = 0; i < lsseqFastas.size(); i++) {
			SeqFasta seqFasta = lsseqFastas.get(i);
			String seqName = seqFasta.getSeqName().toLowerCase();
			
			if ((setMirnaToBeExtract != null && !setMirnaToBeExtract.contains(seqName)))
				continue;
			if (i != 0) {
				if (setUniqueMirMatureName.contains(seqName)) {
					continue;
				} else {
					setUniqueMirMatureName.add(seqName);
				}
			}

			if (i == 0) {
				txtHairpin.writefileln(lsseqFastas.get(i).toStringNRfasta());
			} else {
				txtMature.writefileln(lsseqFastas.get(i).toStringNRfasta());
			}
		}
	}
	
	/**
	 * 给定RNAdata文件的一个block，将其中的序列提取出来
	 * @param rnaDataBlock
	 * @return speciesName 物种英文名，无所谓大小写
	 * 后面为成熟体序列
	 */
	private ArrayList<SeqFasta> getSeqFromRNAdata(List<String> lsMirBlock, String speciesName) {
		ArrayList<SeqFasta> lSeqFastas = new ArrayList<SeqFasta>();
		if (!ListMiRNAdat.isFindSpecies(lsMirBlock, speciesName)) {
			return lSeqFastas;
		}
		
		String[] ssID = lsMirBlock.get(0).split(" +");
		
		String miRNAhairpinName = ssID[1]; //ID   cel-lin-4         standard; RNA; CEL; 94 BP.
		ArrayList<MirMature> lsSeqLocation = ListMiRNAdat.getLsMatureMirnaLocation(lsMirBlock);
		String finalSeq = ListMiRNAdat.getHairpinSeq(lsMirBlock);
		
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		SeqFasta seqFasta = new SeqFasta(miRNAhairpinName, finalSeq);
		seqFasta.setDNA(true);
		lsResult.add(seqFasta);
		for (MirMature listDetailBin : lsSeqLocation) {
			SeqFasta seqFastaMature = new SeqFasta();
			seqFastaMature.setName(listDetailBin.getNameSingle());
			seqFastaMature.setSeq(finalSeq.substring(listDetailBin.getStartAbs()-1, listDetailBin.getEndAbs()));
			seqFastaMature.setDNA(true);
			lsResult.add(seqFastaMature);
		}
		return lsResult;
	}
}