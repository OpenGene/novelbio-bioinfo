package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 将Microarray与本物中序列blast的结果导入数据库
 * <b>注意要设定比对到的是AccID还是geneID，同时也别忘了设定芯片来源setDbInfo</b>
 * @author zong0jie
 *
 */
public class MicroArrayBlast {
	double evalue = 1e-90;
	double identity = 90;
	String dbInfo = "";
	int taxID = 0;
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
	
	String geneIDType = GeneID.IDTYPE_ACCID;
	/**
	 * blast到的ID是accID还是geneID还是UniID
	 * @param blastID
	 */
	public void setGeneID(String geneIDType) {
		this.geneIDType = geneIDType;
	}
	
	/**
	 * 将指定的文件导入数据库，必须是每一行都能单独导入的表
	 * 如果需要导入多行，譬如amiGO的信息，请覆盖该方法
	 */
	public void updateFile(String gene2AccFile) {
		TxtReadandWrite txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(gene2AccFile, "_out", "txt"), true);

		ArrayList<String[]> lsInfo = txtGene2Acc.ExcelRead(1, 1, -1, -1, 1);
		//排个序，按照evalue和identity排序
		Collections.sort(lsInfo, new Comparator<String[]>() {
			/**
			 * 0: queryID
			 * 1: blastID
			 * 2: identity
			 * 10: evalue
			 */
			@Override
			public int compare(String[] o1, String[] o2) {
				Double evalue1 = Double.parseDouble(o1[10]);
				Double identity1 = Double.parseDouble(o1[2]);
				Double evalue2 = Double.parseDouble(o2[10]);
				Double identity2 = Double.parseDouble(o2[2]);
				//evalue越小越好
				int result = evalue1.compareTo(evalue2);
				if (result != 0)
					return result;
				//identity越大越好
				return -identity1.compareTo(identity2);
			}
		});
		//将排序后的lsInfo去重复
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		Set<String> setID = new HashSet<String>();
		for (String[] strings : lsInfo) {
			if (setID.contains(strings[0])) {
				continue;
			}
			lsFinal.add(strings);
		}
		//将去重复和排序的比对结果导入数据库
		for (String[] strings : lsFinal) {
			if(!isAddToDB(strings)) {
				continue;
			}
			
			GeneID geneID = new GeneID(strings[0], taxID);
			//如果数据库中没有这个ID，那么就导入数据库
			if (geneID.getIDtype().equals(GeneID.IDTYPE_ACCID)) {
				if (!geneIDType.equals(GeneID.IDTYPE_ACCID)) {
					geneID.setUpdateGeneID(strings[1], geneIDType);
				} else {
					geneID.setUpdateRefAccID(strings[1]);
				}
			}
			
			geneID.setUpdateDBinfo(dbInfo, true);
			if (geneID.update(false)) {
				txtOut.writefileln(strings);
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
	private boolean isAddToDB(String[] inputBlastInfo) {
		boolean flag = false;
		if (Double.parseDouble(inputBlastInfo[2]) > 90//一致序列大于90%
				&&
		(Double.parseDouble(inputBlastInfo[10]) < 1e-90
		||
		  (Double.parseDouble(inputBlastInfo[3]) > 100//比对长度大于100
				&& 
		  Double.parseDouble(inputBlastInfo[4])+Double.parseDouble(inputBlastInfo[5]) <= Double.parseDouble(inputBlastInfo[3])*0.05//错配和gap之和小于比对长度的0.03
		  )
	   )
      ) {
			flag = true;
		}
		else {
			flag = false;
		}
		return flag;
	}
}
