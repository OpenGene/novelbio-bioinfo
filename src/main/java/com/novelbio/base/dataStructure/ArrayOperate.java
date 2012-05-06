package com.novelbio.base.dataStructure;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class ArrayOperate {
	private static final Logger logger = Logger.getLogger(ArrayOperate.class);
	/**
	 * �ϲ��ַ�������
	 * @param ss
	 * @param sep
	 * @return
	 */
	public static String cmbString(String[] ss, String sep) {
		String result = "";
		if (ss.length < 1) {
			return "";
		}
		result = ss[0];
		for (int i = 1; i < ss.length; i++) {
			result = result + "\t" + ss[i];
		}
		return result;
	}
	/**
	 * ����lsString����lsString����ArrayList-String[]��������ϲ�ΪString[][]��Ҳ��������cbind
	 * @param lsStrings
	 * @return
	 */
	public static String[][] combCol(ArrayList<String[][]> lsStrings) {
		int rowNum=lsStrings.get(0).length;
		int columnNum=lsStrings.size();
		String[][] result=new String[rowNum][columnNum];
		for (int i = 0; i < columnNum; i++) 
		{
			for (int j = 0; j < rowNum; j++) 
			{
				result[j][i]=lsStrings.get(i)[j][0];
			}
		}
		return result;
	}
	/**
	 * ��ά[][]�ĺϲ�������AT[][]��BT[][]
	 * ��AT[][]��BT[][]�ϲ�������ָ��BT������AT����һ����
	 * <b>ע�⣬AT��BT�����ͱ�����ȣ������������,Ҳ���ǵ�һά�������</b>
	 * @param AT
	 * @param BT
	 * @param instNum ���뵽AT�ĵڼ���֮����AT��ʵ����,���instNum<1�������BT����AT�ĺ��档
	 * @return
	 * 
	 */
	public static <T> T[][] combArray(T[][] AT,T[][] BT,int instNum) {
		int rowNum=AT.length;
		int colNum=AT[0].length+BT[0].length;
		if (instNum<1) {
			instNum=AT[0].length;
		}
		instNum--;
		//ͨ������ķ����½�����
		T[][]  result = (T[][]) Array.newInstance(AT.getClass().getComponentType().getComponentType(),rowNum,colNum);
		for (int i = 0; i < rowNum; i++) 
		{
			for (int j = 0; j < colNum; j++) 
			{
				if (j<=instNum) {
					result[i][j]=AT[i][j];
				}
				else if (j>instNum&&j<=instNum+BT[0].length) {
					result[i][j]=BT[i][j-instNum-1];
				}
				else
				{
					result[i][j]=AT[i][j-BT[0].length];
				}
			}
		}
		return result;
	}
	/**
	 * ��ά[][]�ĺϲ�������AT[][]��BT[][]
	 * ��AT[][]��BT[][]�ϲ�������ָ��BT������AT����һ����
	 * <b>ע�⣬AT��BT�����ͱ�����ȣ������������,Ҳ���ǵ�һά�������</b>
	 * @param AT
	 * @param BT
	 * @param instNum ���뵽AT�ĵڼ���֮����AT��ʵ����,���instNum<1�������BT����AT�ĺ��档
	 * @return
	 * 
	 */
	public static <T> ArrayList<T[]> combArray(List<T[]> lsAT,List<T[]> lsBT,int instNum) {
		int rowNum = lsAT.size();
		int colNum = lsAT.get(0).length+lsBT.get(0).length;
		if (instNum<1) {
			instNum = lsAT.get(0).length;
		}
		instNum--;
		//ͨ������ķ����½�����
		ArrayList<T[]> lsResult = new ArrayList<T[]>();
		T[][]  result = (T[][]) Array.newInstance(lsAT.get(0).getClass().getComponentType(),rowNum,colNum);
		for (int i = 0; i < rowNum; i++) 
		{
			for (int j = 0; j < colNum; j++) 
			{
				if (j<=instNum) {
					result[i][j]=lsAT.get(i)[j];
				}
				else if (j>instNum&&j<=instNum+lsBT.get(0).length) {
					result[i][j]=lsBT.get(i)[j-instNum-1];
				}
				else
				{
					result[i][j]=lsAT.get(i)[j-lsBT.get(i).length];
				}
				lsResult.add(result[i]);
			}
		}
		return lsResult;
	}
	
	
	
	/**
	 * @deprecated
	 * ����{@link combArray}ȡ��
	 * String[][]�ĺϲ�������Astring[][]��Bstring[][]
	 * ��Aobject[][]��Bobject[][]�ϲ�������ָ��Bobject������Aobject����һ�к�
	 * <b>ע�⣬Aobject��Bobject�����ͱ�����ȣ������������,Ҳ���ǵ�һά�������</b>
	 * @param Aobject
	 * @param Bobject
	 * @param instNum ���뵽Aobject�ĵڼ���֮����Aobject��ʵ����,���instNum<1�������Bobject����Aobject�ĺ��档
	 * @return
	 */
	public static String[][] combStrArray(String[][] Astring,String[][] Bstring,int instNum) {
		int rowNum=Astring.length;
		int colNum=Astring[0].length+Bstring[0].length;
		if (instNum<1) {
			instNum=Astring[0].length;
		}
		instNum--;
		
		String[][] result=new String[rowNum][colNum];
		for (int i = 0; i < rowNum; i++) 
		{
			for (int j = 0; j < colNum; j++) 
			{
				if (j<=instNum) {
					result[i][j]=Astring[i][j];
				}
				else if (j>instNum&&j<=instNum+Bstring[0].length) {
					result[i][j]=Bstring[i][j-instNum-1];
				}
				else
				{
					result[i][j]=Astring[i][j-Bstring[0].length];
				}
			}
		}
		return result;
	}
	
	/**
	 * String[]�ĺϲ�������Astring[]��Bstring[]
	 * ��Astring[]��Bstring[]�ϲ�������ָ��Bstring������Astring����һ�к�
	 * <b>ע�⣬Astring��Bstring�����ͱ������</b>
	 * @param Astring
	 * @param Bstring
	 * @param instNum ���뵽Astring�ĵڼ���֮����Astring��ʵ����,���instNum<1�������Bobject����Aobject�ĺ��档
	 * @return
	 */
	public static<T> T[] combArray(T[] Aarray,T[] Barray,int instNum) {
		if (instNum<1) {
			instNum=Aarray.length;
		}
		instNum--;
		T[] result=(T[]) Array.newInstance(Aarray.getClass().getComponentType(), Aarray.length + Barray.length);//new T[Astring.length+Bstring.length];
		for (int i = 0; i < result.length; i++) {
			if (i<=instNum) {
				result[i]=Aarray[i];
			}
			else if (i>instNum&&i<=instNum+Barray.length) {
				result[i]=Barray[i-instNum-1];
			}
			else {
				result[i]=Aarray[i-Barray.length];
			}
		}
		return result;
	}
	
	/**
	 * ɾ�������е�һЩ��Ŀ
	 * @param <T>
	 * @param Aarray ����
	 * @param deletNum ��Ҫɾ���ļ����0��ʼ���㣬�����������������
	 * @return
	 */
	public static<T> T[] deletElement(T[] Aarray,int[] deletNum) {
		TreeSet<Integer> treeRemove = new TreeSet<Integer>();
		for (int i : deletNum) {
			if (i < 0 || i >= Aarray.length) {
				continue;
			}
			treeRemove.add(i);
		}
		
		T[] result=(T[]) Array.newInstance(Aarray.getClass().getComponentType(), Aarray.length - treeRemove.size());//new T[Astring.length+Bstring.length];
		int resultNum = 0;
		for (int i = 0; i < Aarray.length; i++) {
			if (treeRemove.contains(i)) {
				continue;
			}
			result[resultNum] = Aarray[i];
			resultNum++ ;
		}
		return result;
	}
	/**
	 * ��hash�ķ������ϲ�����List<br>
	 * ����lsA��lsB<br>
	 * ����lsA�ĵ�AcolNum��û���ظ�����0��ʼ���㣩<br>
	 * ��lsA�ĵ�AcolNum����0��ʼ���㣩��lsB�ĵ�BcolNum����0��ʼ���㣩���бȽ�<br>
	 * �����ͬ����AcolNumȫ����ӵ�lsB���棬��󷵻���Ӻõ�lsA<br>
	 * @return
	 */
	public static ArrayList<String[]> combArrayListHash(List<String[]> lsA ,List<String[]> lsB, int AcolNum, int BcolNum) 
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		Hashtable<String, String[]> hashLsA = new Hashtable<String, String[]>();
		for (String[] strings : lsA) {
			String tmpKey = strings[AcolNum];
			hashLsA.put(tmpKey.trim(), strings);
		}
		for (String[] strings : lsB) {
			String tmpKeyB = strings[BcolNum];
			String[] tmpA = hashLsA.get(tmpKeyB.trim());
			if (tmpA == null) {
				logger.error("no lsA element equals lsB: "+tmpKeyB);
				continue;
			}
			String[] tmpResult = combArray(strings, tmpA, 0);
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	

	/**
	 * ��֮ǰҪ�����ָ����column�Ƿ���ls�� <br>
	 * ����List���������ָ����ĳ����,��õ�ĳ���а���ָ�����н�������,<b>��0��ʼ����</b><br>
	 * ����ȥ��ָ����ĳ����<br>
	 * ��һ��boolean������ָ��<br>
	 * @return
	 */
	public static ArrayList<String[]> listCope(ArrayList<String[]> ls, int[] colNum, boolean include) 
	{
		if (include) {
			return listCope(ls, colNum);
		}
		else {
			HashSet<Integer> hashCol = new HashSet<Integer>();
			for (int i = 0; i < colNum.length; i++) {
				hashCol.add(colNum[i]);
			}
			int[] colNumResult = new int[ls.get(0).length - colNum.length]; 
			int k=0;//�������������Ҳ������Ҫ�ļ���
			//������������
			for (int i = 0; i < ls.get(0).length; i++) {
				//���������ȥ�����У�������
				if (hashCol.contains(i))
				{
					continue;
				}
				colNumResult[k] = i; k++;
			}
			return listCope(ls, colNumResult);
		}
	}
	/**
	 * ����List���������ָ����ĳ����,��õ�ĳ���а���ָ�����н������У���0��ʼ����<br>
	 * @param ls
	 * @param colNum
	 * @return
	 */
	private static ArrayList<String[]> listCope(ArrayList<String[]> ls, int[] colNum) 
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String[] strings : ls) {
			String[] tmpString = new String[colNum.length];
			for (int i = 0; i < tmpString.length; i++) {
				tmpString[i] = strings[colNum[i]];
			}
			lsResult.add(tmpString);
		}
		return lsResult;
	}
	
	
	/**
	 * ����listȡ����,ע��lsA��lsB���治Ҫ���ظ���
	 * @return
	 */
	public static ArrayList<String> getCoLs(List<String>  lsA, List<String> lsB) {
		ArrayList<String> lsResult = new ArrayList<String>();
		HashSet<String> hashA = new HashSet<String>();
		for (String string : lsA) {
			hashA.add(string);
		}
		for (String string : lsB) {
			if (hashA.contains(string)) {
				lsResult.add(string);
			}
		}
		return lsResult;
	}
	
	/**
	 * ����һ�����飬�Լ������е����꣬�������������꣬�и�������������
	 * @param array ����
	 * @param center ����λ�ã�Ʃ��2�Ļ������Ǹ�����ĵڶ�λ
	 * @param up ���������Ԫ�ظ���
	 * @param down ���������Ԫ�ظ���
	 * @param thisdefault Ĭ��ֵ������û�еĵط���ʲô���
	 * @return
	 * ��󷵻س���Ϊ up+1+down��array
	 */
	public static double[] cuttArray(double[] array, int center, int up, int down, double thisdefault) {
		center--;
		double[] result = new double[up + down +1];
		for (int i = 0; i < result.length; i++) {
			result[i] = thisdefault;
		}
		int resultCenter = up;
		for (int i = center; i >= 0; i--) {
			if (resultCenter < 0) {
				break;
			}
			result[resultCenter] = array[i];
			resultCenter--;
		}
		resultCenter = up + 1;
		for (int i = center + 1; i < array.length; i++) {
			if (resultCenter - up > down) {
				break;
			}
			result[resultCenter] = array[i];
			resultCenter++;
		}
		return result;
	}
	
	/**
	 * �ߵ����飬ֱ���Խ���������鵹�ã������ض���
	 * @param array
	 */
	public static void convertArray(int[] array) 
	{
		int tmpValue=0;
		int arrayLength=array.length;
		for (int i = 0; i < arrayLength/2; i++) {
			tmpValue=array[arrayLength-1-i];
			array[arrayLength-1-i]=array[i];
			array[i]=tmpValue;
		}
	}
	
	/**
	 * �ߵ����飬ֱ���Խ���������鵹�ã������ض���
	 * @param array
	 */
	public static void convertArray(double[] array) 
	{
		double tmpValue=0;
		int arrayLength=array.length;
		for (int i = 0; i < arrayLength/2; i++) {
			tmpValue=array[arrayLength-1-i];
			array[arrayLength-1-i]=array[i];
			array[i]=tmpValue;
		}
	}
	
	/**
	 * �ߵ����飬ֱ���Խ���������鵹�ã������ض���
	 * @param array
	 */
	public static<T> void convertArray(T[] array) 
	{
		T tmpValue=null;
		int arrayLength=array.length;
		for (int i = 0; i < arrayLength/2; i++) {
			tmpValue=array[arrayLength-1-i];
			array[arrayLength-1-i]=array[i];
			array[i]=tmpValue;
		}
	}
	/**
	 * ��hashmap��key��ȡ����
	 * @param <K> key
	 * @param <V> value
	 * @param hashMap
	 * 	 * û�з���null
	 */
	public static<K,V> ArrayList<K> getArrayListKey(HashMap<K, V> hashMap) {
		if (hashMap == null || hashMap.size() == 0) {
			return null;
		}
		ArrayList<K> lsResult = new ArrayList<K>();
		Set<K> keys = hashMap.keySet();
		for(K key:keys)
		{
			lsResult.add(key);
		}
		return lsResult;
	}
	/**
	 * ��hashmap��value��ȡ����
	 * @param <K> key
	 * @param <V> value
	 * @param hashMap
	 * 	 * û�з���null
	 */
	public static<K,V> ArrayList<V> getArrayListValue(HashMap<K, V> hashMap) {
		if (hashMap == null || hashMap.size() == 0) {
			return null;
		}
		ArrayList<V> lsResult = new ArrayList<V>();
		Collection<V> values = hashMap.values();
		for(V value:values)
		{
			lsResult.add(value);
		}
		return lsResult;
	}
	
	/**
	 * ��hashset��value��ȡ����
	 * @param <K> key
	 * @param hashset
	 * û�з���һ���յ�arraylist
	 */
	public static<K> ArrayList<K> getArrayListValue(HashSet<K> hashset) {
		if (hashset == null || hashset.size() == 0) {
			return new ArrayList<K>();
		}
		
		ArrayList<K> lsResult = new ArrayList<K>();
		for(K value:hashset)
		{
			lsResult.add(value);
		}
		return lsResult;
	}
	
	/**
	 * ��������
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static<T> T[] copyArray(T[] array) {
		return copyArray(array, array.length);
	}
	/**
	 * ��������
	 * @param <T>
	 * @param array
	 * @param Length ��array��Lengthλ���Ƹ����array�����length > array.length�����ӳ����array
	 * @return
	 * �������Length���ȵ�array
	 */
	public static<T> T[] copyArray(T[] array, int Length) {
		T[] result=(T[]) Array.newInstance(array.getClass().getComponentType(), Length);
		for (int i = 0; i < array.length; i++) {
			if (i >= Length) {
				continue;
			}
			result[i] = array[i];
		}
		return result;
	}
	/**
	 * ��������
	 * @param <T>
	 * @param array
	 * @param Length ��array��Lengthλ���Ƹ����array�����Length > array.length�����ӳ����array
	 * @param ����ƿ�ǰ���ǿ��󣬿�ǰ infoXXX������XXXinfo
	 * @return
	 * �������Length���ȵ�array
	 */
	public static<T> T[] copyArray(T[] array, int Length,boolean start) {
		T[] result=(T[]) Array.newInstance(array.getClass().getComponentType(), Length);
		if (start) {
			for (int i = 0; i < array.length; i++) {
				if (i >= Length) {
					continue;
				}
				result[i] = array[i];
			}
		}
		else
		{
			int j = 0;
			for (int i = array.length - 1; i >= 0; i--) {
				j ++;
				if (j >= Length) {
					continue;
				}
				result[i] = array[i];
			}
		}
		return result;
	}
	
	/**
	 * @param <T> ʵ��compSubArray�ӿ�
	 * @param lsThisList ��һ��list
	 * @param lsCmpList �ڶ���list
	 * @param cis �Ƿ�����ıȣ�true��list��Ԫ�ش�С����������ÿ��cellС����ǰ����ں�
	 * false��list��Ԫ�شӴ�С������ÿ��cell�����ǰС���ں�
	 * @return
	 * list<br>
	 * |<br>
	 * |------ListlsElement|---listCell1��Element1,Element2<br>
	 * |                          |---listCell2��Element3<br>
	 * |             <br>
	 * |             <br>
	 * |<br>
	 * |<br>
	 * |<br>
	 */
	public<T extends CompSubArray> ArrayList<ArrayList<ArrayList<T>>> cmpLsaaa(ArrayList<T> lsThisList, ArrayList<T> lsCmpList, boolean cis)
	{
		ArrayList<ArrayList<ArrayList<T>>> lsResult = new ArrayList<ArrayList<ArrayList<T>>>();
		int score = 0; // ��֣������������ת¼���ж�����
		boolean flag1 = true;// �Ƿ��¼�����exon1
		boolean flag2 = true;// �Ƿ��¼�����exon2
		int i = 0;
		int j = 0;
		//ÿ����Ԫ����Ϣ
		ArrayList<ArrayList<T>> lsElement = new ArrayList<ArrayList<T>>();
		ArrayList<T> lsThisCell = new ArrayList<T>();
		ArrayList<T> lsCmpCell = new ArrayList<T>();
		while (true) {
			if (i >= lsThisList.size() || j >= lsCmpList.size()) {
				break;
			}
			double[] exon1 = lsThisList.get(i).getCell();
			double[] exon2 = lsCmpList.get(j).getCell();
			double[] tmpFlag = null;
			
			if (cis)
				tmpFlag = cmpArray(exon1, exon2);
			else
				tmpFlag = cmpArrayTrans(exon1, exon2);

			if (tmpFlag[0] == 0) {
				//����elementһ����С��������µĵ�Ԫ
				lsElement = new ArrayList<ArrayList<T>>();
				lsThisCell.add(lsThisList.get(i));
				lsCmpCell.add(lsCmpList.get(j));
				lsElement.add(lsThisCell); lsElement.add(lsCmpCell);
				lsResult.add(lsElement);
				lsThisCell = new ArrayList<T>();//�½�cell
				lsCmpCell = new ArrayList<T>();//�½�cell
				i++;j++;
			}
			//
			else if (tmpFlag[0] < 4) //element1 �� β�� �� element2 �� β�� ǰ
			{
				i++;
				if (tmpFlag[0] != 1) {
					flag2 = false;
					if (lsThisCell.size() > 0) {
						//����Ѿ����˸�element��������
						double[] tmpThisLast =  lsThisCell.get(lsThisCell.size()-1).getCell(); // ������һ��cell
						double[] tmpThis = lsThisList.get(i).getCell();
						if (tmpThisLast[0] != tmpThis[0] || tmpThisLast[1] != tmpThis[1]) {
							lsThisCell.add(lsThisList.get(i));
						}
						//����Ѿ����˸�element��������
						double[] tmpCmpLast =  lsCmpCell.get(lsCmpCell.size()-1).getCell(); // ������һ��cell
						double[] tmpCmp = lsCmpList.get(i).getCell();
						if (tmpCmpLast[0] != tmpCmp[0] || tmpCmpLast[1] != tmpCmp[1]) {
							lsCmpCell.add(lsCmpList.get(i));
						}
					}
					lsThisCell.add(lsThisList.get(i));
					lsCmpCell.add(lsCmpList.get(i));
					score = score + 1;
				}
				//
				else {
					if (flag1) {
						lsThisCell = new ArrayList<T>();//�½�cell
						lsCmpCell = new ArrayList<T>();//�½�cell
						lsCmpCell.add(lsCmpList.get(j));
						score = score + 1;
					} else {
						flag1 = true; // ˵����element��Ҫ����һ���µ���
						lsThisCell = new ArrayList<T>();//�½�cell
						lsCmpCell = new ArrayList<T>();//�½�cell
					}
				}
			} else if (tmpFlag[0] >= 4) {
				j++;
				// ����˸�exon
				if (tmpFlag[0] != 6) {
					flag1 = false;
					score = score + 1;
				} else {
					if (flag2) {
						score = score + 1;
					} else {
						flag2 = true;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Ŀǰֻ�������������arraylist
	 * @param <T> ʵ��compSubArray�ӿ�
	 * @param lsThisList ��һ��list
	 * @param lsCmpList �ڶ���list
	 * @param min2max ����������Ƿ��С����true��list��Ԫ�ش�С��������
	 * false��list��Ԫ�شӴ�С����
	 * @return
	 * list<br>
	 * |<br>
	 * |------ListlsElement|---listCell1��Element1,Element2<br>
	 * |                          |---listCell2��Element3<br>
	 * |             <br>
	 * |             <br>
	 * |<br>
	 * |<br>
	 * |<br>d
	 */
	public static<T extends CompSubArray> ArrayList<CmpListCluster<T>> compList(boolean min2max, ArrayList<T>... lsThisList)
	{
		ArrayList<CmpListCluster<T>> lsCompResult = new ArrayList<CmpListCluster<T>>();
		String flagThis = CmpListCluster.FLAGTHIS; String flagComp = CmpListCluster.FLAGCOMP;
		ArrayList<T> lsTmp = new ArrayList<T>();
		int th = 0;
		int co = 0;
		/////////////////////////////////////   �����������Ԫ�ر�Ǻú󣬻���һ�����һ��list��  ////////////////////////////////////////////
		/////////////////////////////////////   Ŀ����Ҫ��þ��������һ��list     //////////////////////////////////////////////////////////////////////
		while (true) {
			if (th >= lsThisList[0].size() || co >= lsThisList[1].size()) {
				break;
			}
			if (min2max) {
				//��������������Ǿ������򣬲���ǰС����
				//���αȽϱ���ͱȽ����Ԫ�أ�Ȼ��װ��list
				if (lsThisList[0].get(th).getCell()[0] < lsThisList[1].get(co).getCell()[0]) {
					T elementThis = lsThisList[0].get(th); elementThis.setFlag(flagThis);
					lsTmp.add(elementThis);
					th++;
				}
				else {
					T elementThis = lsThisList[1].get(co); elementThis.setFlag(flagComp);
					lsTmp.add(elementThis);
					co++;
				}
			}
			else {
				//��������������Ǿ������򣬲���ǰ���С��
				//���αȽϱ���ͱȽ����Ԫ�أ�Ȼ��װ��list
				if (lsThisList[0].get(th).getCell()[1] > lsThisList[1].get(co).getCell()[1]) {
					T elementThis = lsThisList[0].get(th); elementThis.setFlag(flagThis);
					lsTmp.add(elementThis);
					th++;
				}
				else {
					T elementThis = lsThisList[1].get(co); elementThis.setFlag(flagComp);
					lsTmp.add(elementThis);
					co++;
				}
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (th < lsThisList[0].size()) {
			for (int i = th; i < lsThisList[0].size(); i++) {
				T elementThis = lsThisList[0].get(th); elementThis.setFlag(flagThis);
				lsTmp.add(elementThis);
			}
		}
		if (co < lsThisList[1].size()) {
			for (int i = co; i < lsThisList[1].size(); i++) {
				T elementThis = lsThisList[1].get(co); elementThis.setFlag(flagComp);
				lsTmp.add(elementThis);
			}
		}
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//��Ԫ��װ��list�У����ҳ����飬this��compare----��������CompSubArrayCluster���н��е�
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		CmpListCluster<T> compSubArrayCluster = null;
		for (int i = 0; i < lsTmp.size(); i++) {
			T compSubArray = lsTmp.get(i);
			if (i == 0) {
				compSubArrayCluster = new CmpListCluster<T>();
				compSubArrayCluster.addCompElement(compSubArray);
				lsCompResult.add(compSubArrayCluster);
				continue;
			}
			if (!compSubArrayCluster.addCompElement(compSubArray)) {
				compSubArrayCluster = new CmpListCluster<T>();
				compSubArrayCluster.addCompElement(compSubArray);
				lsCompResult.add(compSubArrayCluster);
			}
		}
		return lsCompResult;
	}
	/**
	 * ���ַ�����Coordinate�����,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���<br>
	 * 3: ����һ���ڵ�ľ��룬����������Ϊ-1<br>
	 * 4������һ���ڵ�ľ��룬������յ���Ϊ-1<br>
	 */
	private<T extends CompSubArray> double[] LocPositionT(ArrayList<T> lsTarget, double Coordinate) {
		ArrayList<double[]> lsTmp = new ArrayList<double[]>();
		for (T t : lsTarget) {
			lsTmp.add(t.getCell());
		}
		return LocPosition(lsTmp, Coordinate);
	}
	
		
	
		
	
	
	/**
	 * �����
	 * ���ַ�����Coordinate�����,Ҳ��static�ġ��Ѿ��������ڵ�һ��Item֮ǰ���������û���������һ��Item������<br>
	 * ����һ��int[3]���飬<br>
	 * 0: 1-������ 2-������<br>
	 * 1����������ţ���λ�ڻ����ڣ� / �ϸ���������(��λ�ڻ�����) -1��ʾǰ��û�л���<br>
	 * 2���¸��������� -1��ʾ����û�л���
	 * 3: ����һ���ڵ�ľ��룬����������Ϊ-1
	 * 4������һ���ڵ�ľ��룬������յ���Ϊ-1
	 */
	private double[] LocPosition(ArrayList<double[]> lsTarget, double Coordinate) {
		double[] LocInfo = new double[3];
		int endnum = lsTarget.size() - 1;
		int beginnum = 0;
		int number = 0;
		// �ڵ�һ��Item֮ǰ
		if (Coordinate < lsTarget.get(beginnum)[0]) {
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			LocInfo[3] = -1;
			LocInfo[4] = lsTarget.get(beginnum)[0] - Coordinate;
			return LocInfo;
		}
		// �����һ��Item֮��
		else if (Coordinate > lsTarget.get(endnum)[1]) {
			LocInfo[1] = endnum;
			LocInfo[2] = -1;
			LocInfo[0] = 2;
			LocInfo[3] = Coordinate - lsTarget.get(endnum)[1];
			LocInfo[4] = -1;
			return LocInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsTarget.get(number)[0]) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < lsTarget.get(number)[0]
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		LocInfo[1] = beginnum;
		LocInfo[2] = endnum;
		if (Coordinate <= lsTarget.get(beginnum)[1])// ��֪���᲻�����PeakNumber��biginnumС�����
		{ // location�ڻ����ڲ�
			LocInfo[0] = 1;
			LocInfo[3] = Coordinate - lsTarget.get(beginnum)[0];
			LocInfo[4] = lsTarget.get(beginnum)[1] - Coordinate;
			return LocInfo;
		}
		// location�ڻ����ⲿ
		LocInfo[0] = 2;
		return LocInfo;
	}
	
	
	/**
	 * �Ƚ���������֮���overlap����ֵ�ͱ���
	 * �������ֻ������ֵ�������Ǳ�����
		 * �����Ϣ��
		 * 0��λ�����,�ܹ�6������� 0��һ�� 1������1��ǰ  2������2��ǰ <br>
		 * 1��overlap��bp<br>
		 * 2��overlapռ1�ı�ֵ<br>
		 * 3��overlapռ2�ı�ֵ<br>
		 */
	public static double[] cmpArray(double[] region1, double[] region2) {
		/**
		 * �����Ϣ��
		 * 0��λ�����,�ܹ�6������� 0��һ�� 1������1��ǰ  2������2��ǰ
		 * 1��overlap��bp
		 * 2��overlapռ1�ı�ֵ
		 * 3��overlapռ2�ı�ֵ
		 */
		double[] result = new double[4];

		double[] region1m = new double[2];
		region1m[0] = Math.min(region1[0], region1[1]);
		region1m[1] = Math.max(region1[0], region1[1]);
		double lenReg1 = region1m[1] - region1m[0] + 1;
		
		
		double[] region2m = new double[2];
		region2m[0] = Math.min(region2[0], region2[1]);
		region2m[1] = Math.max(region2[0], region2[1]);
		double lenReg2 = region2m[1] - region2m[0] + 1;
		//equal
		//   |--------|
		//   |--------|
		if (region1m[0] == region2m[0] && region1m[1] == region2m[1]) {
			result[0] = 0;
			result[1] = region1m[1] - region2m[0] + 1;
			result[2] = 1;
			result[3] = 1;
		}
		//overlap
		else if (region1m[0] <= region2m[0] && region1m[1] > region2m[0]) {
			//      0---------1   region2m               2
			//  0-------1         region1m 
			if (region1m[1] <= region2m[1]) {
				result[0] = 2;
				result[1] = region1m[1] - region2m[0] + 1;
				result[2] = result[1]/lenReg1;
				result[3] = result[1]/lenReg2;
			}
			//     |----------|       region2m            4
			//  |-----------------|   region1m
			else {
				result[0] = 4;
				result[1] = lenReg2;
				result[2] = lenReg2/lenReg1;
				result[3] = 1;
			}
		}
		else if (region1m[0] > region2m[0] && region1m[0] < region2m[1]) {
			//   |---------------|   region2m               3
			//        |-------|      region1m
			if (region1m[1] <= region2m[1]) {
				result[0] = 3;
				result[1] = lenReg1;
				result[2] = 1;
				result[3] = lenReg1/lenReg2;
			}
			//   0---------1           region2m            5
			//        0----------1     region1m
			else {
				result[0] = 5;
				result[1] = region2m[1] - region1m[0] + 1;
				result[2] = result[1]/lenReg1;
				result[3] = result[1]/lenReg2;
			}
		}
		//before
		//                   |------|   region2m             1
		//         |------|             region1m
		else if (region1m[1] <= region2m[0]) {
			result[0] = 1;
			result[1] = 0;
			result[2] = 0;
			result[3] = 0;
		}
		//after 
		//       |------|             region2m             6
		//                 |------|   region1m
		else if (region1m[0] >= region2m[1] ) {
			result[0] = 6;
			result[1] = 0;
			result[2] = 0;
			result[3] = 0;
		}
		else {
			logger.error("����δ֪���󣬲����ܴ��ڵ�region������"+ region1m[0] + " " +region1m[1 ] + "     " + region2m[0] + " "+ region2m[1]);
			result[0] = -1;
			result[1] = -1;
			result[2] = -1;
			result[3] = -1;
		}
	
		return result;
	}

	/**
	 * �Ƚϵ����ݺ�cmpArrayһģһ����ֻ�����Ƚϵ�ʱ�򽫴�С����������
	 * �����ǰС���ں�
	 * �Ƚ���������֮���overlap����ֵ�ͱ���
	 * �������ֻ������ֵ
	 * 
	 */
	public static double[] cmpArrayTrans(double[] region1, double[] region2) {
		/**
		 * �����Ϣ��
		 * 0��λ������� 0��һ�� 1������1��ǰ  2������2��ǰ
		 * 1��overlap��bp
		 * 2��overlapռ1�ı�ֵ
		 * 3��overlapռ2�ı�ֵ
		 * 4��
		 */
		double[] result = new double[3];

		double[] region1m = new double[2];
		region1m[0] = Math.max(region1[0], region1[1]);
		region1m[1] = Math.min(region1[0], region1[1]);
		double lenReg1 = region1m[0] - region1m[1] + 1;
		double[] region2m = new double[2];
		region2m[0] = Math.max(region2[0], region2[1]);
		region2m[1] = Math.min(region2[0], region2[1]);
		double lenReg2 = region2m[0] - region2m[1] + 1;
		//equal
		//   |--------|
		//   |--------|
		if (region1m[0] == region2m[0] && region1m[1] == region2m[1]) {
			result[0] = 0;
		}
		//overlap
		else if (region1m[1] <= region2m[0] && region1m[0] > region2m[0]) {
			//  1----------0             region2m
			//         1--------0        region1m 
			if (region1m[1] >= region2m[1]) {
				result[0] = 2;
				result[1] = region2m[0] - region1m[1] + 1;
				result[2] = result[1]/lenReg1;
				result[3] = result[1]/lenReg2;
			}
			//     1----------0       region2m
			//  1------------------0   region1m
			else {
				result[0] = 4;
				result[1] = lenReg2;
				result[2] = lenReg2/lenReg1;
				result[3] = 1;
			}
		}
		else if (region1m[0] < region2m[0] && region1m[0] > region2m[1]) {
			//   1---------------0  region2m
			//        1-------0     region1m
			if (region1m[1] >= region2m[1]) {
				result[0] = 3;
				result[1] = lenReg1;
				result[2] = 1;
				result[3] = lenReg1/lenReg2;
			}
			//            1---------0  region2m
			//       1----------0     region1m
			else {
				result[0] = 5;
				result[1] = region1m[0] - region2m[1] + 1;
				result[2] = result[1]/lenReg1;
				result[3] = result[1]/lenReg2;
			}
		}
		//before
		//     1------0              region2m
		//                1------0    region1m
		else if (region1m[1] >= region2m[0]) {
			result[0] = 1;
			result[1] = 0;
			result[2] = 0;
			result[3] = 0;
		}
		//after
		//                  1------0       region2m
		//        1------0                 region1m
		else if (region1m[0] <= region2m[1] ) {
			result[0] = 6;
			result[1] = 0;
			result[2] = 0;
			result[3] = 0;
		}
		else {
			logger.error("����δ֪���󣬲����ܴ��ڵ�region������"+ region1m[0] + " " +region1m[1 ] + "     " + region2m[0] + " "+ region2m[1]);
			result[0] = -1;
			result[1] = -1;
			result[2] = -1;
			result[3] = -1;
		}
		return result;
	}
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

