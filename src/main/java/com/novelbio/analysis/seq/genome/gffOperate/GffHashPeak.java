package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;



/**
 * 读取那种peak坐标的文件，只输入peak的染色体位置，起点坐标，终点坐标
 * @author zong0jie
 *
 */
public class GffHashPeak extends GffHash{
	/**
     * 最底层读取peak坐标文件的方法，读取生成的peak信息，只读取peak所在Chr列，peak正反向(最好都为正)，peak起点列，peak终点列，并且指定从第几行读起，所有行和列都是实际行和列<br>
     * <b>peak</b> 正反向最好都为正，方便后续处理<br>
     * 输入Gff文件，<b>其中peak可以不按照顺序排列，本类内部会给排序</b>，最后获得两个哈希表和一个list表, 结构如下：<br>
     * <b>1.Chrhash</b><br>
     * （ChrID）--ChrList-- GeneInforList(GffDetail类)
     * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写 chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * （LOCID）--GeneInforlist，其中LOCID代表具体的条目编号,这里不考虑多个重叠的peak： peak起点_peak终点.<br>
     *  <b>3.LOCIDList</b><br>
     * （LOCID）--LOCIDList，按顺序保存Peak,这里不考虑多个重叠的peak，不建议通过其获得某基因的序号,具体情况 :peak起点_peak终点<br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将多个重叠的peak放在一起： peak起点_peak终点/peak起点_peak终点_...<br>
     * @param gfffilename
     * @param peakcis peak是在正链还是负链上，不过没什么用，不用设置，默认为true就好
     * @param colChrID
     * @param colPeakstart
     * @param colPeakend
     * @param rowNum
	 */
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(String gfffilename,boolean peakcis ,int colChrID,int colPeakstart,int colPeakend,int rowNum) throws Exception 
	{		
		TxtReadandWrite txtPeakInfo=new TxtReadandWrite();
		//先把txt文本中的peak信息读取
		int[] colNum=new int[3];
		colNum[0]=colChrID;colNum[1]=colPeakstart;colNum[2]=colPeakend;
		txtPeakInfo.setParameter(gfffilename, false,true);
		String[][] peakInfo=ExcelTxtRead.readtxtExcel(gfffilename, "\t", colNum, rowNum,  txtPeakInfo.ExcelRows());
		/////////装入临时list
		LinkedList<String[]> lstmpPeakinfo=new LinkedList<String[]>();
		for (int i = 0; i < peakInfo.length; i++) {
			lstmpPeakinfo.add(peakInfo[i]);
		}
		////对临时list进行排序,首先按照Chr排序，然后按照具体坐标排序
	     Collections.sort(lstmpPeakinfo,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1) {
	            	int i=arg0[0].compareTo(arg1[0]);
	            	if(i==0){
	            		 if( Integer.parseInt(arg0[1])< Integer.parseInt(arg1[1]))
	 	                {	return -1;}
	 	                else if (Integer.parseInt(arg0[1])== Integer.parseInt(arg1[1])) 
	 	                { 	return 0;}
	 	                else
	 	                {   return 1;}
	            	}
	               return i;
	            }
	        });
		//////////////////////////正式读取，类似GffUCSC的读取方法///////////////////////
	 	//实例化三个表
			locHashtable =new Hashtable<String, GffDetail>();//存储每个LOCID和其具体信息的对照表
			Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//一个哈希表来存储每条染色体
			LOCIDList=new ArrayList<String>();//顺序存储每个peak号，不管是否重叠
			LOCChrHashIDList=new ArrayList<String>();//
			ArrayList<GffDetail> LOCList=null ;//顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
			
			String chrnametmpString="";
			int tmppeakstart=-1;
			int tmppeakend=-1;

			int peakNum=peakInfo.length;
			for (int i = 0; i < peakNum; i++) {
				chrnametmpString=lstmpPeakinfo.get(i)[0];
				tmppeakstart=Integer.parseInt(lstmpPeakinfo.get(i)[1]);
				tmppeakend=Integer.parseInt(lstmpPeakinfo.get(i)[2]);
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//新的染色体
				if (!Chrhash.containsKey(chrnametmpString)) //新的染色体
				{
					if(LOCList!=null)//如果已经存在了LOCList，也就是前一个LOCList，那么先截短，然后将它按照gffGCtmpDetail.numberstart排序
					{
						LOCList.trimToSize();
						 //把peak名称顺序装入LOCIDList
						   for (GffDetail gffDetail : LOCList) {
							   LOCChrHashIDList.add(gffDetail.locString);
						   }
					}
					LOCList=new ArrayList<GffDetail>();//新建一个LOCList并放入Chrhash
					Chrhash.put(chrnametmpString, LOCList);
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				
				//添加重叠peak
				//看本peak的起点是否小于上个peak的终点，如果小于，则说明本peak和上个peak连续
				GffDetail lastGffdetailpeak;
				LOCIDList.add(tmppeakstart+"_"+tmppeakend);//添加入LOCIDList
				if(LOCList.size()>0 && tmppeakstart < (lastGffdetailpeak = LOCList.get(LOCList.size()-1)).numberend )
				{   //修改基因起点和终点
					if(tmppeakstart<lastGffdetailpeak.numberstart)
						lastGffdetailpeak.numberstart=tmppeakstart;
					if(tmppeakend>lastGffdetailpeak.numberend)
						lastGffdetailpeak.numberend=tmppeakend;
  
					//将基因(转录本ID)装入LOCList
				
					
					//将本基因(转录本)的ID装入locString中
					lastGffdetailpeak.locString=lastGffdetailpeak.locString+"/"+tmppeakstart+"_"+tmppeakend;
					//将新值装入locHashtable	

					//将locHashtable中相应的项目也修改，同时加入新的项目
					//因为UCSC里面没有转录本一说，只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
					String[] allPeakID=lastGffdetailpeak.locString.split("/");
					for (int m= 0; m < allPeakID.length; m++) {
						locHashtable.put(allPeakID[m], lastGffdetailpeak);
					}
					continue;
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//添加新peak 
				GffDetail gffdetailpeak=new GffDetail();
				gffdetailpeak.ChrID=chrnametmpString;
				//正反向,所有peak都一个方向的
				gffdetailpeak.cis5to3=peakcis;

				gffdetailpeak.locString=tmppeakstart+"_"+tmppeakend;
				gffdetailpeak.numberstart=tmppeakstart;
				gffdetailpeak.numberend=tmppeakend;
				
				LOCList.add(gffdetailpeak);  
				locHashtable.put(gffdetailpeak.locString, gffdetailpeak);
			}
			LOCList.trimToSize();
			for (GffDetail gffDetail : LOCList) {
				LOCChrHashIDList.add(gffDetail.locString);
			}
			txtPeakInfo.close();
			//System.out.println(mm);
			return Chrhash;

	}

	/**
     * 最底层读取peak坐标文件的方法，读取生成的peak信息，输入peakInfo包括peak所在Chr，peak起点，peak终点<br>
     * 本类内部会给排序</b>，最后获得两个哈希表和一个list表, 结构如下：<br>
     * <b>1.Chrhash</b><br>
     * （ChrID）--ChrList-- GeneInforList(GffDetail类)
     * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写 chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * （LOCID）--GeneInforlist，其中LOCID代表具体的条目编号,这里不考虑多个重叠的peak： peak起点_peak终点.<br>
     *  <b>3.LOCIDList</b><br>
     * （LOCID）--LOCIDList，按顺序保存Peak,这里不考虑多个重叠的peak，不建议通过其获得某基因的序号,具体情况 :peak起点_peak终点<br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将多个重叠的peak放在一起： peak起点_peak终点/peak起点_peak终点_...<br>
     * @param peakInfo 
	 */
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(String[][] peakInfo) throws Exception 
	{		
		/////////装入临时list
		LinkedList<String[]> lstmpPeakinfo=new LinkedList<String[]>();
		for (int i = 0; i < peakInfo.length; i++) {
			lstmpPeakinfo.add(peakInfo[i]);
		}
		////对临时list进行排序,首先按照Chr排序，然后按照具体坐标排序
	     Collections.sort(lstmpPeakinfo,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1) {
	            	int i=arg0[0].compareTo(arg1[0]);
	            	if(i==0){
	            		 if( Integer.parseInt(arg0[1])< Integer.parseInt(arg1[1]))
	 	                {	return -1;}
	 	                else if (Integer.parseInt(arg0[1])== Integer.parseInt(arg1[1])) 
	 	                { 	return 0;}
	 	                else
	 	                {   return 1;}
	            	}
	               return i;
	            }
	        });
		//////////////////////////正式读取，类似GffUCSC的读取方法///////////////////////
	 	//实例化三个表
			locHashtable =new Hashtable<String, GffDetail>();//存储每个LOCID和其具体信息的对照表
			Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//一个哈希表来存储每条染色体
			LOCIDList=new ArrayList<String>();//顺序存储每个peak号，不管是否重叠
			LOCChrHashIDList=new ArrayList<String>();//
			ArrayList<GffDetail> LOCList=null ;//顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
			
			String chrnametmpString="";
			int tmppeakstart=-1;
			int tmppeakend=-1;

			int peakNum=peakInfo.length;
			for (int i = 0; i < peakNum; i++) {
				chrnametmpString=lstmpPeakinfo.get(i)[0];
				tmppeakstart=Integer.parseInt(lstmpPeakinfo.get(i)[1]);
				tmppeakend=Integer.parseInt(lstmpPeakinfo.get(i)[2]);
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//新的染色体
				if (!Chrhash.containsKey(chrnametmpString)) //新的染色体
				{
					if(LOCList!=null)//如果已经存在了LOCList，也就是前一个LOCList，那么先截短，然后将它按照gffGCtmpDetail.numberstart排序
					{
						LOCList.trimToSize();
						 //把peak名称顺序装入LOCIDList
						   for (GffDetail gffDetail : LOCList) {
							   LOCChrHashIDList.add(gffDetail.locString);
						   }
					}
					LOCList=new ArrayList<GffDetail>();//新建一个LOCList并放入Chrhash
					Chrhash.put(chrnametmpString, LOCList);
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				
				//添加重叠peak
				//看本peak的起点是否小于上个peak的终点，如果小于，则说明本peak和上个peak连续
				GffDetail lastGffdetailpeak;
				LOCIDList.add(tmppeakstart+"_"+tmppeakend);//添加入LOCIDList
				if(LOCList.size()>0 && tmppeakstart < (lastGffdetailpeak = LOCList.get(LOCList.size()-1)).numberend )
				{   //修改基因起点和终点
					if(tmppeakstart<lastGffdetailpeak.numberstart)
						lastGffdetailpeak.numberstart=tmppeakstart;
					if(tmppeakend>lastGffdetailpeak.numberend)
						lastGffdetailpeak.numberend=tmppeakend;
  
					//将基因(转录本ID)装入LOCList
				
					
					//将本基因(转录本)的ID装入locString中
					lastGffdetailpeak.locString=lastGffdetailpeak.locString+"/"+tmppeakstart+"_"+tmppeakend;
					//将新值装入locHashtable	

					//将locHashtable中相应的项目也修改，同时加入新的项目
					//因为UCSC里面没有转录本一说，只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
					String[] allPeakID=lastGffdetailpeak.locString.split("/");
					for (int m= 0; m < allPeakID.length; m++) {
						locHashtable.put(allPeakID[m], lastGffdetailpeak);
					}
					continue;
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//添加新peak 
				GffDetail gffdetailpeak=new GffDetail();
				gffdetailpeak.ChrID=chrnametmpString;
				//正反向,所有peak都一个方向的
				gffdetailpeak.cis5to3=true;

				gffdetailpeak.locString=tmppeakstart+"_"+tmppeakend;
				gffdetailpeak.numberstart=tmppeakstart;
				gffdetailpeak.numberend=tmppeakend;
				
				LOCList.add(gffdetailpeak);  
				locHashtable.put(gffdetailpeak.locString, gffdetailpeak);
			}
			LOCList.trimToSize();
			for (GffDetail gffDetail : LOCList) {
				LOCChrHashIDList.add(gffDetail.locString);
			}
			//System.out.println(mm);
			return Chrhash;

	}
	
	
	
	/**
	 * 本类里面该方法无用，直接返回null，用另一个
	 */
	@Override
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(
			String gfffilename) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
}
