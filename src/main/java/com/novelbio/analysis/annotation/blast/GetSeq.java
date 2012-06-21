package com.novelbio.analysis.annotation.blast;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.GeneID;
/**
 * 从NCBI的nt和nr数据库中获得指定物种的序列并整理为fasta格式
	 * 整理成:<br>
	 * >geneID<br>
	 * 序列...............<br>.
	 * 的格式
 * 为后续的blast做准备
 * @author zong0jie
 *
 */
public class GetSeq {
	

	
	/**
	 * 给定nr数据库的标题列，例如：
	 * >gi|71002820|ref|XP_756091.1| conidial pigment biosynthesis 1,3,6,8-tetrahydroxynaphthalene reductase Arp
	 * 2 [Aspergillus fumigatus Af293]gi|6090729|gb|AAF03314.1|AF099736_1 1,3,6,8-tetrahydroxynaphthalene reductase [Asper
	 * gillus fumigatus]gi|5689604|emb|CAB51900.1| 1,3,6,8-tetrahydroxynaphthalene reductase [Aspergillus fumiga
	 * tus]gi|66853729|gb|EAL94053.1| conidial pigment biosynthesis 1,3,6,8-tetrahydroxynaphthalene reductase Arp2 [Asper
	 * gillus fumigatus Af293]gi|159130146|gb|EDP55260.1| 1,3,6,8-tetrahydroxynaphthalene reductase [Aspergillus fumigatus A1163]<br>
	 * 注意之间用奇怪的分隔符分隔开了
	 * @param geneTitle 输入基因的标题，也就是nr中gene的第一列
	 * @param TaxName 物种名
	 * @param hashAcc2GenID accID与geneID的对照表
	 * @param maxGeneIDNum 一个accID很可能对应多个geneID，但是对应太多了就会有问题，所以这里指定一个最大对应数，我觉得3比较合适，就是说一个accID最多对应3个geneID，超过了这个序列就不要了
	 * @return  最后返回该标题所对应的geneID，如果不是指定的物种或没有对应的geneID，则返回null.如果对应的geneID数目大于 maxGeneIDNum ，那么也返回null
	 * 最后返回的是>geneID
	 * 序列
	 * 这种格式，这个文件只能用于blast查询，而不能用于blast建索引，因为一个基因很可能会被切成好几断然后分别有各自的refseq
	 */
	private static String getNrGenID(String geneTitle,String TaxName,HashMap<String, String> hashAcc2GenID,int maxGeneIDNum) 
	{
		String[] ss = geneTitle.split("");
		ArrayList<String> lsGeneID = new ArrayList<String>();
		for (String string : ss)
		{	//将含有指定物种的序列标题装入list
			if (string.contains(TaxName)) 
				lsGeneID.add(string);
		}
		//将list的信息分别进入hash表查找，如果结果中只有一项geneID，那么直接跳出并返回该geneID。
		//如果结果中有多项geneID，那么遍历完全，看最多mache的geneID个数是不是小于maxGeneIDNum。
		//如果小于的话，那么geneID就为那个最小的，也就是用"//"隔开的样式，这样子最后方便添加入数据库，最后多个geneID的信息一起调整
		int minGenNum = 100; //记录其中对应最少的geneID的数量
		int minGenID = -10;//记录该geneID的位置信息，方便从lsGeneID中提取
		int minGeneIDsub = -10; //记录该geneID在具体某个gi|71002820|ref|XP_756091.1| conidial pigment biosynthesis 1,3,6,8-tetrahydroxynaphthalene reductase Arp 2 [Aspergillus fumigatus Af293]中的位置
		for (int i = 0; i< lsGeneID.size(); i++) 
		{
			String string = lsGeneID.get(i); 
			String[] ss2 = string.split("\\|");
			for (int j = 0; j < ss2.length; j++)
			{
				String string2 = ss2[j];
				String accID = GeneID.removeDot(string2);
				String geneID = hashAcc2GenID.get(accID);
				if (geneID == null) 
					continue;
				else if (!geneID.contains("//")) {
					return geneID;
				}
				else {
					String[] ss3 = geneID.split("//");
					//找到最少的那个geneID是由哪个accID查到的
					if (minGenNum > ss3.length) {
						minGenNum = ss3.length;
						minGenID = i;
						minGeneIDsub = j;
					}
				}
			}
		}
		if (minGeneIDsub >= 0) {
			String tmpAccID = lsGeneID.get(minGenID).split("\\|")[minGeneIDsub];
			tmpAccID = GeneID.removeDot(tmpAccID);
			String resultGeneID = hashAcc2GenID.get(tmpAccID);
			if (resultGeneID.split("//").length <= maxGeneIDNum) 
				return resultGeneID;
			else 
				return null;
		}
		else
			return null;
	}
	
