package com.novelbio.analysis.annotation.cog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.annotation.blast.BlastNBC;
import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.geneanno.EnumSpeciesFile;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.model.species.Species.SeqType;
import com.novelbio.generalConf.PathDetailNBC;

/**
 * 给定序列做COG分析，模式物种文件保存在genome/cog里面，非模式物种保存在
 * @author zong0jie
 *
 */
public class COGanno {
	String cogFastaFile = PathDetailNBC.getCOGfastaFile();
	String seqFastaFile;
	int threadNum = 4;
	double evalueCutoff = 1e-10;
	/** 蛋白id到cogid的对照表 */
	String pro2cogFile;
	/** CogId和Cog具体类的对照表 */
	String cogId2AnnoFile;
	/** Cog单字母对应Cog功能的文件
	 * INFORMATION STORAGE AND PROCESSING
	 * [J] Translation, ribosomal structure and biogenesis 
	 * [A] RNA processing and modification 
	 * [K] Transcription 
	 * [L] Replication, recombination and repair 
	 * [B] Chromatin structure and dynamics 
	 */
	String cogAbbr2FunFile;
	/** 物种信息，有这个信息就把cog文件保存在genome/cog中，没有这个信息就把物种保存在sequence的文件下 */
	Species species;
	
	/** key 为小写 */
	Map<String, CogInfo> mapGeneUniId2CogInfo = new HashMap<>();
	/** key 为小写 */
	Map<String, CogInfo> mapCogId2CogInfo = new HashMap<>();

	public void setCogAbbr2FunFile(String cogAbbr2FunFile) {
		this.cogAbbr2FunFile = cogAbbr2FunFile;
	}
	public void setCogId2AnnoFile(String cogId2AnnoFile) {
		this.cogId2AnnoFile = cogId2AnnoFile;
	}
	/** cog blast的阈值，默认为1e-5 */
	public void setEvalueCutoff(double evalueCutoff) {
		this.evalueCutoff = evalueCutoff;
	}
	/** 蛋白名到cog的对照表 */
	public void setPro2cogFile(String pro2cogFile) {
		this.pro2cogFile = pro2cogFile;
	}
	public void setSpecies(Species species) {
		this.species = species;
		this.seqFastaFile = species.getSeqFile(SeqType.refseqOneIso);
	}
	/** 设定输入的文件 */
	public void setSeqFastaFile(String seqFastaFile) {
		this.seqFastaFile = seqFastaFile;
	}
	
	public void initial() {
		String cogFile = getCOGFile();
		if (!FileOperate.isFileExistAndBigThanSize(cogFile, 0)) {
			String blastFile = blastSeqToCOG();
			generateGene2Cog(blastFile);
		}
		TxtReadandWrite txtRead = new TxtReadandWrite(cogFile);
		for (String content : txtRead.readlines()) {
			String queryId = content.split("\t")[0];
			CogInfo cogInfo = new CogInfo(content);
			mapGeneUniId2CogInfo.put(queryId.toLowerCase(), cogInfo);
			mapCogId2CogInfo.put(cogInfo.getCogId().toLowerCase(), cogInfo);
		}
		txtRead.close();
	}
	
	public CogInfo getCogInfoFromGeneUniId(String geneUniId) {
		return mapGeneUniId2CogInfo.get(geneUniId.toLowerCase());
	}
	public CogInfo getCogInfoFromCogId(String cogId) {
		return mapCogId2CogInfo.get(cogId.toLowerCase());
	}
	
	private void generateGene2Cog(String blastFile) {
		//key 输入的序列名 subject 输出的物种id
		String cogOutFile = getCOGFile();
		String tmp = FileOperate.changeFileSuffix(cogOutFile, "_tmp", null);
		TxtReadandWrite txtWrite = new TxtReadandWrite(tmp, true);
		List<BlastInfo> lsBlastInfo = BlastInfo.readBlastFile(blastFile);
		lsBlastInfo = BlastInfo.removeDuplicateQueryID(lsBlastInfo);
		Map<String, String[]> mapCogId2Anno = getMapCogId2Function(cogId2AnnoFile);
		Map<String, String[]> mapCogAbbr2Fun = getMapCogAbbr2Fun(cogAbbr2FunFile);
		Map<String, String> mapProId2CogId = getMapProId2Cog(pro2cogFile);
		for (BlastInfo blastInfo : lsBlastInfo) {
			CogInfo cogInfo = new CogInfo();
			cogInfo.setCogSeqName(blastInfo.getSubjectID());
			cogInfo.setCogId(mapProId2CogId.get(cogInfo.getCogSeqName()));
			String[] anno = mapCogId2Anno.get(cogInfo.getCogId());
			cogInfo.setCogAbbr(anno[0]);
			cogInfo.setCogAnnoDetail(anno[1]);
			String[] anno2Big = mapCogAbbr2Fun.get(anno[0]);
			cogInfo.setCogAnno(anno2Big[0]);
			cogInfo.setCogAnnoBig(anno2Big[1]);
			cogInfo.setEvalue(blastInfo.getEvalue());
			String qId = blastInfo.getQueryID();
			if (species != null) {
				GeneID geneID = new GeneID(qId, species.getTaxID());
				qId = geneID.getGeneUniID();
			}
			List<String> lsCogResult = cogInfo.toLsArray();
			lsCogResult.add(0, qId);
			txtWrite.writefileln(lsCogResult.toArray(new String[0]));
		}
		txtWrite.close();
		FileOperate.moveFile(true, tmp, cogOutFile);
	}
	
