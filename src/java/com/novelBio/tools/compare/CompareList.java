package com.novelBio.tools.compare;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fileOperate.FileOperate;
 





public class CompareList 
{
	
	String AssList1="AssList1";
	String AssList2="AssList2";
	String CASE="CASE";
	String List1="List1";
	String List2="List2";
	String NR_FIELDS1="NR_FIELDS1";
	String NR_FIELDS2="NR_FIELDS2"; 
	String POPULATION="POPULATION";
	String UPLOAD_LIST1="UPLOAD_LIST1";
	String UPLOAD_LIST2="UPLOAD_LIST2";
	 
	
	String input="input";
	String output="output";
	String sep="\t";
	
	String union="union";
	String list2_only="Elements only in List2";
	String list1_only="Elements only in List1";
	String intersection="Intersection of lists";
	String list1_mult="Multiples in List1";
	String list2_mult="Multiples in List2";
	String listunion="Union of lists";
	String finish="program_finished"; //告诉王丛茂计算完毕
	////////给R用的////////////////////////
	String RpvalueFile="RpvalueCal"; //写入ls1数值，ls2数值，intersection和alldataset，等待让R计算pvalue
	 
	String thisFilePath=null;
	

	//String thisFilePath="/home/zong0jie/桌面/compare_lists_cgi/bin"; 
	String Rscript="CompareListPvalue.R";
	String pvalueResult="Probability values";
 
	int firstlinels1=1;
	int firstlinels2=1;
	
	
	
	
	
	
	
	/**
	 * 设定各个文件的文件名，不设定的话，默认文件名同变量名
	 * @param AssList1
	 * @param AssList2
	 * @param CASE
	 * @param List1
	 * @param List2
	 * @param NR_FIELDS1
	 * @param NR_FIELDS2
	 * @param POPULATION
	 * @param UPLOAD_LIST1
	 * @param UPLOAD_LIST2
	 */
	public void setPara(String AssList1, int  firstlinels,String AssList2,int  firstlinels2,String CASE, String List1, String List2, String NR_FIELDS1, 
			String NR_FIELDS2, String POPULATION,String UPLOAD_LIST1, String UPLOAD_LIST2,String input,String output)
	{
		this.AssList1=AssList1;this.AssList2=AssList2;          
		this.firstlinels1=firstlinels;this.firstlinels2=firstlinels2;
		this.CASE=CASE;        
		this.List1=List1; this.List2=List2;                
		this.NR_FIELDS1=NR_FIELDS1; this.NR_FIELDS2=NR_FIELDS2;      
		this.POPULATION=POPULATION;      
		this.UPLOAD_LIST1=UPLOAD_LIST1; this.UPLOAD_LIST2=UPLOAD_LIST2;  
		this.input=input; this.output=output;
		
	}
	
