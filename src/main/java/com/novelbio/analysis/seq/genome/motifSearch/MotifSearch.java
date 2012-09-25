package com.novelbio.analysis.seq.genome.motifSearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.novelbio.analysis.seq.genome.GffChrUnion;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.PatternOperate;
/**
 * ȫ������鿴motif���࣬���ڻ��ĳ��motif�ı���
 * ������ʽ����ĳ��motif���һ����λ�ã���������densityͼ
 * @author zong0jie
 *
 */
public class MotifSearch 
{
	
	int MotifBinNum=10000;//ÿ������bpͳ��һ��motif������
	
	GffChrUnion gffChrUnion=new GffChrUnion();
	Hashtable<String, int[]> hashMotifNum=new Hashtable<String, int[]>();
	Hashtable<String, ArrayList<double[]>> hashMotifDensity=new Hashtable<String,  ArrayList<double[]>>();//������ÿ��Ⱦɫ���ϵ�Motif�ܶ�ͼ
	int motifbpAll=0;
	
	/**
	 * �ļ����������ν�Ӳ���"/"��"\\"
	 * @param chrFilePath
	 */
	public void MotifloadChrFile(String chrFilePath)
	{
		gffChrUnion.loadChr(chrFilePath);
	}
	
	/**
	 * ����motif���򣬺�ȫ����������Ҫ�ָ�ķ���
	 * Ĭ��ͳ��ÿ10000bp��motif������
	 * @param motifRex
	 * @param maxresolution �һ��Ⱦɫ��ķֱ���
	 * ��������hash��
	 * hashMotifNum��chrID--10000bp����Motif�ܶ�
	 * hashMotifDensity: ������ÿ��Ⱦɫ���Motif�ֲ��ܶ�
	 */
	public void startMotifSearch(String motifRex,int maxresolution) 
	{
		motifbpAll=0;
		ArrayList<String[]> chrInfo=gffChrUnion.getChrLengthInfo();
		for (int i = 0; i < chrInfo.size(); i++) {
			int[] chrMotifNum=null;//ȫ�������ϵ�motif�ֲ����
			int[] chrResolution=null;//������ÿ��Ⱦɫ����Ӧֵ��Ⱦɫ�����������
			String chrID=chrInfo.get(i)[0];
			try {
				chrResolution=gffChrUnion.getChrRes(chrID, maxresolution);
				chrMotifNum=getMoifNum(motifRex, gffChrUnion, chrInfo.get(i)[0],MotifBinNum );//Ĭ��ͳ��ÿMotifBinNum��motif������
			} catch (Exception e) {	e.printStackTrace();}
			/////////ת����double���ͣ�����װ��list///////
			double[] dbchrResolution=new double[chrResolution.length];
			for (int j = 0; j < chrResolution.length; j++) {
				dbchrResolution[j]=chrResolution[j];
			}
			//////////////////////////////////////////////////////////////
			motifbpAll=motifbpAll+chrMotifNum[0];
			double[] motifNum=MathComput.mySpline(chrMotifNum, chrResolution.length, 1, 0, 2);//���ƫ��һλ�����ǰѵ�һλ���Ե�
			ArrayList<double[]> lsmotifInfo=new ArrayList<double[]>();
			lsmotifInfo.add(dbchrResolution);
			lsmotifInfo.add(motifNum);
			hashMotifNum.put(chrInfo.get(i)[0], chrMotifNum);
			hashMotifDensity.put(chrInfo.get(i)[0],lsmotifInfo);
		}
	}
	
	/**
	 * ����startMotifSearch�����������
	 * ���ÿ��Ⱦɫ���ϵ�motif������ϸͼ���ֱ���Ϊ10000bp
	 * @return
	 */
	public	Hashtable<String, int[]>  getMotifNum() 
	{
		return hashMotifNum;
	}
	
	
	/**
	 * ����startMotifSearch�����������
	 * ���ÿ��Ⱦɫ���ϵ�motif�ܶ�ͼ
	 * arraylist�У���������double[]
	 * 0: Ⱦɫ������
	 * 1: ��Ӧ����motif�ܶ�
	 * @return
	 */
	public	Hashtable<String, ArrayList<double[]>> getMotifDensity() 
	{
		return hashMotifDensity;
	}
	
	/**
	 * ����startMotifSearch�����������
	 * ���motif����bp����
	 */
	public	int getMotifAllNum() 
	{
		return motifbpAll;
	}
	
	
	/**
	 * ����Motif��������ʽ�ͼ�����䣬����ȫ�������ϵ�motif�ֲ����
	 * @param motifRex
	 * @param gffChrUnion
	 * @param chrID
	 * @param bpNum ͳ�ƶ��ٸ�bp�ڵ�motif������Ϊ�����������ֵ����ΪChr�ı�ÿ�м��������������Ҳ����˵
	 * ���Chr�ļ�һ�к���50bp����ô��ֵ����Ϊ50�ı���
	 * @return ��󷵻�int[] 0:����motif�ĳ���֮�ͣ�����ģ�ÿ�������ڵ�motif����
	 * @throws IOException 
	 */
	private int[] getMoifNum(String motifRex,GffChrUnion gffChrUnion,String chrID,int bpNum) throws IOException 
	{
		BufferedReader bufChrseq= gffChrUnion.getBufChrSeq(chrID);
		int chrLineLength=gffChrUnion.getChrLineLength();//ÿһ�к��м���bp
		StringBuilder tmpSeq=new StringBuilder();
		String seqline="";
		String lastSeq="";//��һ�е����һ��motif������У������һ�ж�û��motif�Ļ���ֱ�Ӿ�ȡ��һ��
		int motifReadLines=bpNum/chrLineLength;//ָ����bpNum�£���Ҫ��ȡ���в��ܴﵽ������
		int tmplines=0;//��¼�Ѿ���ȡ�˼���
		
	   long chrlength=gffChrUnion.getChrLength(chrID);
	   int alllines=(int) Math.ceil((double)chrlength/chrLineLength);
	   int motifInv=(int)Math.ceil((double)alllines/motifReadLines);
	   int[] motifNum=new int[motifInv+1];// 0:����motif�ĳ���֮�ͣ�����ģ�ÿ�������ڵ�motif����
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
			motifNum[motifnum]=motifInfo[0];//��������motif������
			motifNum[0]=motifNum[0]+motifInfo[2];
			motifnum++;
			tmpSeq=null;tmpSeq=new StringBuilder();
			tmpSeq.append(lastSeq);
			tmplines=0;
		}
		if(tmplines>0)//�����������У���ô��������
		{
			int[] motifInfo=getMotifInfo(tmpSeq.toString(), motifRex);
			motifNum[motifnum]=motifInfo[0];//��������motif������
			motifNum[0]=motifNum[0]+motifInfo[2];
			tmpSeq=null;
		}
		////////////////////////ȷ�������Ƿ����////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (motifnum!=motifNum.length-1) {
			System.out.println("motifSearch ��Ĳ���motif���������⣬���õ���motif�ָ�����������Ԥ���õ�motif�������������");
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		return motifNum;
	}
	
	/**
	 * �������У���������motif��Ϣ
	 * @param subChrSeq
	 * @param motifReg
	 * @return int[3]
	 * 0: motif��Ŀ
	 * 1: ���һ��motif������ĩ�˵ľ���
	 * 2: �ܵ�motif����
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
