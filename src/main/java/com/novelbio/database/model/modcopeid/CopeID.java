package com.novelbio.database.model.modcopeid;


import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.database.service.ServAnno;

/**
 * 专门对基因的ID做一些处理的类<br>
 * 也可以将输入的ID合并起来，并且将分散的ID存储在一个Hashmap中
 * @author zong0jie
 *
 */
public class CopeID {
	/**
	 * blast的结果可能类似dbj|AK240418.1|
	 * 将里面的AK240418抓出来并返回
	 * @return
	 */
	public static String getBlastAccID(String blastGenID) {
		String[] ss = blastGenID.split("\\|");
		return removeDot(ss[1]);
	}
	
	/**
	 *  首先除去空格
	 * 如果类似XM_002121.1类型，那么将.1去除
	 * @param accID
	 * @return accID without .1
	 */
	public static String removeDot(String accID)
	{
		String tmpGeneID = accID.trim();
		int dotIndex = tmpGeneID.lastIndexOf(".");
		//如果类似XM_002121.1类型
		if (dotIndex>0 && tmpGeneID.length() - dotIndex == 2) {
			tmpGeneID = tmpGeneID.substring(0,dotIndex);
		}
		return tmpGeneID;
	}
	

//	static HashMap<String, String> hashGeneID2AccID = new HashMap<String, String>();
	/**
	 * 保存查找的geneID与accID的关系，只有当需要合并ID时本hashmap才会填充<br>
	 * key:某一个条件下的hashgene2AccID信息，譬如上调，下调或背景
	 * 
	 * key:geneID/UniID<br>
	 * value:accID//accID//accID
	 */
	static HashMap<String, HashMap<String, String>> hashCondGeneID2AccID = new HashMap<String, HashMap<String,String>>();
	/**
	 * 获得保存查找的geneID与accID的关系，只有当需要合并ID时本hashmap才会填充<br>
	 * 必须首先运行getGenID方法<br>
	 * @param 指定哪一个条件下的hashGene2Acc，譬如上调，下调或背景
	 * key:geneID/UniID<br>
	 * value:accID//accID//accID
	 * @return
	 */
	public static HashMap<String, String> getHashGenID2AccID(String cond) {
		return hashCondGeneID2AccID.get(cond);
	}
	
	/**
	 * 保存输入的accID，去重复用的
	 */
	static HashSet<String> hashAccID = new HashSet<String>();
	
	/**
	 * 保存输入的geneID，去重复用的
	 */
	static HashSet<String> hashGenID = new HashSet<String>();
	
