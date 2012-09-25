package com.novelbio.analysis.seq.genome.motifSearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.novelbio.analysis.seq.genome.GffChrUnion;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.PatternOperate;
/**
 * 全基因组查看motif的类，用于获得某个motif的背景
 * 正则表达式查找某个motif并且获得其位置，考虑生成density图
 * @author zong0jie
 *
 */
public class MotifSearch 
{
	
	int MotifBinNum=10000;//每隔多少bp统计一次motif的数量
	
	GffChrUnion gffChrUnion=new GffChrUnion();
	Hashtable<String, int[]> hashMotifNum=new Hashtable<String, int[]>();
	Hashtable<String, ArrayList<double[]>> hashMotifDensity=new Hashtable<String,  ArrayList<double[]>>();//按比例每条染色体上的Motif密度图
	int motifbpAll=0;
	
	/**
	 * 文件夹最后无所谓加不加"/"或"\\"
	 * @param chrFilePath
	 */
	public void MotifloadChrFile(String chrFilePath)
	{
		gffChrUnion.loadChr(chrFilePath);
	}
	
	/**
	 * 给出motif正则，和全基因组上需要分割的份数
	 * 默认统计每10000bp内motif的数量
	 * @param motifRex
	 * @param maxresolution 最长一条染色体的分辨率
	 * 填满两个hash表
	 * hashMotifNum：chrID--10000bp精度Motif密度
	 * hashMotifDensity: 按比例每条染色体的Motif分布密度
	 */
	public void startMotifSearch(String motifRex,int maxresolution) 
	{
		motifbpAll=0;
		ArrayList<String[]> chrInfo=gffChrUnion.getChrLengthInfo();
		for (int i = 0; i < chrInfo.size(); i++) {
			int[] chrMotifNum=null;//全基因祖上的motif分布情况
			int[] chrResolution=null;//按比例每条染色体相应值下染色体的坐标数组
			String chrID=chrInfo.get(i)[0];
			try {
				chrResolution=gffChrUnion.getChrRes(chrID, maxresolution);
				chrMotifNum=getMoifNum(motifRex, gffChrUnion, chrInfo.get(i)[0],MotifBinNum );//默认统计每MotifBinNum内motif的数量
			} catch (Exception e) {	e.printStackTrace();}
			/////////转换成double类型，方便装入list///////
			double[] dbchrResolution=new double[chrResolution.length];
			for (int j = 0; j < chrResolution.length; j++) {
				dbchrResolution[j]=chrResolution[j];
			}
			//////////////////////////////////////////////////////////////
			motifbpAll=motifbpAll+chrMotifNum[0];
			double[] motifNum=MathComput.mySpline(chrMotifNum, chrResolution.length, 1, 0, 2);//起点偏移一位，就是把第一位忽略掉
			ArrayList<double[]> lsmotifInfo=new ArrayList<double[]>();
			lsmotifInfo.add(dbchrResolution);
			lsmotifInfo.add(motifNum);
			hashMotifNum.put(chrInfo.get(i)[0], chrMotifNum);
			hashMotifDensity.put(chrInfo.get(i)[0],lsmotifInfo);
		}
	}
	
	/**
	 * 运行startMotifSearch方法后才有用
	 * 获得每条染色体上的motif数量精细图，分辨率为10000bp
	 * @return
	 */
	public	Hashtable<String, int[]>  getMotifNum() 
	{
		return hashMotifNum;
	}
	
	
	/**
	 * 运行startMotifSearch方法后才有用
	 * 获得每条染色体上的motif密度图
	 * arraylist中，含有两个double[]
	 * 0: 染色体坐标
	 * 1: 对应坐标motif密度
	 * @return
	 */
	public	Hashtable<String, ArrayList<double[]>> getMotifDensity() 
	{
		return hashMotifDensity;
	}
	