	/**
	 * 给定两个lst，比较两个lst之间有哪些相同，哪些ls1特有，哪些ls2特有
	 * lst中含有重复项的只记录最后一个项。注意输入前ls需经过去除空格的处理
	 * 6: ls2mult
	 * @param ls1
	 * @param ls2
	 * @return
	 * 最后返回一个arraylist--arraylist--string[] <br>
	 * 包含5个arraylist <br>
	 * 1: ls1only<br>
	 * 2: ls2only<br>
	 * 3: intersection<br>
	 * 4: ls1mult
	 * 5: ls2mult
	 */
	private ArrayList<ArrayList<String[]>> compareList(List<String[]> ls1,List<String[]> ls2,boolean considerCase) 
	{
		Hashtable<String, String[]> hs1=new Hashtable<String, String[]>();
		Hashtable<String, String[]> hs2=new Hashtable<String, String[]>();
		////////返回结果/////////////////////////////////
		ArrayList<String[]> ls1only=new ArrayList<String[]>();
		ArrayList<String[]> ls2only=new ArrayList<String[]>();
		ArrayList<String[]> lsBoth=new ArrayList<String[]>();
		ArrayList<String[]> ls1Mult=new ArrayList<String[]>();
		ArrayList<String[]> ls2Mult=new ArrayList<String[]>();
		
		/////////中间结果///////////////////////////////////
		ArrayList<String> ls1NoDup=new ArrayList<String>();//ls1无重复key
		ArrayList<String> ls2NoDup=new ArrayList<String>();//ls2无重复key
		ArrayList<String> ls1MultKey=new ArrayList<String>();
		ArrayList<String> ls2MultKey=new ArrayList<String>();

		
		
		for (int i = 0; i < ls1.size(); i++) {
			String tmpKey="";
			if (!considerCase) {
				 tmpKey=ls1.get(i)[0].toLowerCase();
			}
			else {
				tmpKey=ls1.get(i)[0];
			}
			String[] tmpValue=ls1.get(i);
			if (hs1.containsKey(tmpKey))
			{
				if(!ls1MultKey.contains(tmpKey)) 
					ls1MultKey.add(tmpKey);
			}
			else {
				ls1NoDup.add(tmpKey);
			}
			hs1.put(tmpKey, tmpValue);
		}
		
		for (int i = 0; i < ls2.size(); i++) {
			String tmpKey="";
			if (!considerCase) {
				 tmpKey=ls2.get(i)[0].toLowerCase();
			}
			else {
				tmpKey=ls2.get(i)[0];
			}
			String[] tmpValue=ls2.get(i);
			if (hs2.containsKey(tmpKey))
			{
				if(!ls2MultKey.contains(tmpKey)) 
					ls2MultKey.add(tmpKey);
			}
			else {
				ls2NoDup.add(tmpKey);
			}
			hs2.put(tmpKey, tmpValue);
		}
		
		for (int i = 0; i < ls1NoDup.size(); i++)
		{ 
			String[] tmpValue2;
			String[] tmpValue1;
			//共有
			String tmpls1key=ls1NoDup.get(i);
			if ((tmpValue2=hs2.get(tmpls1key))!=null) 
			{
				tmpValue1=hs1.get(tmpls1key);
				String[] tmpValue=new String[tmpValue1.length+tmpValue2.length]; 
				for (int j = 0; j < tmpValue.length-1; j++)
				{
					if (j<tmpValue1.length) 
						tmpValue[j]=tmpValue1[j];
					else 
						tmpValue[j]=tmpValue2[j+1-tmpValue1.length];
				}
				lsBoth.add(tmpValue);
			}
			else //仅ls1含有
			{
				ls1only.add(hs1.get(tmpls1key));
			}
		}
		
		for (int i = 0; i < ls2NoDup.size(); i++)
		{
			//共有
			String tmpls2key=ls2NoDup.get(i);
			if (hs1.get(tmpls2key)==null) 
			{
				ls2only.add(hs2.get(tmpls2key));
			}
		}
		
		for (int i = 0; i < ls1MultKey.size(); i++) {
			ls1Mult.add(hs1.get(ls1MultKey.get(i)));
		}
		for (int i = 0; i < ls2MultKey.size(); i++) {
			ls2Mult.add(hs2.get(ls2MultKey.get(i)));
		}

		ArrayList<ArrayList<String[]>> result=new ArrayList<ArrayList<String[]>>();
		result.add(ls1only);
		result.add(ls2only);
		result.add(lsBoth);
		result.add(ls1Mult);
		result.add(ls2Mult);
		return result;
		
	}
	
	
	
