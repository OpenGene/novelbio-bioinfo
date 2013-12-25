package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genome.gffOperate.ListHashBin;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * 读取miRNA.dat的信息，构建listabs表，方便给定mirID和loc，从而查找到底是5p还是3p
 * @author zong0jie
 *
 */
public class ListMiRNAdeep extends ListHashBin implements ListMiRNAInt {
	private static final Logger logger = Logger.getLogger(ListMiRNAdeep.class);
//	Set<String> setMiRNApredict;
	Map<String, String> mapID2Blast;
	/**
	 * 读取miRNA.data文件，同时读取和它一起的整理好的taxID2species文件
	 */
	protected void ReadGffarrayExcep(String rnadataFile) {
		ReadGffarrayExcepMirDeep(rnadataFile);
	}
	
	/** 设定注释后的miRNA名字 */
	public void setBlastMap(Map<String, String> mapID2Blast) {
		this.mapID2Blast = mapID2Blast;
	}
//	/** 设定预测出来的miRNA */
//	public void setSetMiRNApredict(Set<String> setMiRNApredict) {
//		this.setMiRNApredict = setMiRNApredict;
//	}
	
	/**
	 * 读取mirdeep文件夹下的run_26_06_2012_t_12_25_36/output.mrd
	 * @param rnadataFile
	 */
	protected void ReadGffarrayExcepMirDeep(String rnadataFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(rnadataFile, false);
		ListBin<ListDetailBin> lsMiRNA = null;
		List<String> lsBlock = new ArrayList<>();
		for (String string : txtRead.readlines()) {
			if (string.startsWith(">")) {
				lsMiRNA = getMiRNAbin(lsBlock);
				if (lsMiRNA != null) {
					getMapChrID2LsGff().put(lsMiRNA.getName().toLowerCase(), lsMiRNA);
				}
				lsBlock.clear();
			}
			lsBlock.add(string);
		}
		lsMiRNA = getMiRNAbin(lsBlock);
		if (lsMiRNA != null) {
			getMapChrID2LsGff().put(lsMiRNA.getName().toLowerCase(), lsMiRNA);
		}
		txtRead.close();
	}
	
	private ListBin<ListDetailBin> getMiRNAbin(List<String> lsBlock) {
		if (lsBlock.size() == 0) return null;
		ListBin<ListDetailBin> lsMiRNA = new ListBin<ListDetailBin>();
		lsMiRNA.setName(lsBlock.get(0).substring(1).trim());
		lsMiRNA.setCis5to3(true);

		PatternOperate patternOperate = new PatternOperate("\\bseq_\\d+", false);
		String mirModel = null;
		boolean readDetail = false;
		
		for (String string : lsBlock) {
			if (string.startsWith(">") ) {
				lsMiRNA.setName(string.substring(1).trim());
				lsMiRNA.setCis5to3(true);
			} else if (string.startsWith("exp")) {
				mirModel = string.replace("exp", "").trim();
				setMatureMiRNAdeep(lsMiRNA, mirModel);
			} else if (string.startsWith("pri_struct ")) {
				readDetail = true;
				continue;
			}
			if (readDetail && !string.equals("") && patternOperate.getPatFirst(string.split(" ")[0]) == null) {
				return null;
			}
		}
		return lsMiRNA;
	}
	
