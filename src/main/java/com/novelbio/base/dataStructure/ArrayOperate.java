package com.novelbio.base.dataStructure;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class ArrayOperate {
	
	
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
				System.out.println(tmpKeyB);
				continue;
			}
			String[] tmpResult = combArray(strings, tmpA, 0);
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	

	/**
	 * 用之前要看清楚指定的column是否在ls内 <br>
	 * 给定List，获得其中指定的某几列,获得的某几列按照指定的列进行排列,从0开始计数<br>
	 * 或者去除指定的某几列<br>
	 * 用一个boolean参数来指定<br>
	 * @return
	 */
	public static ArrayList<String[]> listCope(ArrayList<String[]> ls, int[] colNum, boolean include) 
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		if (include)
		{
			return listCope(ls, colNum);
		}
		else 
		{
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
		for (String[] strings : ls) 
		{
			String[] tmpString = new String[colNum.length];
			for (int i = 0; i < tmpString.length; i++)
			{
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
	
}
