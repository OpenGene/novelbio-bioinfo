package com.novelbio.analysis.annotation.copeID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 专门将数据整理成适合进行fisher检验的格式，并且进行fisher检验
 * @author zong0jie
 *
 */
public class FisherTest {

	/**
	 * 这个是最完善的方法，其他的方法都是它内部的模块
	 * 给定两个Gene2Item的list，计算Fishertest并得到结果返回。中间调用了R脚本
	 * @param lsGene2Item 差异基因的 gene2item的list
	 * @param lsGene2ItemBG
	 * @param itemInfo 实现该接口，该接口输入Item返回string[] ,然后该string[]会贴到1-n的位置上，作为Item的注释
	 * @return 结果没加标题<br>
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1到n:item信息 <br>
	 * n+1:difGene <br>
	 * n+2:AllDifGene<br>
	 * n+3:GeneInGoID <br>
	 * n+4:AllGene <br>
	 * n+5:Pvalue<br>
	 * n+6:FDR <br>
	 * n+7:enrichment n+8:(-log2P) <br>
	 * @throws Exception 
	 */
	public static ArrayList<String[]> getFisherResult(List<String[]> lsGene2Item,List<String[]> lsGene2ItemBG,ItemInfo itemInfo) throws Exception
	{
		HashMap<String, ArrayList<String>> hashItem2DifGen = getHashItem2Gen(lsGene2Item);
		HashMap<String, ArrayList<String>> hashItem2BGGen = getHashItem2Gen(lsGene2ItemBG);
		int numDif = lsGene2Item.size();
		int numBG = lsGene2ItemBG.size();
		return getFisherResult(hashItem2DifGen, numDif, hashItem2BGGen, numBG, itemInfo);
		
	}
	
	/**
	 * 
	 * @param <T> 任意的输入信息，一般是string或string[]，用来计数的信息
	 * @param hashDif
	 * @param NumDif
	 * @param hashAll
	 * @param NumAll
	 * @param itemInfo 实现该接口，该接口输入Item返回string[] ,然后该string[]会贴到1-n的位置上，作为Item的注释
	 * @return
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1到n:item信息 <br>
	 * n+1:difGene <br>
	 * n+2:AllDifGene<br>
	 * n+3:GeneInGoID <br>
	 * n+4:AllGene <br>
	 * n+5:Pvalue<br>
	 * n+6:FDR <br>
	 * n+7:enrichment n+8:(-log2P) <br>
	 * @throws Exception 
	 */
	public static<T> ArrayList<String[]> getFisherResult(HashMap<String, ArrayList<T>> hashDif,int NumDif,HashMap<String, ArrayList<T>> hashAll ,int NumAll,ItemInfo itemInfo) throws Exception {
		 ArrayList<String[]> lsFiserInput = cope2HashForPvalue(hashDif, NumDif, hashAll, NumAll, itemInfo);
		 ArrayList<String[]> lsFiserResult = doFisherTest(lsFiserInput);
		 return lsFiserResult;
	}
	
	
	
	
	/**
	 * 
	 * 给定两个HashMap，
	 * Item--list-GeneID[]
	 * 一个为总Item
	 * Item---list-GeneID[]
	 * 注意差异基因必须在总基因中
	 * @param hashDif 一个为差异Item
	 * @param NumDif
	 * @param hashAll
	 * @param NumAll
	 * @param itemInfo 实现该接口，该接口输入Item返回string[] ,然后该string[]会贴到1-n的位置上，作为Item的注释
	 * @return
	 * arrayList-string[6]
	 * 0:Item
	 * 1到n：用itemInfo实现的东西贴进去
	 * n+1:difInItemNum
	 * n+2:difNum
	 * n+3:allInItemNum
	 * n+4:AllNum
	 */
	public static<T> ArrayList<String[]> cope2HashForPvalue(HashMap<String, ArrayList<T>> hashDif,int NumDif,HashMap<String, ArrayList<T>> hashAll ,int NumAll,ItemInfo itemInfo) 
	{
		ArrayList<String[]> lsResult=new ArrayList<String[]>();
		//////////////////
		for(Entry<String, ArrayList<T>> entry:hashDif.entrySet())
		{
			String ItemID = entry.getKey();
			ArrayList<T> lsGeneID = entry.getValue();
			String[] strItemInfo = null;
			try {
				strItemInfo = itemInfo.getItemName(ItemID);
			} catch (Exception e) {
				System.out.println("error");
			}
			 
			 
			 String[] tmpResult=new String[strItemInfo.length+5];
			    tmpResult[0]=ItemID;
			    if (ItemID.trim().equals("")) {
					continue;
				}
			    //将注释的信息附加到里面
			   for (int i = 0; i < strItemInfo.length; i++) {
				tmpResult[i+1] = strItemInfo[i];
			   }
			    tmpResult[strItemInfo.length+1]=lsGeneID.size()+"";
			    tmpResult[strItemInfo.length+2]=NumDif+"";
			    if (hashAll.get(ItemID)==null) {
					System.out.println(ItemID);
				}
			    try {
			    	tmpResult[strItemInfo.length+3]=hashAll.get(ItemID).size()+"";
				} catch (Exception e) {
					continue;
				}
			    
			    tmpResult[strItemInfo.length+4]=NumAll+"";
			    lsResult.add(tmpResult);
		}
		////////////////////
		return lsResult;
	}
	
	
	/**
	 * 给定gene2Item的list，将其转化为一个hashMap。格式为
	 * Item--list-GeneID<br>
	 * 当获得了试验和背景的两个hashmap的时候，就可以调用cope2HashForPvalue来计算pvalue
	 * @param <T>
	 * @param lsGene2Item string2 0：gene  1：item,item,item的形式，注意 1. gene不能有重复 2.每个gene内的item不能为空，且不能有重复
	 * @return
	 */
	public static HashMap<String, ArrayList<String>> getHashItem2Gen(List<String[]> lsGene2Item)
	{
		HashMap<String, ArrayList<String>> hashResult = new HashMap<String, ArrayList<String>>();
		for (String[] strings : lsGene2Item) {
			String geneID = strings[0];
			if (strings[1].trim().equals("")) {
				continue;
			}
			String[] items = strings[1].split(",");
			for (String string : items) {
				addHashInfo(hashResult, string, geneID);
			}
		}
		return hashResult;
	}
	
