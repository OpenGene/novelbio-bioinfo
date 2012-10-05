package com.novelbio.base.dataStructure;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
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
	public static<T> ArrayList<T> converArray2List(T[] array) {
		ArrayList<T> lsResult = new ArrayList<T>();
		for (T t : array) {
			lsResult.add(t);
		}
		return lsResult;
	}
	public static<T> T[] converList2Array(List<T> ls) {
		@SuppressWarnings("unchecked")
		T[]  result = (T[]) Array.newInstance(ls.get(0).getClass(),ls.size());
		int index = 0;
		for (T t : ls) {
			result[index] = t;
			index++;
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
	
	public static boolean compareString(String str1, String str2) {
		if (str1 == str2) {
			return true;
		}
		else if (str1 == null && str2 != null) {
			return false;
		}
		else if (!str1.equals(str2)) {
			return false;
		}
		return true;
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
	public static<T> LinkedHashSet<T> removeDuplicate(Collection<T> colToverrideHashCode) {
		LinkedHashSet<T> setRemoveDuplicate = new LinkedHashSet<T>();
		for (T t : setRemoveDuplicate) {
			setRemoveDuplicate.add(t);
		}
		return setRemoveDuplicate;
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
	 * <b>û����ӷ�Χ��⹦��</b>
	 * <b>ͬһ�����ֻ�����ɾ����һ��</b><br>
	 * ��ӻ���ɾ�������е�һЩ��Ŀ
	 * @param <T>
	 * @param Aarray ����
	 * @param lsIndelInfo ��Ҫ����С��0���Ҫɾ���ļ����0��ʼ���㣬<br>
	 * 	<b>0��</b> ��ӻ�ɾ����һ��<br>
	 * ��������ӣ�<b>�����ָ��λ�õ�ǰ��</b>��<br>
	 * ������ɾ���������������������<br>
	 * <b>1��</b>��Ӽ���Ԫ�أ���ɾ����Ԫ��<br>
	 * <b>����Ϊ��ӣ���ӿ��Զ������Ϊ���һ��+1ʱ��ֻ����Ӳ���ɾ��<br>����Ϊɾ����ֻ��ɾ��һ��</b>
	 * @param filling Ĭ������Ԫ��
	 * @return
	 */
	public static<T> T[] indelElement(T[] Aarray,ArrayList<int[]> lsIndelInfo, T filling) {
		// 0�������ڼ�λ��1.����ɾ����������ǰ�����һλ
		HashMap<Integer, TreeSet<Integer>> hashIndelInfo = new HashMap<Integer, TreeSet<Integer>>();
		for (int[] i : lsIndelInfo) {
			if (i[0] > Aarray.length || i[0] < 0)
				continue;
			if (hashIndelInfo.containsKey(Math.abs(i[0]))) {
				TreeSet<Integer> lsDetail = hashIndelInfo.get(Math.abs(i[0]));
				lsDetail.add(i[1]);
			}
			else {
				TreeSet<Integer> lsDetail = new TreeSet<Integer>();
				lsDetail.add(i[1]);
				hashIndelInfo.put(Math.abs(i[0]), lsDetail);
			}
		}
		///////// �����������鳤�� //////////////////
		int finalLen = Aarray.length;
		for (TreeSet<Integer> treeSet : hashIndelInfo.values()) {
			for (Integer integer : treeSet) {
				if (integer < 0)
					finalLen --;//������ʾ������λ��ɾ��
				else
					finalLen = finalLen + integer;//������ʾ�ڸ�λ��֮ǰ������ɸ���λ
			}
		}
		T[] result=(T[]) Array.newInstance(Aarray.getClass().getComponentType(), finalLen);//new T[Astring.length+Bstring.length];
		int resultNum = 0;//���array������
		for (int i = 0; i < Aarray.length; i++) {
			boolean flagDel = false;//�Ƿ�������ID
			if (hashIndelInfo.containsKey(i)) {
				//�������У�Ҳ���ǴӴ�С����
				NavigableSet<Integer> treeIndelInfo = hashIndelInfo.get(i).descendingSet();
				for (Integer integer : treeIndelInfo) {
					if (integer > 0) {
						resultNum = resultNum + integer;
					}
					else {
						flagDel = true;
					}
				}
			}
			//���û������
			if (!flagDel) {
				result[resultNum] = Aarray[i];
				resultNum++ ;
			}
		}
		for (int i = 0; i < result.length; i++) {
			if (result[i] == null) {
				result[i] = filling;
			}
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
	private static ArrayList<String[]> listCope(ArrayList<String[]> ls, int[] colNum) {
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
	public static void convertArray(int[] array) {
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
	public static void convertArray(double[] array) {
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
	public static<T> void convertArray(T[] array) {
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
	public static<K,V> ArrayList<K> getArrayListKey(Map<K, V> hashMap) {
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
	 * 	 * û�з��� �յ� list
	 */
	public static<K,V> ArrayList<V> getArrayListValue(Map<K, V> hashMap) {
		if (hashMap == null || hashMap.size() == 0) {
			return new ArrayList<V>();
		}
		ArrayList<V> lsResult = new ArrayList<V>();
		Collection<V> values = hashMap.values();
		for(V value:values) {
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
	public static<K> ArrayList<K> getArrayListValue(Set<K> hashset) {
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
	 * using {@link #indelElement(Object[], int[])} replace<br>
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

