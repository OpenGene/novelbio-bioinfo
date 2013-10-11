package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genome.gffOperate.ListHashBin;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
/**
 * 读取miRNA.dat的信息，构建listabs表，方便给定mirID和loc，从而查找到底是5p还是3p
 * @author zong0jie
 *
 */
public class ListMiRNAdeep extends ListHashBin implements ListMiRNAInt {
	private static final Logger logger = Logger.getLogger(ListMiRNAdeep.class);
	Set<String> setMiRNApredict;
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
	/** 设定预测出来的miRNA */
	public void setSetMiRNApredict(Set<String> setMiRNApredict) {
		this.setMiRNApredict = setMiRNApredict;
	}
	
	/**
	 * 读取mirdeep文件夹下的run_26_06_2012_t_12_25_36/output.mrd
	 * @param rnadataFile
	 */
	protected void ReadGffarrayExcepMirDeep(String rnadataFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(rnadataFile, false);
		ListBin<ListDetailBin> lsMiRNA = null;
		super.mapName2DetailAbs = new LinkedHashMap<String, ListDetailBin>();
		super.lsNameNoRedundent = new ArrayList<String>();
		for (String string : txtRead.readlines()) {
			if (string.startsWith(">") ) {
				lsMiRNA = new ListBin<ListDetailBin>();
				lsMiRNA.setName(string.substring(1).trim());
				lsMiRNA.setCis5to3(true);
				//装入chrHash
				getMapChrID2LsGff().put(lsMiRNA.getName(), lsMiRNA);
			}
			if (string.startsWith("exp")) {
				String mirModel = string.replace("exp", "").trim();
				setMatureMiRNAdeep(lsMiRNA, mirModel);
			}
		}
		txtRead.close();
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
			if (mirModel[i] == 'f' || mirModel[i] == 'I') {
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
					if (setMiRNApredict != null && setMiRNApredict.size() > 0 && !setMiRNApredict.contains(name)) {
						continue;
					}
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
					if (setMiRNApredict != null && setMiRNApredict.size() > 0 && !setMiRNApredict.contains(name)) {
						continue;
					}
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
			logger.error("出现未知miRNA前体名字，是否需要更新miRNA.dat文件：" + mirName);
			return null;
		}
		return element.getNameSingle();
	}

}

