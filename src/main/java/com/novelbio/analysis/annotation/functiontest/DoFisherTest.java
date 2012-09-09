package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.MathComput;

/**
 * ר�Ž�����������ʺϽ���fisher����ĸ�ʽ�����ҽ���fisher����
 * @author zong0jie
 *
 */
public class DoFisherTest {

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
	public static ArrayList<String[]> getFisherResult(List<String[]> lsGene2Item,List<String[]> lsGene2ItemBG,ItemInfo itemInfo) throws Exception {
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
	public static<T> ArrayList<String[]> cope2HashForPvalue(HashMap<String, ArrayList<T>> hashDif,int NumDif,HashMap<String, ArrayList<T>> hashAll ,int NumAll,ItemInfo itemInfo) {
		ArrayList<String[]> lsResult=new ArrayList<String[]>();
		//////////////////
		for(Entry<String, ArrayList<T>> entry:hashDif.entrySet()) {
			String ItemID = entry.getKey();
			ArrayList<T> lsGeneID = entry.getValue();
			String[] strItemInfo = null;
			try {
				strItemInfo = itemInfo.getItemName(ItemID);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
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
	public static HashMap<String, ArrayList<String>> getHashItem2Gen(List<String[]> lsGene2Item) {
		HashMap<String, ArrayList<String>> hashResult = new HashMap<String, ArrayList<String>>();
		for (String[] strings : lsGene2Item) {
			String geneID = strings[0];
			if (strings[1] == null || strings[1].trim().equals("")) {
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
	public static<T> void addHashInfo(HashMap<String, ArrayList<T>> hashItem2Gen,String item, T tmpValue) {
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
		//����fisher�����������ֵ
		int colNum = lsGOinfo.get(0).length; colNum = colNum - 5;
		int max = 0;
		for (String[] strings : lsGOinfo) {
			int tmp = Integer.parseInt(strings[colNum + 1]) + Integer.parseInt(strings[colNum + 2]) + Integer.parseInt(strings[colNum + 3]) + Integer.parseInt(strings[colNum + 4]);
			if (tmp > max) {
				max = tmp; 
			}
		}
		FisherTest fisherTest = new FisherTest(max);
		ArrayList<String[]> lsFisherResult=new ArrayList<String[]>();
		ArrayList<Double> lsPvalue = new ArrayList<Double>();
		for (int i = 0; i < lsGOinfo.size(); i++) {
			String[] tmp = lsGOinfo.get(i);
			String[] tmp2=new String[tmp.length+4];
			
			for (int j = 0; j < tmp.length; j++) {
				if( j<tmp.length) {
					tmp2[j]=tmp[j];
				}
			}
			int a = Integer.parseInt(tmp[colNum + 1]);
			int b = Integer.parseInt(tmp[colNum + 2]);
			int c = Integer.parseInt(tmp[colNum + 3]);
			int d = Integer.parseInt(tmp[colNum + 4]);
			double pvalue = fisherTest.getRightTailedP(a, b, c, d);
			lsPvalue.add(pvalue);
			tmp2[tmp.length] = pvalue + "";
			tmp2[tmp.length + 2] = ((double)a/b)/((double)c/d) + "";
			tmp2[tmp.length + 3] = -Math.log(pvalue)/Math.log(2) + "";
			lsFisherResult.add(tmp2);
		}
		ArrayList<Double> lsFDR = MathComput.pvalue2Fdr(lsPvalue);
		for (int i = 0; i < lsFisherResult.size(); i++) {
			String[] strings = lsFisherResult.get(i);
			strings[strings.length - 3] = lsFDR.get(i) + "";
		}

		//����
        Collections.sort(lsFisherResult,new Comparator<String[]>(){
            public int compare(String[] arg0, String[] arg1) {
            	Double a=Double.parseDouble(arg0[6]); Double b=Double.parseDouble(arg1[6]);
                return a.compareTo(b);
            }
        });
		return lsFisherResult;
		
	}
	
}
