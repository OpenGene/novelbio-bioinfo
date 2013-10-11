package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genome.gffOperate.ListHashBin;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.database.model.species.Species;
/**
 * 读取miRNA.dat的信息，构建listabs表，方便给定mirID和loc，从而查找到底是5p还是3p
 * @author zong0jie
 *
 */
public class ListMiRNAdate extends ListHashBin implements ListMiRNAInt {
	private static final Logger logger = Logger.getLogger(ListMiRNAdate.class);

	/** taxID对应RNA.data中的String */
	HashMap<Integer, String> hashSpecies = new HashMap<Integer, String>();
	String speciesAbbr = "HSA";
	Species species;
	/**
	 * 为miRNA.dat中的物种名
	 * 设定物种，默认为人类：HSA
	 * 具体要检查RNA.data文件
	 * @param species
	 */
	public void setSpecies(Species species) {
		this.species = species;
	}
	/**
	 * 读取miRNA.data文件，同时读取和它一起的整理好的taxID2species文件
	 */
	protected void ReadGffarrayExcep(String rnadataFile) {
		ReadGffarrayExcepRNADat(rnadataFile);
	}
	
	private String getLatinName() {
		String resultName = "";
		String[] names = species.getNameLatin().split(" ");
		if (names.length > 1) {
			resultName = names[0] + " " + names[1];
		} else {
			resultName = species.getNameLatin();
		}
		return resultName;
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
	

	private String getHairpinSeq(List<String> lsMirBlock) {
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
		super.mapName2DetailAbs = new LinkedHashMap<String, ListDetailBin>();
		super.lsNameNoRedundent = new ArrayList<String>();
		List<String> lsMirnaBlock = new ArrayList<>();
		for (String string : txtRead.readlines()) {
			if (string.startsWith("//")) {				
				copeMirBlock(lsMirnaBlock, getLatinName());
				lsMirnaBlock = new ArrayList<>();
				continue;
			}
			lsMirnaBlock.add(string);
		}
		txtRead.close();
	}	
	
	
	
	private void copeMirBlock(List<String> lsMirnaBlock, String speciesName) {
		if (!isFindSpecies(lsMirnaBlock, speciesName)) {
			return;
		}
		ListBin<ListDetailBin> lsMiRNA = new ListBin<>();
		String[] sepInfo = lsMirnaBlock.get(0).split(" +");
		lsMiRNA.setName(sepInfo[1]);
		lsMiRNA.setCis5to3(true);
		//装入chrHash
		getMapChrID2LsGff().put(lsMiRNA.getName().toLowerCase(), lsMiRNA);
		List<ListDetailBin> lsMiRNABin = getLsMatureMirnaLocation(lsMirnaBlock);
		for (ListDetailBin miRNAbin : lsMiRNABin) {
			mapName2DetailAbs.put(miRNAbin.getNameSingle(), miRNAbin);
			lsNameNoRedundent.add(miRNAbin.getNameSingle());
			miRNAbin.setParentListAbs(lsMiRNA);
			lsMiRNA.add(miRNAbin);
		}
	}
	
	private ArrayList<ListDetailBin> getLsMatureMirnaLocation(List<String> lsMirBlock) {
		ArrayList<ListDetailBin> lsResult = new ArrayList<ListDetailBin>();
		ListDetailBin lsMiRNAhairpin = null;
		for (String string : lsMirBlock) {
			String[] sepInfo = string.split(" +");
			if (sepInfo[0].equals("FT")) {
				if (sepInfo[1].equals("miRNA")) {
					lsMiRNAhairpin = new ListDetailBin();
					lsMiRNAhairpin.setCis5to3(true);
					String[] loc = sepInfo[2].split("\\.\\.");
					lsMiRNAhairpin.setStartAbs(Integer.parseInt(loc[0]));
					lsMiRNAhairpin.setEndAbs(Integer.parseInt(loc[1]));
				}
				if (sepInfo[1].contains("product")) {
					String accID = sepInfo[1].split("=")[1];
					accID = accID.replace("\"", "");
					lsMiRNAhairpin.addItemName(accID);
					lsResult.add(lsMiRNAhairpin);
				}
			}
		}
		return lsResult;
	}
	private boolean isFindSpecies(List<String> lsMirBlock, String speciesName) {
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
		ListDetailBin element = searchElement(mirName, start, end);
		if (element == null) {
			logger.error("出现未知miRNA前体名字，是否需要更新miRNA.dat文件：" + mirName);
			return null;
		}
		return element.getNameSingle();
	}
	
	class ExtractMirSeq {
		Set<String> setMirnaToBeExtract;
		String speciesName;
		TxtReadandWrite txtRead;
		TxtReadandWrite txtHairpin;
		TxtReadandWrite txtMature;
		Set<String> setUniqueMirMatureName = new HashSet<>();
		
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
			txtRead = new TxtReadandWrite(rnaDataFile, false);
			txtHairpin = new TxtReadandWrite(rnaHairpinOut, true);
			txtMature = new TxtReadandWrite(rnaMatureOut, true);
		}
		
		/** 内部关闭流 */
		public void writeFile() {
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
			if (!isFindSpecies(lsMirBlock, speciesName)) {
				return lSeqFastas;
			}
			
			String[] ssID = lsMirBlock.get(0).split(" +");
			
			String miRNAhairpinName = ssID[1]; //ID   cel-lin-4         standard; RNA; CEL; 94 BP.
			ArrayList<ListDetailBin> lsSeqLocation = getLsMatureMirnaLocation(lsMirBlock);
			String finalSeq = getHairpinSeq(lsMirBlock);
			
			ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
			SeqFasta seqFasta = new SeqFasta(miRNAhairpinName, finalSeq);
			seqFasta.setDNA(true);
			lsResult.add(seqFasta);
			for (ListDetailBin listDetailBin : lsSeqLocation) {
				SeqFasta seqFastaMature = new SeqFasta();
				seqFastaMature.setName(listDetailBin.getNameSingle());
				seqFastaMature.setSeq(finalSeq.substring(listDetailBin.getStartAbs()-1, listDetailBin.getEndAbs()));
				seqFastaMature.setDNA(true);
				lsResult.add(seqFastaMature);
			}
			return lsResult;
		}
	}

}

