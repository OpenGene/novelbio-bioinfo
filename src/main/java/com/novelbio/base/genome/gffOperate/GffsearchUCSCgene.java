package com.novelbio.base.genome.gffOperate;

import java.util.ArrayList;



public class GffsearchUCSCgene extends Gffsearch
{

	/**
	 * ��λ�㴦�ڻ����ڲ�ʱ�ľ������,����GffcodInfoUCSCgeneʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 */
	@Override
	protected GffCodInfo SearchLOCinside(int Coordinate,ArrayList<GffDetail> loclist, int beginnum, int endnum)
	{
		GffDetailUCSCgene LOCdetial=(GffDetailUCSCgene) loclist.get(beginnum);
		GffCodInfoUCSCgene cordInsideGeneInfo=new GffCodInfoUCSCgene();
		cordInsideGeneInfo.result=true;//�ҵ�λ��
		cordInsideGeneInfo.begincis5to3=LOCdetial.cis5to3;//��������
		cordInsideGeneInfo.insideLOC=true;//�ڻ�����
		cordInsideGeneInfo.LOCID[0]=LOCdetial.locString;//�������ID
		if(LOCdetial.cis5to3)
		{
			cordInsideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//������������λ��
			cordInsideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//���������յ��λ��
		}	
		else {
			cordInsideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberend);//������������λ��
			cordInsideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberstart);//���������յ��λ��
		}
		cordInsideGeneInfo.distancetoLOCStart[1]=-1;
		
		cordInsideGeneInfo.distancetoLOCEnd[1]=-1;
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		boolean flag=false;
		
		/** 
		 * �趨ע�⣬���λ����tss֮ǰ���������֮��(�����������)���ж�Ϊ��һ�������ӣ�ͬ�������ת¼�յ㵽�����յ�֮�䣬����3UTR�ڣ��ж�Ϊ���һ��������
		 * @param splitnum:ת¼����Ŀ
		 * @param position���������ڴ��λ�� 1.������ 2.�ں��� 
		 * @param ExIntronnum: �û����ں���/�����ӵľ���λ��
		 * @param start:�����ں���/������ �������룬5��UTRΪ��gene�����룬 3��UTRΪ�����һ��CDS����
		 * @param end:�����ں���/�����ӵ��յ���룬��5��UTRΪ��ATG�� 3��UTRΪ��geneβ������
		 *  0���������ھ���λ�� 1..������ 2.�ں��� <br/>
		 * 1: �û����ں���/�����ӵ�λ�ã���1��ʼ������ע��UCSC������������ں��ӣ�����ʱ������5UTR��3UTR�� <br/>
		 * 2�������ں���/������ ��������<br/>
		 * 3�������ں���/�����ӵ��յ����<br/>
		 * 4������������ӣ������Ƿ���5UTR��3UTR�ڣ�0: ����  5:5UTR   3:3UTR<br/>
		 * 5�������UTR�����ڣ�5��UTRΪ��gene�����룬  3��UTRΪ�����������룬�������ں��Ӽ�����롣������ڣ���Ϊ-1<br/>
		 * 6�������UTR�����ڣ�5��UTRΪ��ATG�� 3��UTRΪ��geneβ�����룬�������ں��Ӽ�����롣������ڣ���Ϊ-1<br/>
	
		 */
		//int splitnum=LOCdetial.getSplitlistNumber();//�ɱ���ӵ�mRNA��Ŀ
		//ArrayList<String> lsSplitID=LOCdetial.getLsSplitename();
		String splitID; int position; int ExIntronnum; int start; int end;int UTR; int UTRstart;int UTRend;
		
		
		//for (int i = 0; i < splitnum; i++)//һ��һ��ת¼���ļ��
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//ֻ����һ��Ⱦɫ������
		ArrayList<Object> longSplitExonInfo=LOCdetial.getLongestSplit();//����ת¼������Ϣ      
		ArrayList<Integer> longSplitExon=(ArrayList<Integer>)longSplitExonInfo.get(1);
		splitID=(String)longSplitExonInfo.get(0);
		int exonNum=longSplitExon.size();//ת¼����Exon����Ŀ
		cordInsideGeneInfo.codToATG[0]=1000000000;
		cordInsideGeneInfo.codToATG[1]=1000000000;
		
		
		
		flag=false;
		if (cordInsideGeneInfo.begincis5to3 ) 
			cordInsideGeneInfo.codToATG[0]=Coordinate-longSplitExon.get(0);
		else if (!cordInsideGeneInfo.begincis5to3)
			cordInsideGeneInfo.codToATG[0]=longSplitExon.get(1)-Coordinate;
			
		cordInsideGeneInfo.codToATG[1]=-1000000000;
		
		/**
		 * ��������Ǵ�5'-3' 
		 */
		if (LOCdetial.cis5to3)
		{
			for(int j=2; j<exonNum; j++)  //һ��һ��Exon�ļ��
			{
				if(Coordinate<longSplitExon.get(j) && j%2==0)//��������֮ǰ���ں����У���������Ϊ�� 2,3  4,5  6,7  8,9   0��ת¼����ת¼��㣬1��ת¼����ת¼�յ�
				{
					flag=true;
				   if(j==2)//��5��UTR��,Ҳ��������������   tss cod ��2 3 �� 4 5 �� 6 7 �� 8 9 
				   {
					   position=1;
					   start=Coordinate-LOCdetial.numberstart;//����������
					   end=longSplitExon.get(3)-Coordinate;//�����һ���������յ�
					   ExIntronnum=1;
					   UTR=5;
					   UTRstart=start;
					   UTRend=0;
					   if (longSplitExon.get(0)<=longSplitExon.get(3)) //���atg�ڵ�һ����������  tss cod ��2 0 3 �� 4 5 �� 6 7 �� 8 9 
					   {
						   UTRend=longSplitExon.get(0)-Coordinate;//
					   }
					   else    //���atg���ڵ�һ����������  tss cod ��2 3 �� 4 5 �� 6 0 7 �� 8 9 
					   {
						   UTRend=longSplitExon.get(3)-Coordinate;
						   int m=5;
						   while (m<exonNum&&longSplitExon.get(0)>longSplitExon.get(m)) //���ﲻ�ܵ��ڣ���Ϊ���滹Ҫ��һ��
						   {
							   UTRend=UTRend+longSplitExon.get(m)-longSplitExon.get(m-1);
							   m=m+2;
						   }
						   UTRend=UTRend+longSplitExon.get(0)-longSplitExon.get(m-1);
					   }
					   cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
					   break;//����	   
				   }
				   else //���ں�����
				   {
					  // tss  ��2 3 �� 4 5 ��cod   6 7 �� 8 9 
					   position=2;
					   ExIntronnum=j/2-1;//�ڵ�j/2-1���ں�����
					   end=longSplitExon.get(j)- Coordinate;//���һ��������
					   start=Coordinate-longSplitExon.get(j-1);//��ǰһ��������
					   UTR=0; UTRstart=-1; UTRend=-1;
					   cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
					   break;	//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��	
				   }
				}
				else if(Coordinate<=longSplitExon.get(j)&&j%2==1) //��������֮�У�������Ϊ��2,3  4,5  6,7  8,9   0��ת¼����ת¼��㣬1��ת¼����ת¼�յ�
				{
					flag=true;
					position=1;//��������֮��
					ExIntronnum=(j-1)/2;//�ڵ�(j-1)/2����������
					end=longSplitExon.get(j)-Coordinate;//���뱾��������ֹ
					start=Coordinate-longSplitExon.get(j-1);//���뱾��������ʼ
					 UTR=0; UTRstart=-1; UTRend=-1;
					 
					if(Coordinate<longSplitExon.get(0))//����С��atg����5��UTR��,Ҳ������������
					{
						
						UTR=5;UTRstart=0;UTRend=0;
						// tss  2 3,   4 5,   6 cod 7,   8 0 9
						for (int k = 3; k <= j-2; k=k+2) {
							UTRstart=UTRstart+longSplitExon.get(k)-longSplitExon.get(k-1);
						}
						UTRstart=UTRstart+start;
						// tss  2 3,   4 5,   6 cod  0 7
						if (longSplitExon.get(0)<=longSplitExon.get(j)) //һ��ҪС�ڵ���
						{
							UTRend=longSplitExon.get(0)-Coordinate;
						}
						// tss  2 3,   4 5,   6 cod 7,   8  9,   10 0 11
						else 
						{
							UTRend=longSplitExon.get(j)-Coordinate;
							int m=j+2;
							while (m<exonNum&&longSplitExon.get(0)>longSplitExon.get(m)) 
							{
								UTRend=UTRend+longSplitExon.get(m)-longSplitExon.get(m-1);
								m=m+2;
							}
							UTRend=UTRend+longSplitExon.get(0)-longSplitExon.get(m-1);
						}
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
						break;//����
					}
					// tss  2 3,   4 0 5,   6 1 7,   8  9,   10 11
					if(Coordinate>longSplitExon.get(1))//����cds��ʼ������3��UTR��
					{
						UTR=3; UTRstart=0;UTRend=0;
						// tss  2 3,   4 0 5,   6 1 cod 7,   8  9,   10 11
						if (longSplitExon.get(1)>=longSplitExon.get(j-1))//һ��Ҫ���ڵ��� 
						{
							UTRstart=Coordinate-longSplitExon.get(1);
						}
						// tss  2 3,   4 0 5,   6 1 7,   8  9,   10 cod 11
						else 
						{
							UTRstart=Coordinate-longSplitExon.get(j-1);
							int m=j-3;
							while (m>=2&&longSplitExon.get(m)>longSplitExon.get(1)) 
							{
								UTRstart=UTRstart+longSplitExon.get(m+1)-longSplitExon.get(m);
								m=m-2;
							}
							UTRstart=UTRstart+longSplitExon.get(m+1)-longSplitExon.get(1);
						}
						/////////////////////utrend//////////////////
						// tss  2 3,   4 0 5,   6 1 7,   8  cod 9,   10 11,  12 13
						for (int k = exonNum-1; k >= j+2; k=k-2) {
							UTRend=UTRend+longSplitExon.get(k)-longSplitExon.get(k-1);
						}
						UTRend=UTRend+end;
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
						break;//����
					}
					
					cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
					break;//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
				}
			}
			if (flag==false)//�����һ�������ӵ����һλ����Ҳ���������ѭ��û���ܽ�flag����Ϊtrue,�Ǿ���3UTR��������Ϊ��������
			{
				//tss  2 3,   4 0 5,   6 1 7,   8 9,   10 11,  12 13 cod
				position=1;//��������
				ExIntronnum=(exonNum-2)/2;//����
				end=LOCdetial.numberend-Coordinate;//���뱾������ֹ
				start=Coordinate-longSplitExon.get(exonNum-2);//���������
				UTR=3;
				UTRend=end;
				UTRstart=0;
				if (longSplitExon.get(1)>=longSplitExon.get(exonNum-2)) //���uag�����һ����������  tss cod ��2 0 3 �� 4 5 �� 6 7 �� 8 1 9 cod
				{
					UTRstart=Coordinate-longSplitExon.get(1);//
				}
				else    //���uag���ڵ�һ����������  tss  ��2 3 �� 4 uag 5 �� 6 7 �� 8 9  cod
				{
					UTRstart=Coordinate-longSplitExon.get(exonNum-2);
					int m=exonNum-4;
					while (m>=2&&longSplitExon.get(m)>longSplitExon.get(1)) //���ﲻ�ܵ��ڣ���Ϊ���滹Ҫ��һ��
					{
						UTRstart=UTRstart+longSplitExon.get(m+1)-longSplitExon.get(m);
						m=m-2;
					}
					UTRstart=UTRstart+longSplitExon.get(m+1)-longSplitExon.get(1);
				}
				cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
			}
		}
			//////////////////////////20100810//////////////////////////////////////////////////////////////////////////
		 	/**
		 	 * ��������Ǵ�3'-5'����ת¼���Ƿ���
		 	 */
		else
		{
			for(int j=exonNum-1; j>=2; j--)  
			{
				if(Coordinate>longSplitExon.get(j)&&j%2==1)//��������֮ǰ��������Ϊ��2,3  4,5  6,7  8,9 
				{
					flag=true;
					if(j==exonNum-1)//��5��UTR��,Ҳ��������������   end ��2 3 �� 4 5 �� 6 0 7 �� 8 9, cod  tss
					{
						position=1;//����������
						ExIntronnum=1;//���ڵ�һ����������
						end=Coordinate-longSplitExon.get(exonNum-2);//�����һ���������յ�
						start=LOCdetial.numberend-Coordinate;//����������,��Ϊgff�ļ��л���ı���Ǵ�С����ģ�������㷴���Ǵ���Ǹ���
						UTR=5;
						UTRstart=start;
						UTRend=0;
						if (longSplitExon.get(0)>=longSplitExon.get(exonNum-2)) //���atg�ڵ�һ����������  2 1 3 �� 4 5 �� 6 7 �� 8 0 9 cod tss
						{
							UTRend=Coordinate-longSplitExon.get(0);//
						}
						else    //���atg���ڵ�һ����������  2 3 �� 4 0 5 �� 6 7 �� 8 9 cod tss
						{
							UTRend=Coordinate-longSplitExon.get(exonNum-2);
							int m=exonNum-4;
							while (m>=2&&longSplitExon.get(0)<longSplitExon.get(m)) //���ﲻ�ܵ��ڣ���Ϊ���滹Ҫ��һ��
							{
								UTRend=UTRend+longSplitExon.get(m+1)-longSplitExon.get(m);
								m=m-2;
							}
							UTRend=UTRend+longSplitExon.get(m+1)-longSplitExon.get(0);
						}
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
						break;//����	   
					}
					else //���ں�����
					{
						//���atg���ڵ�һ����������  2 3 �� 4 0 5 �� 6 7   cod   8 9  tss
						position=2;//���ں�����
						ExIntronnum=(exonNum-j-1)/2;//�ڵ�(exonNum-j-1)/2���ں�����
						end=Coordinate-longSplitExon.get(j);//���һ��������
						start=longSplitExon.get(j+1)-Coordinate;//��ǰһ��������
						UTR=0; UTRstart=-1; UTRend=-1;
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
						break;	//����ת¼���ļ��
					}
				}
				else if(Coordinate>longSplitExon.get(j)&&j%2==0) //��������֮�У�������Ϊ��2,3  4,5  6,7  8,9 
				{
					flag=true;
					position=1;//��������֮��
					ExIntronnum=(exonNum-j)/2;//�ڵ�(j-1)/2����������
					end=Coordinate-longSplitExon.get(j);//���뱾��������ֹ
					start=longSplitExon.get(j+1)-Coordinate;//���뱾��������ʼ
					UTR=0; UTRstart=-1; UTRend=-1;
					if(Coordinate>longSplitExon.get(1))//С��cds��ʼ������5��UTR��,Ҳ��������������
					{
						//������Ϊ��2 0 3,     4 1 5,   6 7,   8 cod 9,  10 11,   12 13  tss   
						UTR=5;UTRstart=0;UTRend=0;
						for (int k = exonNum-1; k >= j+3; k=k-2) {
							UTRstart=UTRstart+longSplitExon.get(k)-longSplitExon.get(k-1);
						}
						UTRstart=UTRstart+start;
						//������Ϊ��2 0 3,     4 1 cod 5,   6 7,   8 9,  10 11,   12 13  tss   
						if (longSplitExon.get(1)>=longSplitExon.get(j)) //һ��Ҫ���ڵ���
						{
							UTRend=Coordinate-longSplitExon.get(1);
						}
						else //������Ϊ��2 0 3,     4 1 5,   6 7,   8 cod 9,  10 11,   12 13  tss   
						{
							UTRend=Coordinate-longSplitExon.get(j);
							int m=j-2;
							while (m>=2&&longSplitExon.get(1)<longSplitExon.get(m)) {
								UTRend=UTRend+longSplitExon.get(m+1)-longSplitExon.get(m);
								m=m-2;
							}
							UTRend=UTRend+longSplitExon.get(m+1)-longSplitExon.get(1);
						}
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
						break;//����
					}
					//////////////////////////////////////////////////////////////////////////////////////////////
					if(Coordinate<longSplitExon.get(0))//����cds��ֹ������3��UTR��
					{
						UTR=3; UTRstart=0;UTRend=0;
						//������Ϊ��end   2 3,     4 cod 0 5,   6 7,   8 9,  10 11,   12 13     
						if (longSplitExon.get(0)<=longSplitExon.get(j+1))//һ��ҪС�ڵ��� 
						{
							UTRstart=longSplitExon.get(0)-Coordinate;
						}
						else //������Ϊ��end   2 3,     4 cod 5,   6 7,   8 9,  10 0 11,   12 13    
						{
							UTRstart=longSplitExon.get(j+1)-Coordinate;
							int m=j+3;
							while (m<exonNum&&longSplitExon.get(m)<longSplitExon.get(0)) 
							{
								UTRstart=UTRstart+longSplitExon.get(m)-longSplitExon.get(m-1);
								m=m+2;
							}
							UTRstart=UTRstart+longSplitExon.get(0)-longSplitExon.get(m-1);
						}
						/////////////////////utrend//////////////////
						//������Ϊ��end   2 3,     4 5,   6 cod 7,   8 9,  10 0 11,   12 13    
						for (int k = 3; k <= j-1; k=k+2) {
							UTRend=UTRend+longSplitExon.get(k)-longSplitExon.get(k-1);
						}
						UTRend=UTRend+end;
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
						break;//����
					}
					cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
					break;//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��					 }
				 }
				
			}
			 if (flag==false)//�����һ�������ӵ����һλ����Ҳ���������ѭ��û���ܽ�flag����Ϊtrue,�Ǿ���3UTR��������Ϊ��������
			 {
				 // end cod 2 3,  4 5,  6 7,  8 0 9,  10 11
				 position=1;//��������
				 ExIntronnum=(exonNum-2)/2;//����
				 end=Coordinate-LOCdetial.numberstart;//���뱾������ֹ
				 start=longSplitExon.get(3)-Coordinate;//���������
				 UTR=3;
				 UTRend=end;
				 UTRstart=0;
				// end cod 2 0 3,  4 5,  6 7,  8 1 9,  10 11
				 if (longSplitExon.get(0)<=longSplitExon.get(3)) //���end�ڵ�һ����������  end cod 2 0 3 �� 4 5 �� 6 7 �� 8 9 
				 {
					 UTRstart=longSplitExon.get(0)-Coordinate;//
				 }	
				 else    //���UAG���ڵ�һ����������  end cod ��2 3 �� 4 5 �� 6 0 7 �� 8 9 
				 {
					 UTRstart=longSplitExon.get(3)-Coordinate;
					 int m=5;
					 while (m<exonNum&&longSplitExon.get(m)<longSplitExon.get(0)) //���ﲻ�ܵ��ڣ���Ϊ���滹Ҫ��һ��
					 {
						 UTRstart=UTRstart+longSplitExon.get(m)-longSplitExon.get(m-1);
						 m=m+2;
					 }
					 UTRstart=UTRstart+longSplitExon.get(0)-longSplitExon.get(m-1);
				 }
				 cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end,UTR,UTRstart,UTRend);//װ��list
			 }
		}
		return cordInsideGeneInfo;
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	
	/**
	 * ��λ�㴦�ڻ����ⲿʱ�ľ������,����GffCodInfoGeneʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 */
	@Override
	protected GffCodInfo SearchLOCoutside(int Coordinate,ArrayList<GffDetail> Genlist, int beginnum, int endnum) {
		GffCodInfoUCSCgene cordOutSideGeneInfo=new GffCodInfoUCSCgene();
		GffDetailUCSCgene endnumlist=null;
		GffDetailUCSCgene beginnumlist=null;
		
		cordOutSideGeneInfo.result=true;//�ҵ�λ��
		cordOutSideGeneInfo.insideLOC=false;//�ڻ����
		
		cordOutSideGeneInfo.codToATG[0]=1000000000;
		cordOutSideGeneInfo.codToATG[1]=1000000000;
		
		
		
		
		if (beginnum!=-1) {
			 beginnumlist=(GffDetailUCSCgene) Genlist.get(beginnum);
			 cordOutSideGeneInfo.begincis5to3=beginnumlist.cis5to3;//�ϸ�������
			 cordOutSideGeneInfo.LOCID[1]=beginnumlist.locString;//�ϸ������ID
			 //��ǰһ������ת¼�����յ�ľ���
			 if(cordOutSideGeneInfo.begincis5to3)
			 {//����������ʱ����TSS����Ϊ��������EndΪ����        |>----->------*
				 cordOutSideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-beginnumlist.numberstart);
				 cordOutSideGeneInfo.distancetoLOCEnd[0]=-Math.abs(Coordinate-beginnumlist.numberend);
			 }
			 else
			 {//��������ʱ����TSS����Ϊ��������EndΪ����   <-------<|----*
				 cordOutSideGeneInfo.distancetoLOCStart[0]=-Math.abs(beginnumlist.numberend-Coordinate);
				 cordOutSideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-beginnumlist.numberstart);
			 }
			 
			 
			 ArrayList<Object> longSplitExonInfo=beginnumlist.getLongestSplit();//����ת¼������Ϣ      
			 ArrayList<Integer> longSplitExon=(ArrayList<Integer>)longSplitExonInfo.get(1);
			 
			 if (cordOutSideGeneInfo.begincis5to3 ) 
				 cordOutSideGeneInfo.codToATG[0]=Coordinate-longSplitExon.get(0);
			 else if (!cordOutSideGeneInfo.begincis5to3)
				 cordOutSideGeneInfo.codToATG[0]=longSplitExon.get(1)-Coordinate;
		}
		
		if (endnum!=-1) {
			endnumlist=(GffDetailUCSCgene) Genlist.get(endnum);
			cordOutSideGeneInfo.endcis5to3=endnumlist.cis5to3;//�¸�������
			cordOutSideGeneInfo.LOCID[2]=endnumlist.locString;//�¸������ID
			//���һ������ת¼�����յ�ľ���
			//���һ������ת¼�����յ�ľ���
	        if(cordOutSideGeneInfo.endcis5to3)
	        {//����������ʱ����TSS����Ϊ��������EndΪ����         *---|>----->----
			   cordOutSideGeneInfo.distancetoLOCStart[1]=-Math.abs(Coordinate-endnumlist.numberstart);
			   cordOutSideGeneInfo.distancetoLOCEnd[1]=Math.abs(Coordinate-endnumlist.numberend);
	        }
	        else
	        {//��������ʱ����TSS����Ϊ��������EndΪ����        *----<-------<|
	        	cordOutSideGeneInfo.distancetoLOCStart[1]=Math.abs(endnumlist.numberend-Coordinate);
	        	cordOutSideGeneInfo.distancetoLOCEnd[1]=-Math.abs(Coordinate-endnumlist.numberstart);
	        }
	        
	   	 
			 ArrayList<Object> longSplitExonInfo=endnumlist.getLongestSplit();//����ת¼������Ϣ      
			 ArrayList<Integer> longSplitExon=(ArrayList<Integer>)longSplitExonInfo.get(1);
			 
			 if (cordOutSideGeneInfo.begincis5to3 ) 
				 cordOutSideGeneInfo.codToATG[1]=Coordinate-longSplitExon.get(0);
			 else if (!cordOutSideGeneInfo.begincis5to3)
				 cordOutSideGeneInfo.codToATG[1]=longSplitExon.get(1)-Coordinate;
		}
        return cordOutSideGeneInfo;
    }
}