	public void getFileToList() throws Exception 
	{
		thisFilePath="/media/winD/Zong jie/桌面/compare_lists_cgi/bin/";
		//thisFilePath = CompareList.class.getResource("/").toURI().getPath();
	 
		//////////////////////////////////输入文件/////////////////////////////////
		String inputFilePath=thisFilePath+"../"+input+"/";
		//String inputFilePath="/home/zong0jie/桌面/compare_lists_cgi/"+input+"/";
		TxtReadandWrite txt=new TxtReadandWrite();
		String upls1File=inputFilePath+UPLOAD_LIST1;
		String upls2File=inputFilePath+UPLOAD_LIST2;
		String nrfileds1=inputFilePath+NR_FIELDS1;
		String nrfields2=inputFilePath+NR_FIELDS2;
		String lst1=inputFilePath+List1;
		String lst2=inputFilePath+List2;
		String asslst1=inputFilePath+AssList1;
		String asslst2=inputFilePath+AssList2;
		String  Case=inputFilePath+CASE;
		String population=inputFilePath+POPULATION;
	   ////////////////////输出文件///////////////////////////////////////
		String outpuFilePath=thisFilePath+"../"+output+"/";
		//String outpuFilePath="/home/zong0jie/桌面/compare_lists_cgi/"+output+"/";
		String lst1Only=outpuFilePath+list1_only;
		String lst2Only=outpuFilePath+list2_only;
		String interSection=outpuFilePath+intersection;
		String lst1Mult=outpuFilePath+list1_mult;
		String lst2Mult=outpuFilePath+list2_mult;
		String lstUnion=outpuFilePath+listunion;
		String rpvalue=outpuFilePath+RpvalueFile;
		String finish=outpuFilePath+this.finish;
 /////////////////////////////////////////////////////////////////////////////////
		
 
		boolean considerCase=false;
		int datasetNum=0;
		if (FileOperate.isFileExist(Case)) {

			txt.setParameter(Case, true);
			BufferedReader reader=txt.readfile();
			String tmpCase=reader.readLine();
			if (tmpCase.trim().equals("")) 
				considerCase=false;
			else 
				considerCase=true;
		}
		if (FileOperate.isFileExist(population)) {
			txt.setParameter(population, true);
			BufferedReader reader=txt.readfile();
			String tmpPop=reader.readLine();
			if (tmpPop.trim().equals("")) 
				datasetNum=0;
			else
			{
				try 
				{
					datasetNum=Integer.parseInt(tmpPop);
				} catch (Exception e) 
				{
					datasetNum=0;
				}
				if (datasetNum<0) {
					datasetNum=0;
				}
			}
		}
		
		
		
		int nrf1=0;int nrf2=0;
		if (FileOperate.isFileExist(nrfileds1)) {
			txt.setParameter(nrfileds1, true);
			BufferedReader reader=txt.readfile();
			String tmpnrf1=reader.readLine();
			if (tmpnrf1.trim().equals("")) 
				nrf1=0;
			else
			{
				try 
				{
					nrf1=Integer.parseInt(tmpnrf1);
				} catch (Exception e) 
				{
					nrf1=0;
				}
				if (nrf1<0) {
					nrf1=0;
				}
			}
		}
		if (FileOperate.isFileExist(nrfields2)) {
			txt.setParameter(nrfields2, true);
			BufferedReader reader=txt.readfile();
			String tmpnrf2=reader.readLine();
			if (tmpnrf2.trim().equals("")) 
				nrf2=0;
			else
			{
				try 
				{
					nrf2=Integer.parseInt(tmpnrf2);
				} catch (Exception e)
				{
					nrf2=0;
					}
				if (nrf2<0) {
					nrf2=0;
				}
			}
			
		}
		
		ArrayList<String[]> ls1=null;ArrayList<String[]> ls2=null;
		if (FileOperate.isFileExist(upls1File)) {
			txt.setParameter(upls1File, true);
			ls1=txt.ExcelRead(sep, firstlinels1, 1, txt.ExcelRows(), 100, 1);//从目标行读取
		}
		else 
		{
			ls1= getList(lst1, asslst1,nrf1,firstlinels1);  
		}
		
		if (FileOperate.isFileExist(upls2File)) {
			txt.setParameter(upls2File, true);
			ls2=txt.ExcelRead(sep, firstlinels2, 1, txt.ExcelRows(), 100, 1);
		}
		else 
		{
			ls2= getList(lst2, asslst2,nrf2,firstlinels2); 
		}
		
		
		
		ArrayList<ArrayList<String[]>> compareResult=compareList(ls1, ls2,considerCase);
		
		ExcelOperate excelOperate=new ExcelOperate();
		excelOperate.newExcelOpen(lst1Only+".xls");
		excelOperate.WriteExcel( 1, 1,compareResult.get(0));
		txt.setParameter(lst1Only, true);
		txt.ExcelWrite(compareResult.get(0), sep, 1, 1);
		
		
		excelOperate.newExcelOpen(lst2Only+".xls");
		excelOperate.WriteExcel( 1, 1,compareResult.get(1));
		txt.setParameter(lst2Only, true);
		txt.ExcelWrite(compareResult.get(1), sep, 1, 1);
		
		excelOperate.newExcelOpen(interSection+".xls");
		excelOperate.WriteExcel( 1, 1,compareResult.get(2));
		txt.setParameter(interSection, true);
		txt.ExcelWrite(compareResult.get(2), sep, 1, 1);
		
		excelOperate.newExcelOpen(lst1Mult+".xls");
		excelOperate.WriteExcel( 1, 1,compareResult.get(3));
		txt.setParameter(lst1Mult, true);
		txt.ExcelWrite(compareResult.get(3), sep, 1, 1);
		
		excelOperate.newExcelOpen(lst2Mult+".xls");
		excelOperate.WriteExcel( 1, 1,compareResult.get(4));
		txt.setParameter(lst2Mult, true);
		txt.ExcelWrite(compareResult.get(4), sep, 1, 1);
		
		excelOperate.newExcelOpen(lstUnion+".xls");
		excelOperate.WriteExcel( 1, 1,"This function is under developing !");
		txt.setParameter(lstUnion, true);
		txt.writefile("This function is under developing !");
		
		
		txt.setParameter(rpvalue, true);
		int[] pvalueInfo=new int[4];
		pvalueInfo[0]=compareResult.get(0).size()+compareResult.get(2).size();//第一个样本数
		pvalueInfo[1]=compareResult.get(1).size()+compareResult.get(2).size();//第二个样本数
		pvalueInfo[2]=compareResult.get(2).size();//交集
		int tmpSum=pvalueInfo[0]+pvalueInfo[1]+pvalueInfo[2];//总和
		if (datasetNum<tmpSum) 
		{
			datasetNum=tmpSum;
		}
		pvalueInfo[3]=datasetNum;
		txt.Rwritefile(pvalueInfo);
		//RcalPvalue();
		
		if (RcalPvalue(thisFilePath)==0) {
			txt.setParameter("error", true);
			txt.writefile("");
			System.out.println("error");
		}
		FileOperate.DeleteFolder(rpvalue);
		txt.setParameter(finish, true);
		txt.writefile("");
	 
	}
	
