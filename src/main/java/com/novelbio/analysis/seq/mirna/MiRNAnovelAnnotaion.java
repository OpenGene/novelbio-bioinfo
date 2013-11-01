package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class MiRNAnovelAnnotaion {
	static String sepSymbol = SepSign.SEP_INFO;

	String miRNAthis;
	String miRNAcope;
	/** 分割novelmirName和blast到的mirName的标识 */
	List<Species> lsBlastToSpecies = new ArrayList<>();
	List<String> lsTmpBlastResult = new ArrayList<>();
	Map<String, String> mapID2Blast;
	/** 设定需要比对到的物种 */
	public void setLsMiRNAblastTo(List<Species> lsBlastToSpecies) {
		this.lsBlastToSpecies = lsBlastToSpecies;
	}
	
	public void setMiRNAthis(String miRNAthis) {
		this.miRNAthis = miRNAthis;
		this.miRNAcope = FileOperate.changeFileSuffix(miRNAthis, "_anno", null);
	}
	
	/** 会将输入的miRNA序列的名字进行修正 */
	public void annotation() {
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
		BlastNBC blastNBC = new BlastNBC();
		lsTmpBlastResult.clear();
		blastNBC.setQueryFastaFile(miRNAthis);
		for (Species species : lsBlastToSpecies) {
			if (species == null || species.getTaxID() == 0) {
				continue;
			}
			blastNBC.setSubjectSeq(species.getMiRNAmatureFile());
			blastNBC.setShortQuerySeq(true);
			blastNBC.setBlastType(BlastType.blastn);
			blastNBC.setResultSeqNum(1);
			blastNBC.setResultType(BlastNBC.ResultType_Simple);
			String tmpBlastFile = FileOperate.addSep(PathDetail.getTmpPath()) + species.getAbbrName() + DateUtil.getDateAndRandom();
			lsTmpBlastResult.add(tmpBlastFile);
			blastNBC.setResultFile(tmpBlastFile);
			blastNBC.blast();
		}
	}
	
	private Map<String, String> getMapGeneID2Info() {
		Map<String, String> mapGeneID2BlastID = new HashMap<>();
		List<BlastInfo> lsBlastInfoAll = new ArrayList<>();
		for (String string : lsTmpBlastResult) {
			lsBlastInfoAll.addAll(BlastInfo.readBlastFile(string));
		}
		List<BlastInfo> lsBlastInfo = BlastInfo.removeDuplicate(lsBlastInfoAll);
		
		for (BlastInfo blastInfo : lsBlastInfo) {
			mapGeneID2BlastID.put(blastInfo.getQueryID(), blastInfo.getSubjectID());
		}
		return mapGeneID2BlastID;
	}
	
	private void copeSeqFile(Map<String, String> mapID2Blast) {
		TxtReadandWrite txtRead = new TxtReadandWrite(miRNAthis);
		TxtReadandWrite txtWrite = new TxtReadandWrite(miRNAcope, true);
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				content = content.replace(">", "").trim();
				if (mapID2Blast.containsKey(content)) {
					content = ">" + content + sepSymbol + mapID2Blast.get(content);
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
}