	private String blastSeqToCOG() {
		BlastNBC blastNBC = new BlastNBC();
		BlastType blastType = BlastType.blastp;
		int seqQueryType = SeqHash.getSeqType(seqFastaFile);
		if (seqQueryType != SeqFasta.SEQ_PRO) {
			blastType = BlastType.blastx;
		}
		String blastFileResult = getBlastFile();
		blastNBC.setResultFile(blastFileResult);
		blastNBC.setBlastType(blastType);
		blastNBC.setEvalue(1e-10);
		blastNBC.setCpuNum(threadNum);
		blastNBC.setQueryFastaFile(seqFastaFile);
		blastNBC.setSubjectSeq(cogFastaFile);
		blastNBC.setResultSeqNum(1);
		blastNBC.blast();
		return blastFileResult;
	}
	
	private String getBlastFile() {
		String blastFile = generatePathPrefix() + "blastCOG.txt";
		if (species != null) {
			FileOperate.changeFilePrefix(blastFile, species.getAbbrName() + "_" + species.getVersion(), null);
		}
		return blastFile;
	}
	
	private String getCOGFile() {
		String blastFile = generatePathPrefix() + "COGanno.txt";
		if (species != null) {
			FileOperate.changeFilePrefix(blastFile, species.getAbbrName() + "_" + species.getVersion(), null);
		}
		return blastFile;
	}
	
	/** 返回保存路径的前缀 */
	private String generatePathPrefix() {
		String outPathPrefix = "";
		if (species == null) {
			outPathPrefix = seqFastaFile;
		} else {
			outPathPrefix = EnumSpeciesFile.COG.getSavePath(species.getSelectSpeciesFile());
		}
		return outPathPrefix;
	}
	
	//TODO 还没测试
	public static String getModifiedSeq(String cogSeq, String cogAnno) {
		Map<String, String[]> mapCogId2Function = getMapCogId2Function(cogAnno);
		String modifyFile = FileOperate.changeFileSuffix(cogSeq, "_modify", null);
		TxtReadandWrite txtRead = new TxtReadandWrite(cogSeq);
		TxtReadandWrite txtWrite = new TxtReadandWrite(modifyFile, true);
		boolean isHaveCogFunction = true;
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				String id = content.substring(1);
				isHaveCogFunction = mapCogId2Function.containsKey(id)? true : false;
			}
			if (isHaveCogFunction) {
				txtWrite.writefile(content);
			}
		}
		txtRead.close();
		txtWrite.close();
		return modifyFile;
	}
	
	/** CogId和Cog具体类的对照表
	 * @param cogAnno
	 * @return key: COGID
	 * value: 0 COGabbr
	 * 1: COG anno
	 */
	private static Map<String, String[]> getMapCogId2Function(String cogAnno) {
		Map<String, String[]> mapCogId2Abbr = new HashMap<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(cogAnno);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split(",", 3);
			String[] anno = new String[]{ss[1], ss[2]};
			mapCogId2Abbr.put(ss[0], anno);
		}
		txtRead.close();
		return mapCogId2Abbr;
	}
	
	/** 蛋白Id和CogId的对照表
	 * @param cogId2AnnoFile
	 * @return key: COGID
	 * value: 0 COGabbr
	 * 1: COG anno
	 */
	private static Map<String, String> getMapProId2Cog(String proId2CogIdFile) {
		Map<String, String> MapProId2Cog = new HashMap<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(proId2CogIdFile);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			MapProId2Cog.put(ss[0], ss[1]);
		}
		txtRead.close();
		return MapProId2Cog;
	}
	
	/**
	 * 返回单字母对应的注释
	 * @return
	 * key 单字母 J
	 * value 0 注释 Translation, ribosomal structure and biogenesis 
	 * 1 大类注释 INFORMATION STORAGE AND PROCESSING
	 */
	private static Map<String, String[]> getMapCogAbbr2Fun(String cogAbbr2FunFile) {
		Map<String, String[]> mapAbbr2Fun = new HashMap<>();
		String annoBig = "";//宏观的注释，如INFORMATION STORAGE AND PROCESSING
		TxtReadandWrite txtRead = new TxtReadandWrite(cogAbbr2FunFile);
		for (String content : txtRead.readlines()) {
			if (!content.startsWith("[")) {
				annoBig = content.toLowerCase();
			} else {
				String[] ss = content.split("]");
				String abbr = ss[0].replace("[", "").trim();
				String anno = ss[1].trim();
				mapAbbr2Fun.put(abbr, new String[]{anno, annoBig});
			}
		}
		txtRead.close();
		return mapAbbr2Fun;
	}
	
}
