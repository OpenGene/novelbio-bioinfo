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
	 * 合并字符串数组
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
	 * 给定lsString，将lsString看作ArrayList-String[]，纵向将其合并为String[][]，也就是类似cbind
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
	 * 二维[][]的合并，给定AT[][]和BT[][]
	 * 将AT[][]和BT[][]合并，可以指定BT插入在AT的哪一列中
	 * <b>注意，AT和BT的类型必须相等，行数必须相等,也就是第一维必须相等</b>
	 * @param AT
	 * @param BT
	 * @param instNum 插入到AT的第几列之后，是AT的实际列,如果instNum<1则仅仅将BT并到AT的后面。
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
		//通过反射的方法新建数组
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
	 * 二维[][]的合并，给定AT[][]和BT[][]
	 * 将AT[][]和BT[][]合并，可以指定BT插入在AT的哪一列中
	 * <b>注意，AT和BT的类型必须相等，行数必须相等,也就是第一维必须相等</b>
	 * @param AT
	 * @param BT
	 * @param instNum 插入到AT的第几列之后，是AT的实际列,如果instNum<1则仅仅将BT并到AT的后面。
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
		//通过反射的方法新建数组
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
	 * 采用{@link combArray}取代
	 * String[][]的合并，给定Astring[][]和Bstring[][]
	 * 将Aobject[][]和Bobject[][]合并，可以指定Bobject插入在Aobject的哪一列后
	 * <b>注意，Aobject和Bobject的类型必须相等，行数必须相等,也就是第一维必须相等</b>
	 * @param Aobject
	 * @param Bobject
	 * @param instNum 插入到Aobject的第几列之后，是Aobject的实际列,如果instNum<1则仅仅将Bobject并到Aobject的后面。
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
	 * String[]的合并，给定Astring[]和Bstring[]
	 * 将Astring[]和Bstring[]合并，可以指定Bstring插入在Astring的哪一列后
	 * <b>注意，Astring和Bstring的类型必须相等</b>
	 * @param Astring
	 * @param Bstring
	 * @param instNum 插入到Astring的第几列之后，是Astring的实际列,如果instNum<1则仅仅将Bobject并到Aobject的后面。
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
	 * 删除数组中的一些项目
	 * @param <T>
	 * @param Aarray 数组
	 * @param deletNum 需要删除哪几项，从0开始计算，如果超出数组项，则忽略
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
	 * 用hash的方法来合并两个List<br>
	 * 给定lsA、lsB<br>
	 * 其中lsA的第AcolNum列没有重复（从0开始计算）<br>
	 * 将lsA的第AcolNum（从0开始计算）和lsB的第BcolNum（从0开始计算）进行比较<br>
	 * 如果相同，则将AcolNum全部添加到lsB后面，最后返回添加好的lsA<br>
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
	 * 用之前要看清楚指定的column是否在ls内 <br>
	 * 给定List，获得其中指定的某几列,获得的某几列按照指定的列进行排列,<b>从0开始计数</b><br>
	 * 或者去除指定的某几列<br>
	 * 用一个boolean参数来指定<br>
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
			int k=0;//给最后结果计数，也就是需要哪几列
			//遍历所有列数
			for (int i = 0; i < ls.get(0).length; i++) {
				//如果该列在去除项中，则跳过
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
	 * 给定List，获得其中指定的某几列,获得的某几列按照指定的列进行排列，从0开始记数<br>
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
	 * 两个list取交集,注意lsA和lsB里面不要有重复项
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
	 * 给定一个数组，以及它的中点坐标，和上游下游坐标，切割或者扩充该数组
	 * @param array 数组
	 * @param center 中心位置，譬如2的话，就是该数组的第二位
	 * @param up 中心上面的元素个数
	 * @param down 中心下面的元素个数
	 * @param thisdefault 默认值，就是没有的地方用什么填充
	 * @return
	 * 最后返回长度为 up+1+down的array
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
	 * 颠倒数组，直接性将传入的数组倒置，不返回东西
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
	 * 颠倒数组，直接性将传入的数组倒置，不返回东西
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
	 * 颠倒数组，直接性将传入的数组倒置，不返回东西
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
	 * 将hashmap的key提取出来
	 * @param <K> key
	 * @param <V> value
	 * @param hashMap
	 * 	 * 没有返回null
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
	 * 将hashmap的value提取出来
	 * @param <K> key
	 * @param <V> value
	 * @param hashMap
	 * 	 * 没有返回null
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
	 * 将hashset的value提取出来
	 * @param <K> key
	 * @param hashset
	 * 没有返回一个空的arraylist
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
	 * 复制数组
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static<T> T[] copyArray(T[] array) {
		return copyArray(array, array.length);
	}
	/**
	 * 复制数组
	 * @param <T>
	 * @param array
	 * @param Length 将array的Length位复制给结果array，如果length > array.length，则延长结果array
	 * @return
	 * 最后生成Length长度的array
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
	 * 复制数组
	 * @param <T>
	 * @param array
	 * @param Length 将array的Length位复制给结果array，如果Length > array.length，则延长结果array
	 * @param 最后复制靠前还是靠后，靠前 infoXXX，靠后XXXinfo
	 * @return
	 * 最后生成Length长度的array
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
	 * @param <T> 实现compSubArray接口
	 * @param lsThisList 第一个list
	 * @param lsCmpList 第二个list
	 * @param cis 是否正向的比，true：list的元素从小到大排列且每个cell小的在前大的在后
	 * false：list的元素从大到小排列且每个cell大的在前小的在后
	 * @return
	 * list<br>
	 * |<br>
	 * |------ListlsElement|---listCell1：Element1,Element2<br>
	 * |                          |---listCell2：Element3<br>
	 * |             <br>
	 * |             <br>
	 * |<br>
	 * |<br>
	 * |<br>
	 */
	public<T extends CompSubArray> ArrayList<ArrayList<ArrayList<T>>> cmpLsaaa(ArrayList<T> lsThisList, ArrayList<T> lsCmpList, boolean cis)
	{
		ArrayList<ArrayList<ArrayList<T>>> lsResult = new ArrayList<ArrayList<ArrayList<T>>>();
		int score = 0; // 打分，看最后这两个转录本有多相似
		boolean flag1 = true;// 是否记录跨过的exon1
		boolean flag2 = true;// 是否记录跨过的exon2
		int i = 0;
		int j = 0;
		//每个单元的信息
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
				//两个element一样大小，则添加新的单元
				lsElement = new ArrayList<ArrayList<T>>();
				lsThisCell.add(lsThisList.get(i));
				lsCmpCell.add(lsCmpList.get(j));
				lsElement.add(lsThisCell); lsElement.add(lsCmpCell);
				lsResult.add(lsElement);
				lsThisCell = new ArrayList<T>();//新建cell
				lsCmpCell = new ArrayList<T>();//新建cell
				i++;j++;
			}
			//
			else if (tmpFlag[0] < 4) //element1 的 尾部 在 element2 的 尾部 前
			{
				i++;
				if (tmpFlag[0] != 1) {
					flag2 = false;
					if (lsThisCell.size() > 0) {
						//如果已经有了该element，就跳过
						double[] tmpThisLast =  lsThisCell.get(lsThisCell.size()-1).getCell(); // 获得最后一个cell
						double[] tmpThis = lsThisList.get(i).getCell();
						if (tmpThisLast[0] != tmpThis[0] || tmpThisLast[1] != tmpThis[1]) {
							lsThisCell.add(lsThisList.get(i));
						}
						//如果已经有了该element，就跳过
						double[] tmpCmpLast =  lsCmpCell.get(lsCmpCell.size()-1).getCell(); // 获得最后一个cell
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
						lsThisCell = new ArrayList<T>();//新建cell
						lsCmpCell = new ArrayList<T>();//新建cell
						lsCmpCell.add(lsCmpList.get(j));
						score = score + 1;
					} else {
						flag1 = true; // 说明该element需要另起一组新的了
						lsThisCell = new ArrayList<T>();//新建cell
						lsCmpCell = new ArrayList<T>();//新建cell
					}
				}
			} else if (tmpFlag[0] >= 4) {
				j++;
				// 跨过了该exon
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
	 * 目前只能最多输入两个arraylist
	 * @param <T> 实现compSubArray接口
	 * @param lsThisList 第一个list
	 * @param lsCmpList 第二个list
	 * @param min2max 输入的数据是否从小到大，true：list的元素从小到大排列
	 * false：list的元素从大到小排列
	 * @return
	 * list<br>
	 * |<br>
	 * |------ListlsElement|---listCell1：Element1,Element2<br>
	 * |                          |---listCell2：Element3<br>
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
		/////////////////////////////////////   将输入的数组元素标记好后，混在一起放入一个list中  ////////////////////////////////////////////
		/////////////////////////////////////   目的是要获得经过排序的一个list     //////////////////////////////////////////////////////////////////////
		while (true) {
			if (th >= lsThisList[0].size() || co >= lsThisList[1].size()) {
				break;
			}
			if (min2max) {
				//假设输入的数组是经过排序，并且前小后大的
				//依次比较本组和比较组的元素，然后装入list
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
				//假设输入的数组是经过排序，并且前大后小的
				//依次比较本组和比较组的元素，然后装入list
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
		//将元素装入list中，并且成两组，this和compare----分组是在CompSubArrayCluster类中进行的
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
	 * 二分法查找Coordinate的情况,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因<br>
	 * 3: 和上一个节点的距离，如果在起点则为-1<br>
	 * 4：和下一个节点的距离，如果在终点则为-1<br>
	 */
	private<T extends CompSubArray> double[] LocPositionT(ArrayList<T> lsTarget, double Coordinate) {
		ArrayList<double[]> lsTmp = new ArrayList<double[]>();
		for (T t : lsTarget) {
			lsTmp.add(t.getCell());
		}
		return LocPosition(lsTmp, Coordinate);
	}
	
		
	
		
	
	
	/**
	 * 待检查
	 * 二分法查找Coordinate的情况,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3: 和上一个节点的距离，如果在起点则为-1
	 * 4：和下一个节点的距离，如果在终点则为-1
	 */
	private double[] LocPosition(ArrayList<double[]> lsTarget, double Coordinate) {
		double[] LocInfo = new double[3];
		int endnum = lsTarget.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < lsTarget.get(beginnum)[0]) {
			LocInfo[0] = 2;
			LocInfo[1] = -1;
			LocInfo[2] = 0;
			LocInfo[3] = -1;
			LocInfo[4] = lsTarget.get(beginnum)[0] - Coordinate;
			return LocInfo;
		}
		// 在最后一个Item之后
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
		if (Coordinate <= lsTarget.get(beginnum)[1])// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			LocInfo[0] = 1;
			LocInfo[3] = Coordinate - lsTarget.get(beginnum)[0];
			LocInfo[4] = lsTarget.get(beginnum)[1] - Coordinate;
			return LocInfo;
		}
		// location在基因外部
		LocInfo[0] = 2;
		return LocInfo;
	}
	
	
	/**
	 * 比较两个区域之间的overlap的数值和比例
	 * 数组必须只有两个值，并且是闭区间
		 * 结果信息：
		 * 0：位置情况,总共6种情况， 0：一致 1：数组1在前  2：数组2在前 <br>
		 * 1：overlap的bp<br>
		 * 2：overlap占1的比值<br>
		 * 3：overlap占2的比值<br>
		 */
	public static double[] cmpArray(double[] region1, double[] region2) {
		/**
		 * 结果信息：
		 * 0：位置情况,总共6种情况， 0：一致 1：数组1在前  2：数组2在前
		 * 1：overlap的bp
		 * 2：overlap占1的比值
		 * 3：overlap占2的比值
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
			logger.error("出现未知错误，不可能存在的region特征："+ region1m[0] + " " +region1m[1 ] + "     " + region2m[0] + " "+ region2m[1]);
			result[0] = -1;
			result[1] = -1;
			result[2] = -1;
			result[3] = -1;
		}
	
		return result;
	}

	/**
	 * 比较的内容和cmpArray一模一样，只不过比较的时候将大小反过来而已
	 * 大的在前小的在后
	 * 比较两个区域之间的overlap的数值和比例
	 * 数组必须只有两个值
	 * 
	 */
	public static double[] cmpArrayTrans(double[] region1, double[] region2) {
		/**
		 * 结果信息：
		 * 0：位置情况， 0：一致 1：数组1在前  2：数组2在前
		 * 1：overlap的bp
		 * 2：overlap占1的比值
		 * 3：overlap占2的比值
		 * 4：
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
			logger.error("出现未知错误，不可能存在的region特征："+ region1m[0] + " " +region1m[1 ] + "     " + region2m[0] + " "+ region2m[1]);
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