	/**
	 * 将Item和tmpValue加入,内部没有进行去重复，所以需要在外部进行去重复
	 * 如果已经存在有Item，那么将tmpValue附加在listValue的后面
	 * 如果是新key，那么将value
	 * 	填充 path-gene 的hash表
	 * @param hashItem2Gen
	 * @param item 某个GOID或pathID
	 * @param tmpValue 该Item所含有的一个geneID信息，记得输入前去冗余。
	 */
	public static<T> void addHashInfo(HashMap<String, ArrayList<T>> hashItem2Gen,String item, T tmpValue)
	{
		if (hashItem2Gen.containsKey(item)) {
			ArrayList<T> lsGeneID=hashItem2Gen.get(item);
			lsGeneID.add(tmpValue);
		}
		else	{
			ArrayList<T> lsGeneID=new ArrayList<T>();
			//信息还不够全面，添加具体的基因信息
			lsGeneID.add(tmpValue);
			hashItem2Gen.put(item, lsGeneID);
		}
	}
	
	/**
	 * 给定fisher需要的信息，将结果合并后返回为ArrayList - string，结果用pvalue排序
	 * 0:itemID  
	 * 1到n:item信息
	 * n+1:difGene  
	 * n+2:AllDifGene  
	 * n+3:GeneInGoID  
	 * n+4:AllGene 
	 * n+5:Pvalue  
	 * n+6:FDR  
	 * n+7:enrichment  
	 * n+8:(-log2P) ;
	 * @param lsGOinfo
	 * Item的具体信息
	 * arrayList-string[6]
	 * 0:Item
	 * 1到n：用itemInfo实现的东西贴进去
	 * n+1:difInItemNum
	 * n+2:difNum
	 * n+3:allInItemNum
	 * n+4:AllNum
	 * @param 
	 * @throws Exception
	 */
	public static ArrayList<String[]> doFisherTest(List<String[]> lsGOinfo) throws Exception {
		if (lsGOinfo.size() == 0) {
			return null;
		}
		FileOperate.delAllFile(NovelBioConst.R_WORKSPACE_FISHER);
		int colNum = lsGOinfo.get(0).length; colNum = colNum - 5;
		TxtReadandWrite txtGoInfo=new TxtReadandWrite();
		txtGoInfo.setParameter(NovelBioConst.R_WORKSPACE_FISHER_INFO, true, false);
		int column[]=new int[4]; column[0] = colNum + 1; column[1]= colNum + 2; column[2] = colNum + 3; column[3] = colNum + 4;
		txtGoInfo.ExcelWrite(lsGOinfo, "\t", column, true, 1, 1);
		callR();
		TxtReadandWrite txtRresult=new TxtReadandWrite();
		txtRresult.setParameter(NovelBioConst.R_WORKSPACE_FISHER_RESULT, false, true);
		
		String[][] RFisherResult=txtRresult.ExcelRead("\t", 2, 2, txtRresult.ExcelRows(), txtRresult.ExcelColumns(2, "\t"));
		ArrayList<String[]> lsFisherResult=new ArrayList<String[]>();
	
		
		for (int i = 0; i < lsGOinfo.size(); i++) {
			String[] tmp = lsGOinfo.get(i);
			String[] tmp2=new String[tmp.length+RFisherResult[i].length-4];
			for (int j = 0; j < tmp2.length; j++) {
				if( j<tmp.length)
				{
					tmp2[j]=tmp[j];
				}
				else 
				{
					tmp2[j]=RFisherResult[i][j-tmp.length+4];
				}
			}
			lsFisherResult.add(tmp2);
		}
		
		//排序
        Collections.sort(lsFisherResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[6]); Double b=Double.parseDouble(arg1[6]);
                return a.compareTo(b);
            }
        });
		return lsFisherResult;
		//FileOperate.delFile(writeRFIle);
		//FileOperate.delFile(Rresult);
		
	}
	private static void callR() throws Exception{
		//这个就是相对路径，必须在当前文件夹下运行
		String command= NovelBioConst.R_SCRIPT+NovelBioConst.R_WORKSPACE_FISHER_SCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	
}
