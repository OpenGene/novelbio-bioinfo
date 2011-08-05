package com.novelbio.base.dataStructure;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class ArrayOperate {
	
	
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
				System.out.println(tmpKeyB);
				continue;
			}
			String[] tmpResult = combArray(strings, tmpA, 0);
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	

	/**
	 * ��֮ǰҪ�����ָ����column�Ƿ���ls�� <br>
	 * ����List���������ָ����ĳ����,��õ�ĳ���а���ָ�����н�������,��0��ʼ����<br>
	 * ����ȥ��ָ����ĳ����<br>
	 * ��һ��boolean������ָ��<br>
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
	
}
