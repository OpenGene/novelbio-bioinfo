package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.geneanno.BlastInfo;

/**
 * 将Microarray与本物中序列blast的结果导入数据库
 * <b>注意要设定比对到的是AccID还是geneID，同时也别忘了设定芯片来源setDbInfo</b>
 * @author zong0jie
 *
 */
public class MicroArrayBlast {
	double evalue = 1e-80;
	double identity = 80;
	int blastLength = 90;
	
	String dbInfo = "";
	int taxID = 0;
	/** 默认subID是accID */
	boolean subjectIDIsGeneUniID = false;
	boolean subIDisBlastType = false;
	
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/**
	 * 设定evalue阈值，只有当evalue小于等于该阈值时才会导入数据库
	 * @param evalue
	 */
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	/**
	 * 设定identity阈值，只有当identity大于等于该阈值时才会导入数据库
	 * @param evalue
	 */
	public void setIdentity(double identity) {
		this.identity = identity;
	}
	/**
	 * 设定数据库，指名来源于哪个芯片
	 * @param dbInfo
	 */
	public void setDbInfo(String dbInfo) {
		this.dbInfo = dbInfo;
	}
	/**
	 * 默认false，即subID是accID<br>
	 * true 表示subID是geneUniID
	 */
	public void setSubIDType(boolean subjectIDIsGeneUniID) {
		this.subjectIDIsGeneUniID = subjectIDIsGeneUniID;
	}
	
	/** 默认false，即subID是常规ID
	 * true：subID是类似 dbj|AK240418.1| 这种样子
	 */
	public void setSubIDisBlastType(boolean subIDisBlastType) {
		this.subIDisBlastType = subIDisBlastType;
	}
	
	/**
	 * 将指定的文件导入数据库，必须是每一行都能单独导入的表
	 * 如果需要导入多行，譬如amiGO的信息，请覆盖该方法
	 */
	public void updateFile(String gene2AccFile) {
		TxtReadandWrite txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(gene2AccFile, "_out", "txt"), true);
		List<BlastInfo> lsBlastInfos = new ArrayList<BlastInfo>();
		
		for (String blastStr : txtGene2Acc.readlines()) {
			BlastInfo blastInfo = new BlastInfo(true, taxID, false, taxID, subjectIDIsGeneUniID, blastStr);
			lsBlastInfos.add(blastInfo);
		}
		
		Set<String> setProbeID = new HashSet<String>();
		List<BlastInfo> lsBlastInfosFinal = new ArrayList<BlastInfo>();
		for (BlastInfo blastInfo : lsBlastInfos) {
			if (setProbeID.contains(blastInfo.getQueryID())) {
				continue;
			}
			setProbeID.add(blastInfo.getQueryID());
			lsBlastInfosFinal.add(blastInfo);
		}
		
		//将去重复和排序的比对结果导入数据库
		for (BlastInfo blastInfo : lsBlastInfosFinal) {
			if(!isAddToDB(blastInfo) || blastInfo.getQueryIDtype() != GeneID.IDTYPE_ACCID) {
				continue;
			}
			
			GeneID geneID = new GeneID(blastInfo.getSubjectIDtype(), blastInfo.getSubjectID(), taxID);
			//如果数据库中有这个SubID，那么就导入数据库
			if (geneID.getIDtype() != GeneID.IDTYPE_ACCID) {
				geneID.setUpdateAccID(blastInfo.getQueryID());
				geneID.setUpdateDBinfo(dbInfo, true);
				if (geneID.update(false)) {
					txtOut.writefileln(geneID.getAccID());
				}
			}
		}
		txtOut.close();
		txtGene2Acc.close();
	}
	
	/**
	 * 是否能blast到合适的基因并导入数据库
	 * @param inputBlastInfo blast的那一行
	 * @return
	 */
	private boolean isAddToDB(BlastInfo blastInfo) {
		boolean flag = false;
		if (blastInfo.getIdentities() > identity//一致序列大于90%
		 && blastInfo.getEvalue() < evalue
		) {
			flag = true;
		} else {
			flag = false;
		}
		return flag;
	}
}
