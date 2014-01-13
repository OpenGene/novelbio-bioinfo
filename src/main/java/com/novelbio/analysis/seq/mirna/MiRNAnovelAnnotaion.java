package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.annotation.blast.BlastNBC;
import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.base.PathDetail;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.species.Species;

/** 新miRNA的注释 */
public class MiRNAnovelAnnotaion implements IntCmdSoft {
	static String sepSymbol = SepSign.SEP_INFO;
	boolean isUseOldResult = true;
	String pathTmpBlast;
	String miRNAthis;
	String miRNAcope;
	/** 分割novelmirName和blast到的mirName的标识 */
	List<Species> lsBlastToSpecies = new ArrayList<>();
	List<String> lsTmpBlastResult = new ArrayList<>();
	Map<String, String> mapID2Blast;
	List<String> lsCmd = new ArrayList<>();
	/** 设定需要比对到的物种 */
	public void setLsMiRNAblastTo(List<Species> lsBlastToSpecies, String pathTmpBlast) {
		this.pathTmpBlast = FileOperate.addSep(pathTmpBlast);
		this.lsBlastToSpecies = lsBlastToSpecies;
	}
	public void setIsUseOldResult(boolean isUseOldResult) {
		this.isUseOldResult = isUseOldResult;
	}
	public void setMiRNAthis(String miRNAthis) {
		this.miRNAthis = miRNAthis;
		this.miRNAcope = FileOperate.changeFileSuffix(miRNAthis, "_anno", null);
	}
	
	/** 会将输入的miRNA序列的名字进行修正 */
	public void annotation() {
		List<Species> lsSpecies = new ArrayList<>();
		for (Species species : lsBlastToSpecies) {
			if (species == null || species.getTaxID() == 0) {
				continue;
			}
			lsSpecies.add(species);
		}
		lsBlastToSpecies = lsSpecies;
		if (lsBlastToSpecies.isEmpty()) {
			return;
		}
		
		blast();
		mapID2Blast = getMapGeneID2Info();
		copeSeqFile(mapID2Blast);
	}
	
	/** 返回修正过名字的miRNA */
	public String getMiRNAmatureCope() {
		return miRNAcope;
	}
	
	/** 获得注释好的对照表
	 * key: 本miRNA name
	 * value: BlastTo miRNA name
	 *  */
	public Map<String, String> getMapID2Blast() {
		return mapID2Blast;
	}
	
	private void blast() {
		FileOperate.createFolders(pathTmpBlast);
		lsCmd.clear();
		BlastNBC blastNBC = new BlastNBC();
		lsTmpBlastResult.clear();
		blastNBC.setQueryFastaFile(miRNAthis);
		for (Species species : lsBlastToSpecies) {
			String tmpBlastResult = pathTmpBlast + "novel_miRNA_blast_to_" + species.getNameLatin().trim().replace(" ", "_");
			lsTmpBlastResult.add(tmpBlastResult);
			if (!isUseOldResult || !FileOperate.isFileExistAndBigThanSize(tmpBlastResult, 0)) {
				blastNBC.setSubjectSeq(species.getMiRNAmatureFile());
				blastNBC.setShortQuerySeq(true);
				blastNBC.setBlastType(BlastType.blastn);
				blastNBC.setResultSeqNum(1);
				blastNBC.setResultType(BlastNBC.ResultType_Simple);
				blastNBC.setResultFile(tmpBlastResult);
				blastNBC.blast();
				lsCmd.addAll(blastNBC.getCmdExeStr());
			}
		}
	}
	
	/**
	 * @return key：mirName 为小写
	 * value：Subject
	 */
	private Map<String, String> getMapGeneID2Info() {
		Map<String, String> mapGeneID2BlastID = new HashMap<>();
		List<BlastInfo> lsBlastInfoAll = new ArrayList<>();
		for (String string : lsTmpBlastResult) {
			lsBlastInfoAll.addAll(BlastInfo.readBlastFile(string));
		}
		List<BlastInfo> lsBlastInfo = BlastInfo.removeDuplicate(lsBlastInfoAll);
		
		for (BlastInfo blastInfo : lsBlastInfo) {
			//过滤一下
			if (blastInfo.getAlignLen() < 16 || blastInfo.getIdentities() < 90 || (blastInfo.getGapNum() + blastInfo.getMismatchNum()) > 4) {
				continue;
			}
			mapGeneID2BlastID.put(blastInfo.getQueryID().toLowerCase(), blastInfo.getSubjectID());
		}
		return mapGeneID2BlastID;
	}
	
	/**
	 * @param mapID2Blast key为小写
	 */
	private void copeSeqFile(Map<String, String> mapID2Blast) {
		TxtReadandWrite txtRead = new TxtReadandWrite(miRNAthis);
		TxtReadandWrite txtWrite = new TxtReadandWrite(miRNAcope, true);
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				content = content.replace(">", "").trim();
				String key = content.toLowerCase();
				if (mapID2Blast.containsKey(key)) {
					content = ">" + content + sepSymbol + mapID2Blast.get(key);
				} else {
					content = ">" + content;
				}
			}
			txtWrite.writefileln(content);
		}
		txtRead.close();
		txtWrite.close();
	}
	
	public static String getSepSymbol() {
		return sepSymbol;
	}

	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
}
