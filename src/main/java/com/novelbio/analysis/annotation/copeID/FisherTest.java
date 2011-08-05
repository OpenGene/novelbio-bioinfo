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
 * ר�Ž�����������ʺϽ���fisher����ĸ�ʽ�����ҽ���fisher����
 * @author zong0jie
 *
 */
public class FisherTest {

	/**
	 * ����������Ƶķ����������ķ����������ڲ���ģ��
	 * ��������Gene2Item��list������Fishertest���õ�������ء��м������R�ű�
	 * @param lsGene2Item �������� gene2item��list
	 * @param lsGene2ItemBG
	 * @param itemInfo ʵ�ָýӿڣ��ýӿ�����Item����string[] ,Ȼ���string[]������1-n��λ���ϣ���ΪItem��ע��
	 * @return ���û�ӱ���<br>
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1��n:item��Ϣ <br>
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
	 * @param <T> �����������Ϣ��һ����string��string[]��������������Ϣ
	 * @param hashDif
	 * @param NumDif
	 * @param hashAll
	 * @param NumAll
	 * @param itemInfo ʵ�ָýӿڣ��ýӿ�����Item����string[] ,Ȼ���string[]������1-n��λ���ϣ���ΪItem��ע��
	 * @return
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1��n:item��Ϣ <br>
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
	 * ��������HashMap��
	 * Item--list-GeneID[]
	 * һ��Ϊ��Item
	 * Item---list-GeneID[]
	 * ע��������������ܻ�����
	 * @param hashDif һ��Ϊ����Item
	 * @param NumDif
	 * @param hashAll
	 * @param NumAll
	 * @param itemInfo ʵ�ָýӿڣ��ýӿ�����Item����string[] ,Ȼ���string[]������1-n��λ���ϣ���ΪItem��ע��
	 * @return
	 * arrayList-string[6]
	 * 0:Item
	 * 1��n����itemInfoʵ�ֵĶ�������ȥ
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
			    //��ע�͵���Ϣ���ӵ�����
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
	 * ����gene2Item��list������ת��Ϊһ��hashMap����ʽΪ
	 * Item--list-GeneID<br>
	 * �����������ͱ���������hashmap��ʱ�򣬾Ϳ��Ե���cope2HashForPvalue������pvalue
	 * @param <T>
	 * @param lsGene2Item string2 0��gene  1��item,item,item����ʽ��ע�� 1. gene�������ظ� 2.ÿ��gene�ڵ�item����Ϊ�գ��Ҳ������ظ�
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
	 * ��Item��tmpValue����,�ڲ�û�н���ȥ�ظ���������Ҫ���ⲿ����ȥ�ظ�
	 * ����Ѿ�������Item����ô��tmpValue������listValue�ĺ���
	 * �������key����ô��value
	 * 	��� path-gene ��hash��
	 * @param hashItem2Gen
	 * @param item ĳ��GOID��pathID
	 * @param tmpValue ��Item�����е�һ��geneID��Ϣ���ǵ�����ǰȥ���ࡣ
	 */
	public static<T> void addHashInfo(HashMap<String, ArrayList<T>> hashItem2Gen,String item, T tmpValue)
	{
		if (hashItem2Gen.containsKey(item)) {
			ArrayList<T> lsGeneID=hashItem2Gen.get(item);
			lsGeneID.add(tmpValue);
		}
		else	{
			ArrayList<T> lsGeneID=new ArrayList<T>();
			//��Ϣ������ȫ�棬��Ӿ���Ļ�����Ϣ
			lsGeneID.add(tmpValue);
			hashItem2Gen.put(item, lsGeneID);
		}
	}
	
	/**
	 * ����fisher��Ҫ����Ϣ��������ϲ��󷵻�ΪArrayList - string�������pvalue����
	 * 0:itemID  
	 * 1��n:item��Ϣ
	 * n+1:difGene  
	 * n+2:AllDifGene  
	 * n+3:GeneInGoID  
	 * n+4:AllGene 
	 * n+5:Pvalue  
	 * n+6:FDR  
	 * n+7:enrichment  
	 * n+8:(-log2P) ;
	 * @param lsGOinfo
	 * Item�ľ�����Ϣ
	 * arrayList-string[6]
	 * 0:Item
	 * 1��n����itemInfoʵ�ֵĶ�������ȥ
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
		
		//����
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
		//����������·���������ڵ�ǰ�ļ���������
		String command= NovelBioConst.R_SCRIPT+NovelBioConst.R_WORKSPACE_FISHER_SCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	
}