	/**
	 * 输入 accID，taxID,accID最好之前做一下trim
	 * 因为很常见的一个accID会对应多个geneID，所以是返回list
	 * @param accID
	 * @param taxID 如果accID不是symbol，taxID可以为0
	 * @param sepID 是否分开ID
	 * @param hashGeneID2AccID 每个geneID 对应的 accID列表，在合并ID时用到
	 * @return
	 * arraylist-string[3]
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * 在hashGeneID2AccID中的重复ID会返回null
	 */
	private static ArrayList<String[]> combainID( String accID, int taxID,boolean sepID,HashMap<String, String> hashGeneID2AccID) 
	{
		
		////////////////////////去重复///////////////////////////////////////////////////
		if (hashAccID.contains(accID)) {
			return null;
		}
		else {
			hashAccID.add(accID);
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////
		ArrayList<String[]> lsAccResult = new ArrayList<String[]>();
		
		ArrayList<String> lsaccID = ServAnno.getNCBIUni(accID, taxID);
		String type = lsaccID.get(0);
		 for (int i = 1 ; i<lsaccID.size();i++) {
			 String accID2;
			 String tmpGenID = lsaccID.get(i);
			 
			 String[] accIDResult = new String[3];
			 accIDResult[0] = type;
			 accIDResult[1] = accID;
			 accIDResult[2] = tmpGenID;

			 
			 
			 lsAccResult.add(accIDResult);
			 //////////////////////////////////////////////////
			 if ((accID2 = hashGeneID2AccID.get(tmpGenID))!=null) //如果hash表里已经存在，将非冗余的accID加入hash表
			 {
				 String[] ss = accID2.split("//");
				 boolean add = true;
				for (String string : ss) {
					if (string.equals(accID)) {
						add = false;
						break;
					}
				}
				if (add) {
					accID2 = accID2 + "//" + accID;
					hashGeneID2AccID.put(tmpGenID, accID2);
				}
			 }
			 else//没有就装入hashMap
			 {
				hashGeneID2AccID.put(tmpGenID, accID);
			 }
			 //////////////////////////////////////////////////
			 if (!sepID) //合并ID
			 {
					if (hashGenID.contains(tmpGenID)) {
						return null;
					}
					else {
						hashGenID.add(tmpGenID);
					}
			 }
			 //////////////////////////////////////////////////
		}
		 return lsAccResult;
	}
	
	/**
	 * 给定一系列accID，以及taxID, 将这一系列的accID去重复后整合成一个list,其中一个accID对应多个geneID的情况已经考虑进去
	 * 结果可以进行go以及pathway的分析
	 * 如果colAccID.size() == 0；直接返回一个空的结果，就是size==0,但不是null
	 * @param condition 指定条件，譬如上调，下调或背景
	 * @param colAccID 一系列的accID，最好是list形式
	 * @param taxID
	 * @param sepID
	 * true：不合并ID，所有的accID经过去重复后得到结果
	 * false：合并ID，所有的accID中相同geneID的只保留一项，但是hashGeneID2AccID依然添加进去
	 * @return 
	 * 将这一系列的accID去重复后整合成一个list<br>
	 * arrayList-string[3] :<br>
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 */
	public static ArrayList<String[]> getGenID(String condition, AbstractCollection<String> colAccID, int taxID,boolean sepID)
	{
		hashAccID = new HashSet<String>();
		hashGenID = new HashSet<String>();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		if (colAccID.size() == 0) {
			return lsResult;
		}
		HashMap<String, String> hashGeneID2AccID = new HashMap<String, String>();
		hashCondGeneID2AccID.put(condition, hashGeneID2AccID);
		for (String string : colAccID) {
			if (string == null || string.trim().equals("")) {
				continue;
			}
			ArrayList<String[]> lsTmp = combainID(removeDot(string), taxID,sepID,hashGeneID2AccID);
			if (lsTmp != null ) {
				lsResult.addAll(lsTmp);
			}
		}
		return lsResult;
	}
	
	/**
	 * 
	 * 
	 * 在合并ID的情况下，将分开的accID装到合并geneID的前面去。
	 * 主要是一个geneID对应好多个accID，然后最后得到了合并geneID的list，那么结果中要把accID附加上去，也就是生成一个与输入相同的非合并ID集
	 * @param condition 获得某个条件下的hashgene2AccID信息，譬如上调，下调或背景。hashgene2AccID在产生时，会根据不同的condition产生不同的hashgene2AccID，譬如上调一个，下调一个，背景一个
	 * @param lsGenInfo 给定的gene信息
	 * @param searchCol 指定lsGenInfo的第几列为唯一ID列，也就是geneID列，从0开始记数
	 * @param replaceCol 指定lsGenInfo的第几列是QueryID列，也就是探针列，从0开始记数
	 * @param sepID 是否需要分开，如果true。那么输入和输出没变化
	 * @return
	 */
	public static ArrayList<String[]> copeCombineID(String condition, ArrayList<String[]> lsGenInfo,int searchCol,int replaceCol,boolean sepID) {
		   ArrayList<String[]> lsResultFinal = null;
	        //如果合并ID，那么要将每一个基因的accID对到相应的geneID前面
			HashMap<String, String> hashGeneID2AccID = hashCondGeneID2AccID.get(condition);
	        if (!sepID) 
	        {
	        	//////////合并lsGoResult///////////////////////////////////////////////////////
	        	lsResultFinal = new ArrayList<String[]>();
	        	for (String[] strings : lsGenInfo) 
	        	{
	        		//获得某个geneID的所有accID
	        	
	        		String[] accIDarray = hashGeneID2AccID.get(strings[searchCol]).split("//");
	        		for (int i = 0; i < accIDarray.length; i++) {
	        			//将这些accID都附加到该geneID上，并且装入list
						String[] tmpResultFinal = new String[strings.length];
						for (int j = 0; j < tmpResultFinal.length; j++) {
							tmpResultFinal[j] = strings[j];
						}
						tmpResultFinal[replaceCol] = accIDarray[i];
						lsResultFinal.add(tmpResultFinal);
	 				}
				}
	        	return lsResultFinal;
			}
	        else 
	        {
	        	return  lsGenInfo;
			}
	        
	}
	
}