	/**
	 * @param TaxName 物种名，注意要和nr里面的名字对应
	 * @param taxID 物种ID
	 * @param nrFile nr数据文件
	 * @param outFile 输出文件
	 * @param maxGeneIDNum 一个accID很可能对应多个geneID，但是对应太多了就会有问题，所以这里指定一个最大对应数，我觉得3比较合适，就是说一个accID最多对应3个geneID，超过了这个序列就不要了
	 * @throws Exception 
	 */
	public static void getNrTaxSeq(String TaxName , int taxID, String nrFile, String outFile, int maxGeneIDNum) throws Exception 
	{
		HashMap<String, String> hashAcc2GenID = HashDB.getHashGenID(taxID);
		TxtReadandWrite txtNrRead = new TxtReadandWrite();
		txtNrRead.setParameter(nrFile, false, true);
		
		TxtReadandWrite txtOutWrite = new TxtReadandWrite();
		txtOutWrite.setParameter(outFile, true, false);
		
		BufferedReader readNr = txtNrRead.readfile();
		String content = ""; boolean flag = false;//用来标记该Seq是否写入文本
		while ((content = readNr.readLine()) != null) {
			if (content.startsWith(">")) 
			{
				flag = false; //每次遇到新的序列就重置flag
				String geneID = getNrGenID(content, TaxName, hashAcc2GenID, maxGeneIDNum);
				if (geneID == null) 
					continue;
				else //找到了geneID
				{
					flag = true;//说明以下写入文本
					txtOutWrite.writefile(">"+geneID +"\n", false);
				}
				continue;
			}
			if (flag) {
				txtOutWrite.writefile(content+"\n", false);
			}
		}
		txtOutWrite.writefile("");//全部写入文本
		txtNrRead.close();
		txtOutWrite.close();
	}
	
	/**
	 * @param taxID 物种ID
	 * @param ntFile nr数据t文件
	 * @param outFile 输出文件
	 * @param maxGeneIDNum 一个accID很可能对 应多个geneID，但是对应太多了就会有问题，所以这里指定一个最大对应数，我觉得3比较合适，就是说一个accID最多对应3个geneID，超过了这个序列就不要了
	 * @throws Exception 
	 */
	public static void getNtTaxSeq( int taxID, String ntFile, String outFile, int maxGeneIDNum) throws Exception 
	{
		HashMap<String, String> hashAcc2GenID = HashDB.getHashGenID(taxID);
		TxtReadandWrite txtNrRead = new TxtReadandWrite();
		txtNrRead.setParameter(ntFile, false, true);
		
		TxtReadandWrite txtOutWrite = new TxtReadandWrite();
		txtOutWrite.setParameter(outFile, true, false);
		
		BufferedReader readNr = txtNrRead.readfile();
		String content = ""; boolean flag = false;//用来标记该Seq是否写入文本
		while ((content = readNr.readLine()) != null) {
			if (content.startsWith(">"))
			{
				flag = false; //每次遇到新的序列就重置flag
				String geneID = getNtGenID(content, hashAcc2GenID, maxGeneIDNum);
				if (geneID == null) 
					continue;
				else //找到了geneID
				{
					flag = true;//说明以下写入文本
					txtOutWrite.writefile(">"+geneID +"\n", false);
				}
				continue;
			}
			if (flag) {
				txtOutWrite.writefile(content+"\n", false);
			}
		}
		txtOutWrite.writefile("");//全部写入文本
		txtNrRead.close();
		txtOutWrite.close();
	}
	/**
	 * 给定nt数据库的标题列，例如：
	 * >gi|4|emb|X17276.1| Giant Panda satellite 1 DNA<br>
	 * @param geneTitle 输入基因的标题，也就是nt中gene的第一列
	 * @param TaxName 物种名
	 * @param hashAcc2GenID accID与geneID的对照表
	 * @param maxGeneIDNum 一个accID很可能对应多个geneID，但是对应太多了就会有问题，所以这里指定一个最大对应数，我觉得3比较合适，就是说一个accID最多对应3个geneID，超过了这个序列就不要了
	 * @return  最后返回该标题中的accID，可以用于做blast索引，如果不是指定的物种或没有对应的geneID，则返回null.如果对应的geneID数目大于 maxGeneIDNum ，那么也返回null
	 */
	private static String getNtGenID(String geneTitle,HashMap<String, String> hashAcc2GenID,int maxGeneIDNum) 
	{
		//将list的信息分别进入hash表查找，如果结果中只有一项geneID，那么直接跳出并返回该geneID。
		//如果结果中有多项geneID，那么遍历完全，看最多mache的geneID个数是不是小于maxGeneIDNum。
		//如果小于的话，那么geneID就为那个最小的，也就是用"//"隔开的样式，这样子最后方便添加入数据库，最后多个geneID的信息一起调整
		int minGenNum = 100; //记录其中对应最少的geneID的数量
		int minGeneIDsub = -10; //记录该geneID在具体某个gi|71002820|ref|XP_756091.1| conidial pigment biosynthesis 1,3,6,8-tetrahydroxynaphthalene reductase Arp 2 [Aspergillus fumigatus Af293]中的位置
		String[] ss2 = geneTitle.split("\\|");
		for (int j = 0; j < ss2.length; j++)
		{
			String string2 = ss2[j];
			String accID = KGprepare.removeDot(string2);
			String geneID = hashAcc2GenID.get(accID);
			if (geneID == null) 
				continue;
			else if (!geneID.contains("//")) {
				return accID;
			}
			else {
				String[] ss3 = geneID.split("//");
				//找到最少的那个geneID是由哪个accID查到的
				if (minGenNum > ss3.length) {
					minGenNum = ss3.length;
					minGeneIDsub = j;//是第几个ID
				}
			}
		}
		
		if (minGeneIDsub >= 0) {
			String tmpAccID = geneTitle.split("\\|")[minGeneIDsub];
			tmpAccID = KGprepare.removeDot(tmpAccID);
			String resultGeneID = hashAcc2GenID.get(tmpAccID);
			if (resultGeneID.split("//").length <= maxGeneIDNum) 
				return tmpAccID;
			else 
				return null;
		}
		else
			return null;
	}
	
	
}