	/**
	 * 
	 * @param list
	 * @param asslist
	 * @param nrf 0时 获得全部列
	 * @return 返回空值说明有错
	 * @throws Exception
	 */
	private ArrayList<String[]> getList(String list, String asslist,int nrf,int firstlinesls) throws Exception 
	{
		txtReadandWrite txtList=new txtReadandWrite();
		txtList.setParameter(list, true);
		ArrayList<String[]> lsKey=txtList.ExcelRead(sep, firstlinesls, 1, txtList.ExcelRows(), 1, 1);
		txtList.setParameter(asslist, true);
		if(nrf<=0)
			nrf=txtList.ExcelColumns(firstlinesls, sep);
		ArrayList<String[]> lsvalue=txtList.ExcelRead(sep, firstlinesls, 1, txtList.ExcelRows(), nrf, 1);
		if(lsvalue.size()!=0&&lsKey.size()!=lsvalue.size())//这时候会出错
		{
			return null;
		}
		ArrayList<String[]> result=new ArrayList<String[]>();
		for (int i = 0; i < lsKey.size(); i++) 
		{
			String[] tmpResult=new String[1+lsvalue.get(i).length];
			tmpResult[0]=lsKey.get(i)[0];
			for (int j = 1; j < tmpResult.length; j++) {
				tmpResult[j]=lsvalue.get(i)[j-1];
			}
			result.add(tmpResult);
		}
		return result;
	}

	/**
	 * 执行R程序，直到R程序结束再返回
	 * @return
	 * @throws IOException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	private int RcalPvalue(String bin) throws IOException, InterruptedException  
	{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+bin+ "CompareListPvalue.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		return 1;
	}
	
	
}