	/**
	 * fffffffffffffffffffffMMMMMMMMMMMMMMMMMMMMMMllllllllllllllllllllllllllllllllSSSSSSSSSSSSSSSSSSSSSSfffffffffffffffff
	 * M: mature mirna
	 * S: star mirna
	 */
	private void setMatureMiRNAdeep(ListBin<ListDetailBin> lsMirnaMautre, String mirModelString) {
		char[] mirModel = mirModelString.toCharArray();
		boolean MstartFlag = false;
		boolean SstartFlag = false;
		ListDetailBin listDetailBin = null;
		for (int i = 0; i < mirModel.length; i++) {
			if (mirModel[i] == 'f' || mirModel[i] == 'l') {
				if (MstartFlag) {
					listDetailBin.setEndAbs(i);
					MstartFlag = false;
				}
				if (SstartFlag) {
					listDetailBin.setEndAbs(i);
					SstartFlag = false;
				}
				continue;
			}
			else if (mirModel[i] == 'M') {
				if (!MstartFlag) {
					listDetailBin = new ListDetailBin();
					String name = lsMirnaMautre.getName() + "_mature";
//					if (setMiRNApredict != null && setMiRNApredict.size() > 0 && !setMiRNApredict.contains(name)) {
//						continue;
//					}
					if (mapID2Blast != null && mapID2Blast.size() > 0 && mapID2Blast.containsKey(name)) {
						name += SepSign.SEP_INFO + mapID2Blast.get(name);
					}
					listDetailBin.addItemName(name);
					listDetailBin.setCis5to3(true);
					listDetailBin.setStartAbs(i+1);
					lsMirnaMautre.add(listDetailBin);
					MstartFlag = true;
				}
			}
			else if (mirModel[i] == 'S') {
				if (!SstartFlag) {
					listDetailBin = new ListDetailBin();
					String name = lsMirnaMautre.getName() + "_star";
//					if (setMiRNApredict != null && setMiRNApredict.size() > 0 && !setMiRNApredict.contains(name)) {
//						continue;
//					}
					if (mapID2Blast != null && mapID2Blast.size() > 0 && mapID2Blast.containsKey(name)) {
						name += SepSign.SEP_INFO + mapID2Blast.get(name);
					}
					listDetailBin.addItemName(name);
					listDetailBin.setCis5to3(true);
					listDetailBin.setStartAbs(i+1);
					lsMirnaMautre.add(listDetailBin);
					
					SstartFlag = true;
				}
			}
		}
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
 			logger.error("没有比对到miRNA上：" + mirName +" " + start + " " + end);
			return null;
		}
		return element.getNameSingle();
	}
	
	/**
	 * 提取序列，同时将mrd文件复制到提取序列的文件夹下，并改名为Predict_MiRNA_Structure.txt
	 * @param run_output_mrd 待提取的文件
	 * @param outMatureSeq 输出
	 * @param outPreSeq 输出
	 */
	public static void extractHairpinSeqMatureSeq(String run_output_mrd, String outMatureSeq, String outPreSeq) {
		FileOperate.createFolders(FileOperate.getParentPathName(outMatureSeq));
		FileOperate.createFolders(FileOperate.getParentPathName(outPreSeq));
		
		TxtReadandWrite txtReadMrd = new TxtReadandWrite(run_output_mrd, false);
		TxtReadandWrite txtWriteMature = new TxtReadandWrite(outMatureSeq, true);
		TxtReadandWrite txtWritePre = new TxtReadandWrite(outPreSeq, true);
		
		List<String> lsBlock = new ArrayList<>();
 		for (String string : txtReadMrd.readlines()) {
			if (string.startsWith(">")) {
				List<SeqFasta> lsMiRNA = getLsMirna(lsBlock);
				writeMir(lsMiRNA, txtWritePre, txtWriteMature);
				lsBlock.clear();
			}
			lsBlock.add(string);
 		}
		List<SeqFasta> lsMiRNA = getLsMirna(lsBlock);
		writeMir(lsMiRNA, txtWritePre, txtWriteMature);
		
		String mrdNew = FileOperate.getParentPathName(outMatureSeq) + "Predict_MiRNA_Structure.txt";
		FileOperate.copyFile(run_output_mrd, mrdNew, true);
		
		txtReadMrd.close();
		txtWriteMature.close();
		txtWritePre.close();
	}
	
	private static List<SeqFasta> getLsMirna(List<String> lsMirInfo) {
		if (lsMirInfo.size() == 0) {
			return new ArrayList<>();
		}
		
		String mirName = lsMirInfo.get(0).substring(1).trim();
		String mirModel = "", mirSeq = "";
		List<SeqFasta> lsMirna = null;
		PatternOperate patternOperate = new PatternOperate("\\bseq_\\d+", false);
		boolean readDetail = false;
		boolean isExistMiRNA = false;
		for (String string : lsMirInfo) {
			string = string.trim();
			if (string.startsWith("exp")) {
				mirModel = string.replace("exp", "").trim();
			} else if (string.startsWith("pri_seq ")) {
				mirSeq = string.replace("pri_seq ", "").trim();
				lsMirna = getMirDeepSeq(mirName, mirModel, mirSeq);
			} else if (string.startsWith("pri_struct ")) {
				readDetail = true;
				continue;
			}
			
			if (readDetail && !string.equals("") && patternOperate.getPatFirst(string.split(" ")[0]) == null) {
				isExistMiRNA = true;
			}
		}
		if (isExistMiRNA) {
			return new ArrayList<>();
		}
		return lsMirna;
	}
	
	private static void writeMir(List<SeqFasta> lsMiRNA, TxtReadandWrite txtMirPre, TxtReadandWrite txtMirMature) {
		for (int i = 0; i < lsMiRNA.size(); i++) {
			if (i == 0) {
				txtMirPre.writefileln(lsMiRNA.get(i).toStringNRfasta());
			} else {
				txtMirMature.writefileln(lsMiRNA.get(i).toStringNRfasta());
			}
		}
	}
	
	/**
	 * 给定RNAdeep的结果文件，从里面提取序列
	 * @param seqName
	 * @param mirModel
	 * @param mirSeq
	 * @return
	 * 0: precess
	 * 1: mature
	 * 2: star
	 */
	private static ArrayList<SeqFasta> getMirDeepSeq(String seqName, String mirModel, String mirSeq) {
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		
		SeqFasta seqFasta = new SeqFasta(seqName, mirSeq);
		seqFasta.setDNA(true);
		
		int startS = mirModel.indexOf("S"); int endS = mirModel.lastIndexOf("S");
		int startM = mirModel.indexOf("M"); int endM = mirModel.lastIndexOf("M");
		
		SeqFasta seqFastaMature = new SeqFasta(seqName + "_mature", mirSeq.substring(startM, endM));
		seqFastaMature.setDNA(true);
		SeqFasta seqFastaStar = new SeqFasta(seqName + "_star", mirSeq.substring(startS, endS));
		seqFastaStar.setDNA(true);
		
		lsResult.add(seqFasta);
		lsResult.add(seqFastaMature);
		lsResult.add(seqFastaStar);
		return lsResult;
	}

}