	/**
	 * 运行startMotifSearch方法后才有用
	 * 获得motif的总bp数量
	 */
	public	int getMotifAllNum() 
	{
		return motifbpAll;
	}
	
	
	/**
	 * 给定Motif的正则表达式和间隔区间，返回全基因祖上的motif分布情况
	 * @param motifRex
	 * @param gffChrUnion
	 * @param chrID
	 * @param bpNum 统计多少个bp内的motif数量，为方便起见，该值必须为Chr文本每行碱基数的整数倍，也就是说
	 * 如果Chr文件一行含有50bp，那么该值必须为50的倍数
	 * @return 最后返回int[] 0:所有motif的长度之和，后面的，每个区段内的motif数量
	 * @throws IOException 
	 */
	private int[] getMoifNum(String motifRex,GffChrUnion gffChrUnion,String chrID,int bpNum) throws IOException 
	{
		BufferedReader bufChrseq= gffChrUnion.getBufChrSeq(chrID);
		int chrLineLength=gffChrUnion.getChrLineLength();//每一行含有几个bp
		StringBuilder tmpSeq=new StringBuilder();
		String seqline="";
		String lastSeq="";//上一行的最后一个motif后的序列，如果上一行都没有motif的话，直接就取上一行
		int motifReadLines=bpNum/chrLineLength;//指定的bpNum下，需要读取几行才能达到该数量
		int tmplines=0;//记录已经读取了几行
		
	   long chrlength=gffChrUnion.getChrLength(chrID);
	   int alllines=(int) Math.ceil((double)chrlength/chrLineLength);
	   int motifInv=(int)Math.ceil((double)alllines/motifReadLines);
	   int[] motifNum=new int[motifInv+1];// 0:所有motif的长度之和，后面的，每个区段内的motif数量
		int motifnum=1;
		bufChrseq.readLine();
		while((seqline=bufChrseq.readLine())!=null)
		{
			if(tmplines<motifReadLines)
			{
				tmpSeq.append(seqline.trim());
				tmplines++;
				continue;
			}
			int[] motifInfo=getMotifInfo(tmpSeq.toString(), motifRex);
			if (motifInfo[0]>0&&motifInfo[1]<=chrLineLength) 
			{
				lastSeq=tmpSeq.substring(tmpSeq.length()-motifInfo[1]+1, tmpSeq.length()).trim();
			}
		
			else {
				lastSeq=tmpSeq.substring(tmpSeq.length()-chrLineLength, tmpSeq.length()).trim();
			}
			motifNum[motifnum]=motifInfo[0];//该区域内motif的数量
			motifNum[0]=motifNum[0]+motifInfo[2];
			motifnum++;
			tmpSeq=null;tmpSeq=new StringBuilder();
			tmpSeq.append(lastSeq);
			tmplines=0;
		}
		if(tmplines>0)//如果最后有序列，那么做最后结算
		{
			int[] motifInfo=getMotifInfo(tmpSeq.toString(), motifRex);
			motifNum[motifnum]=motifInfo[0];//该区域内motif的数量
			motifNum[0]=motifNum[0]+motifInfo[2];
			tmpSeq=null;
		}
		////////////////////////确定程序是否出错////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (motifnum!=motifNum.length-1) {
			System.out.println("motifSearch 类的查找motif方法有问题，最后得到的motif分割区域数量和预设置的motif区域数量不相等");
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		return motifNum;
	}
	
	/**
	 * 给定序列，获得里面的motif信息
	 * @param subChrSeq
	 * @param motifReg
	 * @return int[3]
	 * 0: motif数目
	 * 1: 最后一个motif与序列末端的距离
	 * 2: 总的motif长度
	 */
	public int[] getMotifInfo(String subChrSeq,String motifReg)
	{
		int[] motifInfo=new int[3];
		ArrayList<String[]> lsmotif=PatternOperate.getPatLoc(subChrSeq, motifReg, false);
		motifInfo[0]=lsmotif.size();
		if (motifInfo[0]==0) {
			motifInfo[1]=0;
		}
		else {
			motifInfo[1]=Integer.parseInt(lsmotif.get(motifInfo[0]-1)[2]);
		}
		int sumMotifLength=0;
		int motifNum=lsmotif.size();
		for(int i=0;i<motifNum;i++)
		{
			sumMotifLength=sumMotifLength+lsmotif.get(i)[0].length();
		}
		motifInfo[2]=sumMotifLength;
		return motifInfo;
	}
	
	
	
	
	
	
	
}
