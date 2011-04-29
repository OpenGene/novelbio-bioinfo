package com.novelBio.base.genome;

import java.util.ArrayList;
import java.util.List;

import com.novelBio.base.genome.gffOperate.GffCodInfo;
import com.novelBio.base.genome.gffOperate.GffCodInfoUCSCgene;
import com.novelBio.base.genome.gffOperate.GffDetailUCSCgene;
import com.novelBio.base.genome.gffOperate.GffHash;
import com.novelBio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelBio.base.genome.gffOperate.GffsearchUCSCgene;


/**
 * ��Ŀ�궨λ��Ⱦɫ�����λ����Annotation��ͬʱҲ��ͳ���ں��������ӵ���Ϣ
 * @author zong0jie
 *
 */
public class GffLocatCod extends GffChrUnion
{
 
	 
	
 
	
	/**
	 * ������ά����,�����ÿ��peakLOC���ڵĻ������UCSCknown gene�Լ�refseq
	 * @param LOCIDInfo <br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������<br>
	 * �����2k���κ�50bp���ε�����<br>
	 * @return ���ArrayList-String[10]<br>
	 * 0: ChrID<br>
	 * 1: ����<br>
	 * 2: �ڻ�������ʾ"������"<br>
	 * 3: �ڻ���䲢�Ҿ������»���ܽ�����ʾ"����䣬������/�»���ܽ�"<br>
	 * 4 �ڻ���� "����䣬������/�»����е�Զ"
	 * 5: �ڻ����ڣ���������<br>
	 * 6: �ڻ����ڵľ�����Ϣ<br>
	 * 7: �ڻ���䲢�Ҿ����ϸ�����ܽ����ϸ�������<br>
	 * 8: �ϸ������򣬵��ϸ��������/�յ�ľ���<br>
	 * 9: �ڻ���䲢�Ҿ����¸�����ܽ����¸�������<br>
	 * 10: �¸������򣬵��¸��������/�յ�ľ��� 
	 * 
	 */
	public ArrayList<String[]> peakAnnotationCH(String[][] LOCIDInfo)
	{
		ArrayList<String[]> lspeakAnnotation=new ArrayList<String[]>();
		
		for (int i = 0; i < LOCIDInfo.length; i++)
		{
			GffCodInfoUCSCgene tmpresult=null;
		//	try {
				 tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]), gffHash);
		//	} catch (Exception e) {
			//	System.out.println(LOCIDInfo[i][0]+" "+LOCIDInfo[i][1]);
			//}
			
			String[] tmpPeakAnnotation=new String[11];//�����
			//////////////////////////////////////////////////////////////////////////////
			for (int j = 0; j < tmpPeakAnnotation.length; j++) {
				tmpPeakAnnotation[j]="";//ȫ������Ϊ""
			}
			///////////////////////////////////////////////////////////////////////
			tmpPeakAnnotation[0]=LOCIDInfo[i][0].toLowerCase();
			tmpPeakAnnotation[1]=LOCIDInfo[i][1];
			//////////////////////////////////////////////////////////////////////////////////////////////
			//����ڻ�����
			if (tmpresult.insideLOC) 
			{
				tmpPeakAnnotation[2]="������";tmpPeakAnnotation[3]="";

				/////////////////  �� �� �� ��   //////////////////////////////////////////////////////////////////////////
				tmpPeakAnnotation[5]=tmpresult.LOCID[0];
				
               ////////////////// ��   �� ///////////////////////////
				if (tmpresult.begincis5to3) 
					tmpPeakAnnotation[6]=tmpPeakAnnotation[6]+"_"+"��������"+"����";
				else
					tmpPeakAnnotation[6]=tmpPeakAnnotation[6]+"_"+"��������"+"����";
				
				/////////////////////  UTR  ////////////////////////////////////////////////////////////
				if (tmpresult.GeneInfo.get(0)[4]==5) 
					tmpPeakAnnotation[6]=tmpPeakAnnotation[6]+"_"+"5UTR";
				else if (tmpresult.GeneInfo.get(0)[4]==3) 
					tmpPeakAnnotation[6]=tmpPeakAnnotation[6]+"_"+"3UTR";
					
				//////////////////  Intron  /  Exon  ///////////////////////////////////////
				if(tmpresult.GeneInfo.get(0)[0]==1)
					tmpPeakAnnotation[6]="������"+tmpPeakAnnotation[6]+"_"+"����"+"��"+tmpresult.GeneInfo.get(0)[1]+"��"+"��������";
				else if (tmpresult.GeneInfo.get(0)[0]==2) 
					tmpPeakAnnotation[6]="�ں���"+tmpPeakAnnotation[6]+"_"+"����"+"��"+tmpresult.GeneInfo.get(0)[1]+"��"+"�ں�����";
				
			}
			else
			{
				//���ϸ�����Ĺ�ϵ
				/**
				 * * 0: ChrID<br>
				 * 1: ����<br>
				 * 2: �ڻ�������ʾ"������"<br>
				 * 3: �ڻ���䲢�Ҿ������»���ܽ�����ʾ"����䣬������/�»���ܽ�"<br>
				 * 4 �ڻ���� "����䣬������/�»����е�Զ"
				 * 5: �ڻ����ڣ���������<br>
				 * 6: �ڻ����ڵľ�����Ϣ<br>
				 * 7: �ڻ���䲢�Ҿ����ϸ�����ܽ����ϸ�������<br>
				 * 8: �ϸ������򣬵��ϸ��������/�յ�ľ���<br>
				 * 9: �ڻ���䲢�Ҿ����¸�����ܽ����¸�������<br>
				 * 10: �¸������򣬵��¸��������/�յ�ľ��� 
				 */
				if(tmpresult.begincis5to3)
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[0])<=GeneEnd3UTR)
					{
						tmpPeakAnnotation[4]="����䣬�����ϸ������յ�ܽ�";
						tmpPeakAnnotation[7]=tmpresult.LOCID[1];
						tmpPeakAnnotation[8]="�ϸ�������Ϊ���򣬵��ϸ������յ�ľ���Ϊ"+Math.abs(tmpresult.distancetoLOCEnd[0])+"";
					}
				}
				else
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCStart[0])<=UpStreamTSSbp)
					{
						tmpPeakAnnotation[4]="����䣬�����ϸ�����TSS�ܽ�";
						tmpPeakAnnotation[7]=tmpresult.LOCID[1];
						tmpPeakAnnotation[8]="�ϸ�������Ϊ���򣬵��ϸ�����TSS�ľ���Ϊ"+Math.abs(tmpresult.distancetoLOCStart[0])+"";
					}
				}
				//���¸�����Ĺ�ϵ
				if(tmpresult.endcis5to3)
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCStart[1])<=UpStreamTSSbp)
					{
						if (tmpPeakAnnotation[4].equals("")) 
							tmpPeakAnnotation[4]="����䣬�����¸�����TSS�ܽ�";
						else 
							tmpPeakAnnotation[4]=tmpPeakAnnotation[4]+"_����䣬�����¸�����TSS�ܽ�";
						
						tmpPeakAnnotation[9]=tmpresult.LOCID[2];
						tmpPeakAnnotation[10]="�¸�������Ϊ���򣬵��¸�����TSS�ľ���Ϊ"+Math.abs(tmpresult.distancetoLOCStart[1])+"";
					}
				}
				else
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[1])<=GeneEnd3UTR)
					{
						if (tmpPeakAnnotation[4].equals("")) 
							tmpPeakAnnotation[4]="����䣬�����¸������յ�ܽ�";
						else 
							tmpPeakAnnotation[4]=tmpPeakAnnotation[4]+"_����䣬�����¸������յ�ܽ�";
						
						tmpPeakAnnotation[9]=tmpresult.LOCID[2];
						tmpPeakAnnotation[10]="�¸�������Ϊ���򣬵��¸������յ�ľ���Ϊ"+Math.abs(tmpresult.distancetoLOCEnd[1])+"";
					}
				}
			}
			lspeakAnnotation.add(tmpPeakAnnotation);
		}
		return lspeakAnnotation;
	}
	
	/**
	 * ������ά����,�����ÿ��peakLOC���ڵĻ������UCSCknown gene�Լ�refseq
	 * @param LOCIDInfo <br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������<br>
	 * �����2k���κ�50bp���ε�����<br>
	 * @return ���ArrayList-String[8]<br>
	 * 0: ChrID<br>
	 * 1: ����<br>
	 * 2: �ڻ����ڣ���������<br>
	 * 3: �ڻ����ڵľ�����Ϣ<br>
	 * 4: �ڻ���䲢�Ҿ����ϸ�����ܽ����ϸ�������<br>
	 * 5: �ϸ������򣬵��ϸ��������/�յ�ľ���<br>
	 * 6: �ڻ���䲢�Ҿ����¸�����ܽ����¸�������<br>
	 * 7: �¸������򣬵��¸��������/�յ�ľ��� 
	 * 
	 */
	public ArrayList<String[]> peakAnnotationEN(String[][] LOCIDInfo)
	{
		ArrayList<String[]> lspeakAnnotation=new ArrayList<String[]>();
		
		for (int i = 0; i < LOCIDInfo.length; i++)
		{
			String[] tmpPeakAnnotation=new String[8];//�����
			//////////////////////////////////////////////////////////////////////////////
			for (int j = 0; j < tmpPeakAnnotation.length; j++) {
				tmpPeakAnnotation[j]="";//ȫ������Ϊ""
			}
			GffCodInfoUCSCgene tmpresult=null;
			try {
				tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]), gffHash);

			} catch (Exception e) {
				System.out.println("peakAnnotationEN error");
				tmpPeakAnnotation[3] = "noLOC";
				lspeakAnnotation.add(tmpPeakAnnotation);
				continue;
			}
			

			///////////////////////////////////////////////////////////////////////
			tmpPeakAnnotation[0]=LOCIDInfo[i][0].toLowerCase();
			tmpPeakAnnotation[1]=LOCIDInfo[i][1];
			//////////////////////////////////////////////////////////////////////////////////////////////
			//����ڻ�����
			if (tmpresult.insideLOC) 
			{
				if (tmpresult.distancetoLOCStart[0]<DownStreamTssbp) {
					tmpPeakAnnotation[3]="Promoter:"+tmpresult.distancetoLOCStart[0]+ "bp DownStreamOfTss";
				}
				/////////////////  �� �� �� ��   //////////////////////////////////////////////////////////////////////////
				tmpPeakAnnotation[2]=tmpresult.LOCID[0];
				if (tmpresult.GeneInfo.get(0)[4]==5) 
					tmpPeakAnnotation[3]=tmpPeakAnnotation[3]+"5UTR";
				else if (tmpresult.GeneInfo.get(0)[4]==3) 
					tmpPeakAnnotation[3]=tmpPeakAnnotation[3]+"3UTR";
	
				//////////////////  Intron  /  Exon  ///////////////////////////////////////
				if(tmpresult.GeneInfo.get(0)[0]==1)
					tmpPeakAnnotation[3]=tmpPeakAnnotation[3]+"Exon_"+"Exon Position Number is:"+tmpresult.GeneInfo.get(0)[1];
				else if (tmpresult.GeneInfo.get(0)[0]==2) 
					tmpPeakAnnotation[3]=tmpPeakAnnotation[3]+"Intron_"+"Intron Position Number is:"+tmpresult.GeneInfo.get(0)[1];
			}
			else
			{
				if(tmpresult.begincis5to3)
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[0])<=GeneEnd3UTR)
					{
						tmpPeakAnnotation[4]=tmpresult.LOCID[1];
						tmpPeakAnnotation[5]="Distance to GeneEnd of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[0])+"";
					}
				}
				else
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCStart[0]) <= UpStreamTSSbp)
					{
						tmpPeakAnnotation[4]=tmpresult.LOCID[1];
						if (Math.abs(tmpresult.distancetoLOCStart[0])>=10000) 
						{
							tmpPeakAnnotation[5]="InterGenic_";
						}
						else if (Math.abs(tmpresult.distancetoLOCStart[0])<10000&&Math.abs(tmpresult.distancetoLOCStart[0])>=5000) {
							tmpPeakAnnotation[5]="Distal Promoter_";
						}
						else
						{
							tmpPeakAnnotation[5]="Proximal Promoter_";
						}
						tmpPeakAnnotation[5]=tmpPeakAnnotation[5]+"Distance to TSS of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[0])+"";
					}
				}
				//���¸�����Ĺ�ϵ
				if(tmpresult.endcis5to3)
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCStart[1])<=UpStreamTSSbp)
					{
						tmpPeakAnnotation[6]=tmpresult.LOCID[2];
						if (Math.abs(tmpresult.distancetoLOCStart[1])>=10000) 
						{
							tmpPeakAnnotation[7]="InterGenic_";
						}
						else if (Math.abs(tmpresult.distancetoLOCStart[1])<10000&&Math.abs(tmpresult.distancetoLOCStart[1])>=5000) {
							tmpPeakAnnotation[7]="Distal Promoter_";
						}
						else
						{
							tmpPeakAnnotation[7]="Proximal Promoter_";
						}
						tmpPeakAnnotation[7]=tmpPeakAnnotation[7]+"Distance to TSS of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[1])+"";
					}
				}
				else
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[1])<=GeneEnd3UTR)
					{						
						tmpPeakAnnotation[6]=tmpresult.LOCID[2];
						tmpPeakAnnotation[7]="Distance to GeneEnd of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[1])+"";
					}
				}
			}
			lspeakAnnotation.add(tmpPeakAnnotation);
		}
		return lspeakAnnotation;
	}
	
	
	/**
	 * ָ��������������������peakץ��������ע�ͣ���Ҫ��ɸѡ�����ʵ�peakȻ���������ȽϹ���
	 * �����ϵĻ�����
	 * @param LOCIDInfo ��excelȫ����ȡ���List-String[]
	 * @param colChrID chrID�ڵڼ��У�ʵ����
	 * @param colSummit summitλ���ڵڼ��У�ʵ����
	 * @param filterTss �Ƿ����tssɸѡ��null�����У�������У���ô������int[2],0��tss���ζ���bp  1��tss���ζ���bp����Ϊ���� <b>ֻ�е�filterGeneBodyΪfalseʱ��tss���βŻᷢ������</b>
	 * @param filterGenEnd �Ƿ����geneEndɸѡ��null�����У�������У���ô������int[2],0��geneEnd���ζ���bp  1��geneEnd���ζ���bp����Ϊ����<b>ֻ�е�filterGeneBodyΪfalseʱ��geneEnd���βŻᷢ������</b>
	 * @param filterGeneBody �Ƿ���geneBody��true��������geneBody�Ļ���ȫ��ɸѡ������false��������geneBody��ɸѡ<br>
	 * <b>��������ֻ�е�filterGeneBodyΪfalseʱ���ܷ�������</b>
	 * @param filter5UTR �Ƿ���5UTR��
	 * @param filter3UTR �Ƿ���3UTR��
	 * @param filterExon �Ƿ�����������
	 * @param filterIntron �Ƿ����ں�����
	 * 0-n:�����loc��Ϣ<br>
	 * n+1: ������<br>
	 * n+2: ������Ϣ<br>
	 **/
	public ArrayList<String[]> peakAnnoFilter(List<String[]> LOCIDInfo,int colChrID,int colSummit,int[] filterTss, int[] filterGenEnd, 
			boolean filterGeneBody,boolean filter5UTR, boolean filter3UTR,boolean filterExon, boolean filterIntron)
	{
		int imputLength = LOCIDInfo.get(0).length;
		colChrID--; colSummit--;
		ArrayList<String[]> lspeakAnnotation=new ArrayList<String[]>();
		for (int i = 0; i < LOCIDInfo.size(); i++)
		{
			//��ǣ���Ϊtss������genebodyʵ�������ظ��ģ���ô���tss��true���Ϳ��Բ�����geneBody�Ķ�λ
			//ͬ�����geneEnd��true�����Բ�����geneBody�Ķ�λ
			//����ֻ�е�peak����tss���Σ���geneEnd���Σ�Ҳ���Ǵ���gene�ڲ�ʱ��������mark�Ż��� 
			boolean tss = false; boolean geneEnd = false;
			//��ǣ���ΪUTR�������ں��������Ӳ����ظ��ģ���ô���UTR��true���Ϳ��Բ������ں����������ӵĶ�λ
			//����ֻ�е�peak����UTR�ڲ�ʱ��������mark�Ż��� 
			boolean UTR5  = false; boolean UTR3 = false;
			GffCodInfoUCSCgene tmpresult=null;
			try {
				String chrID = LOCIDInfo.get(i)[colChrID].toLowerCase();
				int summit = Integer.parseInt(LOCIDInfo.get(i)[colSummit]);
				tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(chrID, summit, gffHash);

			} catch (Exception e) {
				System.out.println("peakAnnoFilter error");
				continue;
			}
			//������/��һ������
			String[] tmpPeakAnnotation=new String[imputLength+5];//�������Ҫ���������������Ϣ
			//////////////////////////////////////////////////////////////////////////////
			for (int j = 0; j < tmpPeakAnnotation.length; j++) {
				tmpPeakAnnotation[j]="";//ȫ������Ϊ""
			}
			for (int j = 0; j < imputLength; j++) {
				try {
					tmpPeakAnnotation[j] = LOCIDInfo.get(i)[j];
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			//��һ������
			boolean downGene = false;
			String[] tmpPeakAnnotation2=new String[imputLength+5];//�������Ҫ���������������Ϣ
			//////////////////////////////////////////////////////////////////////////////
			for (int j = 0; j < tmpPeakAnnotation2.length; j++) {
				tmpPeakAnnotation2[j]="";//ȫ������Ϊ""
			}
			for (int j = 0; j < imputLength; j++) {
				try {
					tmpPeakAnnotation2[j] = LOCIDInfo.get(i)[j];
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			if (filterTss != null) //ɸѡtss������
			{
				if (!tmpresult.insideLOC)//����ɸѡtss����
				{
					if (!tmpresult.begincis5to3) 
					{
						if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCStart[0])<=filterTss[0])
						{
							tmpPeakAnnotation[imputLength+0]=tmpresult.LOCID[1];
							if (Math.abs(tmpresult.distancetoLOCStart[0])>=10000) 
							{
								tmpPeakAnnotation[imputLength+1]="InterGenic_";
							}
							else if (Math.abs(tmpresult.distancetoLOCStart[0])<10000&&Math.abs(tmpresult.distancetoLOCStart[0])>=5000) {
								tmpPeakAnnotation[imputLength+1]="Distal Promoter_";
							}
							else
							{
								tmpPeakAnnotation[imputLength+1]="Proximal Promoter_";
							}
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1]+"Distance to TSS of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[0])+"";
						}
					}
					//���¸�����Ĺ�ϵ
					if(tmpresult.endcis5to3)
					{
						if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCStart[1])<=filterTss[0])
						{
							if (!tmpPeakAnnotation[imputLength+0].equals("") && !tmpPeakAnnotation[imputLength+0].contains(tmpresult.LOCID[2])) 
							{
								//˵�����µ�LOC
								downGene = true;
								tmpPeakAnnotation2[imputLength+0] = tmpresult.LOCID[2];
								if (Math.abs(tmpresult.distancetoLOCStart[1])>=10000) 
								{
									tmpPeakAnnotation2[imputLength+1]= "InterGenic_";
								}
								else if (Math.abs(tmpresult.distancetoLOCStart[1])<10000&&Math.abs(tmpresult.distancetoLOCStart[1])>=5000) {
									tmpPeakAnnotation2[imputLength+1]="Distal Promoter_";
								}
								else
								{
									tmpPeakAnnotation2[imputLength+1]="Proximal Promoter_";
								}
								tmpPeakAnnotation2[imputLength+1] = tmpPeakAnnotation2[imputLength+1] + "Distance to TSS of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[1])+"";
							}
							else
							{
								tmpPeakAnnotation[imputLength+0] = tmpresult.LOCID[2];
								if (Math.abs(tmpresult.distancetoLOCStart[1])>=10000) 
								{
									tmpPeakAnnotation[imputLength+1]= "InterGenic_";
								}
								else if (Math.abs(tmpresult.distancetoLOCStart[1])<10000&&Math.abs(tmpresult.distancetoLOCStart[1])>=5000) {
									tmpPeakAnnotation[imputLength+1]="Distal Promoter_";
								}
								else
								{
									tmpPeakAnnotation[imputLength+1]="Proximal Promoter_";
								}
								tmpPeakAnnotation[imputLength+1] = tmpPeakAnnotation[imputLength+1] + "Distance to TSS of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCStart[1])+"";
							}
						}
					}
				}
				else //ɸѡtss����
				{
					if (tmpresult.distancetoLOCStart[0]<filterTss[1])
					{
						tss = true;
						tmpPeakAnnotation[imputLength] = tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]="Promoter:"+tmpresult.distancetoLOCStart[0]+ "bp DownStreamOfTss";
					}
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			if (filterGenEnd != null) //ɸѡgeneEnd������
			{
				if (!tmpresult.insideLOC)//����ɸѡgeneEnd����
				{	
					if(tmpresult.begincis5to3)
					{
						if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[0])<=filterGenEnd[1])
						{
							if (!tmpPeakAnnotation[imputLength+0].equals("") && !tmpPeakAnnotation[imputLength+0].contains(tmpresult.LOCID[1])) 
							{
								downGene = true;
								tmpPeakAnnotation2[imputLength] = tmpresult.LOCID[1];
								tmpPeakAnnotation2[imputLength+1] = "Distance to GeneEnd of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[0])+"";
							}
							else
							{
								tmpPeakAnnotation[imputLength] = tmpresult.LOCID[1];
								tmpPeakAnnotation[imputLength+1] = "Distance to GeneEnd of UpStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[0])+"";
							}
						}
					}
					if(!tmpresult.endcis5to3)
					{
						if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[1])<=filterGenEnd[1])
						{
							if (!tmpPeakAnnotation[imputLength+0].equals("") && !tmpPeakAnnotation[imputLength+0].contains(tmpresult.LOCID[2])) 
							{
								downGene = true;
								tmpPeakAnnotation2[imputLength] = tmpresult.LOCID[2];
								tmpPeakAnnotation2[imputLength+1] = "Distance to GeneEnd of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[1])+"";
							}
							else
							{
								tmpPeakAnnotation[imputLength] = tmpresult.LOCID[2];
								tmpPeakAnnotation[imputLength+1] = "Distance to GeneEnd of DownStream Gene: "+Math.abs(tmpresult.distancetoLOCEnd[1])+"";
							}
						}
					}
				}
				else
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					if (Math.abs(tmpresult.distancetoLOCEnd[0]) < filterGenEnd[0])
					{
						geneEnd = true;
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength] + sep + tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"GeneEnd:"+tmpresult.distancetoLOCStart[0]+ "bp DownStreamOfGeneEnd";
					}
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//geneBody
			if (filterGeneBody && !tss && !geneEnd)
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  �� �� �� ��   //////////////////////////////////////////////////////////////////////////
					if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
						tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
					boolean utr = false;
					if (tmpresult.GeneInfo.get(0)[4]==5)
					{
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"5UTR_";utr = true;
					}
					else if (tmpresult.GeneInfo.get(0)[4]==3) 
					{
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"3UTR_";utr = true;
					}
					//////////////////  Intron  /  Exon  ///////////////////////////////////////
					if(tmpresult.GeneInfo.get(0)[0]==1 )
					{
						if (utr) {
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + "Exon_"+"Exon Position Number is:"+tmpresult.GeneInfo.get(0)[1];
						}
						else {
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] +sep + "Exon_"+"Exon Position Number is:"+tmpresult.GeneInfo.get(0)[1];
						}
					}
						
					else if (tmpresult.GeneInfo.get(0)[0]==2) 
						if (utr) {
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + "Intron_"+"Intron Position Number is:"+tmpresult.GeneInfo.get(0)[1];
						}
						else {
							tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] +sep + "Intron_"+"Intron Position Number is:"+tmpresult.GeneInfo.get(0)[1];
						}
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//5UTR
			if ( !filterGeneBody && filter5UTR && !tss && !geneEnd )
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  �� �� �� ��   //////////////////////////////////////////////////////////////////////////
					if (tmpresult.GeneInfo.get(0)[4]==5)
					{
						UTR5 = true;
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"5UTR";
					}
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//3UTR
			if ( !filterGeneBody && filter3UTR && !tss && !geneEnd)
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  �� �� �� ��   //////////////////////////////////////////////////////////////////////////
					if (tmpresult.GeneInfo.get(0)[4]==3) 
					{
						UTR3 = true;
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] + sep +"3UTR";
					}
				}
			}
			
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//Exon
			if (!filterGeneBody && filterExon && !tss && !geneEnd && UTR5 && UTR3)
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  �� �� �� ��   //////////////////////////////////////////////////////////////////////////
					
					//////////////////  Intron  /  Exon  ///////////////////////////////////////
					if(tmpresult.GeneInfo.get(0)[0]==1 )
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] +sep + "Exon_"+"Exon Position Number is:"+tmpresult.GeneInfo.get(0)[1];
				}
			}
			//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	//////////////////////	
			//Intron
			if (!filterGeneBody && filterIntron && !tss && !geneEnd && UTR5 && UTR3)
			{
				if (tmpresult.insideLOC)
				{
					String sep = "";
					if (!tmpPeakAnnotation[imputLength+0].trim().equals("")) {
						sep = "///";
					}
					/////////////////  �� �� �� ��   //////////////////////////////////////////////////////////////////////////
					//////////////////  Intron  /  Exon  ///////////////////////////////////////
					if (tmpresult.GeneInfo.get(0)[0]==2) 
						if (!tmpPeakAnnotation[imputLength].contains(tmpresult.LOCID[0])) 
							tmpPeakAnnotation[imputLength] = tmpPeakAnnotation[imputLength]+sep+tmpresult.LOCID[0];
						tmpPeakAnnotation[imputLength+1]=tmpPeakAnnotation[imputLength+1] +sep + "Intron_"+"Intron Position Number is:"+tmpresult.GeneInfo.get(0)[1];
				}
			}
			if (tmpPeakAnnotation[imputLength].trim().equals("")) {
				continue;
			}
			lspeakAnnotation.add(tmpPeakAnnotation);
			if (downGene) {
				lspeakAnnotation.add(tmpPeakAnnotation2);
			}
		}
		return lspeakAnnotation;
	}
	
	
	/**
	 * ������ά����,ͳ��peakLOC���ڻ���λ�õ�ͳ�ƽ�������UCSCknown gene�Լ�refseq
	 * @param LOCIDInfo <br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������<br>
	 * �����3k���κ�100bp���ε�����<br>
	 * @return ���String[6][2]����һά�����⣬�ڶ�ά������<br>
	 * [0]: 5UTR <br>
	 * [1]: 3UTR<br>
	 * [2]: Exon ע�ⲻ����5UTR��3UTR<br> 
	 * [3]: Intron<br>
	 * [4]:Up3k<br>
	 * [5]:InterGenic
	 */
	public String[][] peakStatistic(String[][] LOCIDInfo)
	{
		String[][] peakStatistic=new String[6][2];
		//��ʼ��
		peakStatistic[0][0]="5UTR";peakStatistic[1][0]="3UTR";peakStatistic[2][0]="Exon";
		peakStatistic[3][0]="Intron";peakStatistic[4][0]="Up3k";peakStatistic[5][0]="InterGenic";
		peakStatistic[0][1]="0";peakStatistic[1][1]="0";peakStatistic[2][1]="0";
		peakStatistic[3][1]="0";peakStatistic[4][1]="0";peakStatistic[5][1]="0";
		
		for (int i = 0; i < LOCIDInfo.length; i++)
		{
			GffCodInfoUCSCgene tmpresult=null; String chrID = LOCIDInfo[i][0].toLowerCase(); int summit = Integer.parseInt(LOCIDInfo[i][1]);
			if (summit == 26842076) {
				System.out.println("sss");
			}
			try {
				tmpresult = (GffCodInfoUCSCgene)gffSearch.searchLocation(chrID, summit, gffHash);
			} catch (Exception e) {
				System.out.println("peakStatistic"+LOCIDInfo[i][0].toLowerCase()+" " + LOCIDInfo[i][1]);
				continue;
			}
		
			
			//////////////////////////////////////////////////////////////////////////////
 
			if (tmpresult.insideLOC) 
			{
				if (tmpresult.GeneInfo.get(0)[4]==5) //5UTR
				{
					peakStatistic[0][1]=Integer.parseInt(peakStatistic[0][1])+1+"";
					continue;
				}
				else if (tmpresult.GeneInfo.get(0)[4]==3) //3UTR
				{
					peakStatistic[1][1]=Integer.parseInt(peakStatistic[1][1])+1+"";
					continue;
				}
				//////////////////  Intron  /  Exon  ///////////////////////////////////////
				if(tmpresult.GeneInfo.get(0)[0]==1 && tmpresult.GeneInfo.get(0)[4] == 0) //Exon
				{
					peakStatistic[2][1]=Integer.parseInt(peakStatistic[2][1])+1+"";
					continue;
				}
				else if (tmpresult.GeneInfo.get(0)[0]==2 && tmpresult.GeneInfo.get(0)[4] == 0) //Intron
				{
					peakStatistic[3][1]=Integer.parseInt(peakStatistic[3][1])+1+"";
					continue;
				}
			}
			else
			{
				//���ϸ�����Ĺ�ϵ
				if(tmpresult.begincis5to3)
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[0])<=GeneEnd3UTR)
					{
						peakStatistic[1][1]=Integer.parseInt(peakStatistic[1][1])+1+"";
						continue;//�����ϸ�������GeneEnd3UTR��
					}
				}
				else if (!tmpresult.begincis5to3) 
				{
					if(tmpresult.geneChrHashListNum[0]!=-1&&Math.abs(tmpresult.distancetoLOCStart[0])<=UpStreamTSSbp)
					{
						peakStatistic[4][1]=Integer.parseInt(peakStatistic[4][1])+1+"";
						continue;
					}
				}
				//���¸�����Ĺ�ϵ
				if(tmpresult.endcis5to3)
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCStart[1])<=UpStreamTSSbp)
					{
						peakStatistic[4][1]=Integer.parseInt(peakStatistic[4][1])+1+"";
						continue;
					}
				}
				else if(!tmpresult.endcis5to3)
				{
					if(tmpresult.geneChrHashListNum[1]!=-1&&Math.abs(tmpresult.distancetoLOCEnd[1])<=GeneEnd3UTR)
					{			 
						peakStatistic[1][1]=Integer.parseInt(peakStatistic[1][1])+1+"";
						continue;//InterGenic
					}
				}
				peakStatistic[5][1]=Integer.parseInt(peakStatistic[5][1])+1+"";
			}
		}
		return peakStatistic;
	}
	
	
	
	/**
	 *  ������ά����,�����ÿ��peakLOC���ڵĻ������UCSCknown gene�Լ�refseq
	 * @param LOCIDInfo
	 * ��һά��ChrID<br>
	 * �ڶ�ά������<br>
	 * @param considerDistance �Ƿ�����2k���κ�50bp���ε�����
	 * @return ����ÿ��peak����ϸ������Ϣ���
	 * 0: ChrID<br>
	 * 1: ����<br>
	 * 2: �����ڻ��ǻ����<br>
	 * 3: �ڻ�����: �ں��ӻ���������<br>
	 * 4: �ں������ں������������<br>
	 * 5: �ں������ں����յ�������<br>
	 * 6: �����������������������<br>
	 * 7: ���������������յ�������<br>
	 * 8: �Ƿ�5UTR<br>
	 * 9: 5UTR��������������<br>
	 * 10: 5UTR��ATG�������<br>
	 * 11: �Ƿ�3UTR<br>
	 * 12: 3UTR��UAG�������<br>
	 * 13: 3UTR������β�������<br>
	 * 14: peak��TSS����<br>
	 * 15: peak��ATG����<br>
	 * 16: peak������յ�ľ���
	 */
	public ArrayList<String[]> peakAnnotationDetail(String[][] LOCIDInfo)
	{
		ArrayList<String[]> lspeakAnnotation=new ArrayList<String[]>();
		for (int i = 0; i < LOCIDInfo.length; i++) 
		{
			if (LOCIDInfo[i][1].contains("79963824")) {
				System.out.println("stop");
			}
			
			GffCodInfoUCSCgene tmpresult=null;
			try {
				String LOCID=LOCIDInfo[i][0].toLowerCase();
				int LOCcod=Integer.parseInt(LOCIDInfo[i][1]);
				 tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(LOCID, LOCcod, gffHash);
			} catch (Exception e) {
				String test=LOCIDInfo[i][1];
				System.out.println(test);
			}
			////////////////// �� �� �� ֵ /////////////////////////////////
			String[] tmpPeakAnnotation=new String[17];
			for (int j = 0; j <17; j++) {
				tmpPeakAnnotation[j]="";
			}
			
			tmpPeakAnnotation[0]=LOCIDInfo[i][0].toLowerCase();
			tmpPeakAnnotation[1]=LOCIDInfo[i][1];
			//////////////////////////////////////////////////////////////////////////////////////////////
			//����ڻ�����
			
			
			if (tmpresult.insideLOC) 
			{
				tmpPeakAnnotation[2]="������";
				GffDetailUCSCgene tmpDetailUCSCgene=(GffDetailUCSCgene) tmpresult.geneDetail[0];
				if (tmpresult.geneChrHashListNum[0]==-1) {
					continue;
				}
				if(tmpresult.GeneInfo.get(0)[0]==1)
				{
					tmpPeakAnnotation[3]="������";	
					double ItemLength=tmpDetailUCSCgene.getTypeLength("Exon",  tmpresult.GeneInfo.get(0)[1]);
					////////////////////�� �� �� �� //////////////////////////////////
					if (ItemLength==0) {
						System.out.println("Exon0"+tmpresult.LOCID[0]);
						//int ItemLengthtest=tmpDetailUCSCgene.getTypeLength("5UTR", 0);
					}
					if (tmpresult.GeneInfo.get(0)[2]+tmpresult.GeneInfo.get(0)[3]!=ItemLength) {
						System.out.println("Exon"+tmpresult.LOCID[0]);
					}
					///////////////// �� �� �� �� //////////////////////////////////////////////
					tmpPeakAnnotation[6]=tmpresult.GeneInfo.get(0)[2]/ItemLength+"";
					tmpPeakAnnotation[7]=tmpresult.GeneInfo.get(0)[3]/ItemLength+"";
				}
				else if (tmpresult.GeneInfo.get(0)[0]==2) 
				{
					tmpPeakAnnotation[3]="�ں���";
					double ItemLength=tmpDetailUCSCgene.getTypeLength("Intron", tmpresult.GeneInfo.get(0)[1]);
					//////////////////// �� �� �� �� //////////////////////////////////
					if (ItemLength==0) {
						System.out.println("Intron0"+tmpresult.LOCID[0]);
						//int ItemLengthtest=tmpDetailUCSCgene.getTypeLength("Exon", tmpresult.GeneInfo.get(0)[1]);
					}
					if (tmpresult.GeneInfo.get(0)[2]+tmpresult.GeneInfo.get(0)[3]!=ItemLength) {
						System.out.println("Intron"+tmpresult.LOCID[0]);
					}
					///////////////// �� �� �� �� //////////////////////////////////////////////
					tmpPeakAnnotation[4]=tmpresult.GeneInfo.get(0)[2]/ItemLength+"";
					tmpPeakAnnotation[5]=tmpresult.GeneInfo.get(0)[3]/ItemLength+"";
				}
					
				if (tmpresult.GeneInfo.get(0)[4]==3) 
				{
					tmpPeakAnnotation[11]="3UTR";
					
					double ItemLength=tmpDetailUCSCgene.getTypeLength("3UTR", 0);
					//////////////////// �� �� �� �� //////////////////////////////////
					if (ItemLength==0) {
						System.out.println("3UTR0"+tmpresult.LOCID[0]);
						//int ItemLengthtest=tmpDetailUCSCgene.getTypeLength("Exon", tmpresult.GeneInfo.get(0)[1]);
					}
					if (tmpresult.GeneInfo.get(0)[5]+tmpresult.GeneInfo.get(0)[6]!=ItemLength) {
						System.out.println("3UTR"+tmpresult.LOCID[0]);
					}
					///////////////// �� �� �� �� //////////////////////////////////////////////

					tmpPeakAnnotation[12]=tmpresult.GeneInfo.get(0)[5]/ItemLength+"";
					tmpPeakAnnotation[13]=tmpresult.GeneInfo.get(0)[6]/ItemLength+"";
				}
					
				else if(tmpresult.GeneInfo.get(0)[4]==5)
				{
					tmpPeakAnnotation[8]="5UTR";
					double ItemLength=tmpDetailUCSCgene.getTypeLength("5UTR",0);
					//////////////////// �� �� �� �� //////////////////////////////////
					if (ItemLength==0) {
						System.out.println("5UTR0"+tmpresult.LOCID[0]);
						//int ItemLengthtest=tmpDetailUCSCgene.getTypeLength("Exon", tmpresult.GeneInfo.get(0)[1]);
					}
					if (tmpresult.GeneInfo.get(0)[5]+tmpresult.GeneInfo.get(0)[6]!=ItemLength) {
						System.out.println("5UTR"+tmpresult.LOCID[0]);
					}
					///////////////// �� �� �� �� //////////////////////////////////////////////

					tmpPeakAnnotation[9]=tmpresult.GeneInfo.get(0)[5]/ItemLength+"";
					tmpPeakAnnotation[10]=tmpresult.GeneInfo.get(0)[6]/ItemLength+"";
				}
				tmpPeakAnnotation[14]=tmpresult.distancetoLOCStart[0]+"";
				tmpPeakAnnotation[15]=tmpresult.codToATG[0]+"";
				//����Ҫ�����ڻ�����Ϊ���ţ�������Ϊ����
				tmpPeakAnnotation[16]=-tmpresult.distancetoLOCEnd[0]+"";
			}
			else
			{
				tmpPeakAnnotation[2]="�����";
			
				int tmpUpend=100000000;int tmpUpstart=100000000; int tmpUpATG = 100000000;
				int tmpDownend=100000000;int tmpDownstart=100000000; int tmpDownATG = 100000000;
				
				//���ϸ���Ŀ���/�յ����
				if (tmpresult.geneChrHashListNum[0]!=-1) {
					if (tmpresult.begincis5to3) {
						tmpUpend=-tmpresult.distancetoLOCEnd[0];
					}
					else {
						tmpUpstart=tmpresult.distancetoLOCStart[0];
						tmpUpATG = tmpresult.codToATG[0];
					}
				}
				
				//���¸���Ŀ���/�յ����
				if  (tmpresult.geneChrHashListNum[1]!=-1){
					if(tmpresult.endcis5to3)
					{
						tmpDownstart=tmpresult.distancetoLOCStart[1];
						tmpDownATG = tmpresult.codToATG[1];
					}
					else {
						tmpDownend=-tmpresult.distancetoLOCEnd[1];
					}
				}
				//ȡ����֮��С���Ǹ�ATG
				if (Math.abs(tmpUpATG)<Math.abs(tmpDownATG))
					tmpPeakAnnotation[15]=tmpUpATG+"";
				else 
					tmpPeakAnnotation[15]=tmpDownATG+"";
				//GeneEnd
				if (Math.abs(tmpUpend)<Math.abs(tmpDownend)) 
					tmpPeakAnnotation[16]=tmpUpend+"";
				else 
					tmpPeakAnnotation[16]=tmpDownend+"";
				//TSS
				if (Math.abs(tmpUpstart)<Math.abs(tmpDownstart)) 
					tmpPeakAnnotation[14]=tmpUpstart+"";
				else 
					tmpPeakAnnotation[14]=tmpDownstart+"";
			}
			lspeakAnnotation.add(tmpPeakAnnotation);
		}
		return lspeakAnnotation;
	}
	
	
	/**
	 * ������ά����,ͳ�Ƹ���������ռ�ı��أ����UCSCknown gene
	 * ��������ݣ�<br>
	 * ��һά��ChrID<br>
	 * �ڶ�ά������<br>
	 * ��������ͳ����Ϣ<br>
	 * int[6]<br>
	 * 0:Intron<br>
	 * 1:Exon<br>
	 * 2:5UTR<br>
	 * 3:3UTR<br>
	 * 4:up2k<br>
	 * 5:Intergeneic<br>
	 */
	public int[] locateCod(String[][] LOCIDInfo)
	{
		
		int Intron=0;int Exon=0;int fiveUTR=0;int threeUTR=0;int up2k=0;int intergeneic=0;
		for (int i = 0; i < LOCIDInfo.length; i++)
		{
			GffCodInfoUCSCgene tmpresult=(GffCodInfoUCSCgene)gffSearch.searchLocation(LOCIDInfo[i][0].toLowerCase(), Integer.parseInt(LOCIDInfo[i][1]), gffHash);
			if(tmpresult.insideLOC)//������
			{
				if(tmpresult.GeneInfo.get(0)[0]==1)
					fiveUTR++;
				else if(tmpresult.GeneInfo.get(0)[0]==2)
					Exon++;
				else if(tmpresult.GeneInfo.get(0)[0]==3)	
					Intron++;
				else if(tmpresult.GeneInfo.get(0)[0]==4)
					threeUTR++;
			}
			else 
			{
				if(!tmpresult.begincis5to3 && tmpresult.distancetoLOCStart[0]>-2000)
					up2k++;
				else if(tmpresult.endcis5to3 && tmpresult.distancetoLOCStart[1]>-2000)
					up2k++;
				else 
					intergeneic++;
			}
		}
		int[] result =new int[6];
		result[0]=Intron;result[1]=Exon;result[2]=fiveUTR;result[3]=threeUTR;result[4]=up2k;result[5]=intergeneic;
		return result;
	}
	
	
	
	
	
	
}
