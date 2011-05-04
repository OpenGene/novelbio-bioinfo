package com.novelBio.base.genome.gffOperate;

import java.util.ArrayList;

 /**
  * ����ĳ������λ�㷵�ؾ����LOC����Լ���λ
  * ע�⣬������ȫ�ǽ�����GffHash��Ļ����ϵģ�����
  * ����Ҫ��GffHash���֧�ֲ��ܹ�����Ҳ����˵����������GffHash��ķ�����ȡGff�ļ���
  * ������Ҫʵ����
  * ���ߣ��ڽ� 20090617
  */
 
public class GffsearchGene extends Gffsearch{
	
	/**
	 * ��λ�㴦�ڻ����ڲ�ʱ�ľ������,����GffCodInfoGeneʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 */
	protected  GffCodInfo SearchLOCinside(int Coordinate,ArrayList<GffDetail> Genlist,int beginnum,int endnum)
	{  
		GffDetailGene LOCdetial=(GffDetailGene) Genlist.get(beginnum);
		GffCodInfoGene cordInsideGeneInfo=new GffCodInfoGene();
		cordInsideGeneInfo.result=true;//�ҵ�λ��
		cordInsideGeneInfo.begincis5to3=LOCdetial.cis5to3;//��������
		cordInsideGeneInfo.insideLOC=true;//�ڻ�����
		cordInsideGeneInfo.LOCID[0]=LOCdetial.locString;//�������ID
		if(LOCdetial.cis5to3)
		{
			cordInsideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberstart);//��������ʵ������λ��
			cordInsideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberend);//��������ʵ���յ��λ��
		}	
		else {
			cordInsideGeneInfo.distancetoLOCStart[0]=Math.abs(Coordinate-LOCdetial.numberend);//��������ʵ������λ��
			cordInsideGeneInfo.distancetoLOCEnd[0]=Math.abs(Coordinate-LOCdetial.numberstart);//��������ʵ���յ��λ��
		}
		cordInsideGeneInfo.distancetoLOCStart[1]=-1;
		cordInsideGeneInfo.distancetoLOCEnd[1]=-1;
		
		//��ʾ��cds���бȽϵĽ�������flagΪtrue��˵��peakλ���������cds��˵����peak��3'-UTR��
		boolean flag=false;
		
		/** 
		 * @param splitnum:ת¼����Ŀ
		 * @param position���������ڴ��λ�� 1. 5��UTR 2.������ 3.�ں���  4.3��UTR 
		 * @param ExIntronnum: �û����ں���/�����ӵľ���λ�� ��5��UTRΪ-1��3��UTRΪ-2
		 * @param start:�����ں���/������ �������룬5��UTRΪ��gene�����룬 3��UTRΪ�����һ��CDS����
		 * @param end:�����ں���/�����ӵ��յ���룬��5��UTRΪ��ATG�� 3��UTRΪ��geneβ������
		*/
		int splitnum=LOCdetial.getSplitlistNumber();//�ɱ���ӵ�mRNA��Ŀ
		int splitID; int position; int ExIntronnum; int start; int end;
		
		
		for (int i = 0; i < splitnum; i++)//һ��һ��ת¼���ļ��
		{  
            splitID= LOCdetial.getcdslist(i).get(0);//���ת¼����ID��ת¼����һ��ֵget(0)�Ǹ�ת¼����ID
            
			int cdsnum=LOCdetial.getcdslist(i).size();//ת¼����cds����Ŀ
			
			flag=false;
			/**
		 	 * ��������Ǵ�5'-3' 
		 	 */
			if (LOCdetial.cis5to3)
			 {   
		        for(int j=1; j<cdsnum; j++)  //һ��һ��cds�ļ��
		         {
		        	if(Coordinate<LOCdetial.getcdslist(i).get(j) && j%2==1)//��������֮ǰ���ں����У���������Ϊ�� 1,2  3,4  5,6  7,8   0����ת¼����ID
					{  flag=true;
						if(j==1)//��5��UTR��
						{
						     position=1;//��5��UTR��
						     ExIntronnum=-1;//��5��UTR��
						     end=LOCdetial.getcdslist(i).get(j)-Coordinate;//���������
                             start=Coordinate-LOCdetial.numberstart;//����������
                             cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//װ��list
                             break;//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
						}
						else //���ں�����
						{
							 position=3;
						     ExIntronnum=(j-1)/2;//�ڵ�(j-1)/2)���ں�����
						     end=LOCdetial.getcdslist(i).get(j)- Coordinate;//���һ��������
                             start=Coordinate-LOCdetial.getcdslist(i).get(j-1);//��ǰһ��������
                             cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//װ��list
                             break;	//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��	
						}
					}
					else if(Coordinate<LOCdetial.getcdslist(i).get(j)&&j%2==0) //��������֮�У�������Ϊ�� 1,2  3,4  5,6  7,8   0����ת¼����ID
					{  
						flag=true;
						position=2;//��������֮��
						ExIntronnum=j/2;//�ڵ�j/2����������
						end=LOCdetial.getcdslist(i).get(j)-Coordinate;//���뱾��������ֹ
						start=Coordinate-LOCdetial.getcdslist(i).get(j-1);//���뱾��������ʼ
						cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//װ��list
						break;//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
					}
		         }
		        if (flag==false)//�����һ�������ӵ����һλ����Ҳ���������ѭ��û���ܽ�flag����Ϊtrue
		        {
		        	position=4;//��3��UTR
		        	ExIntronnum=-2;//��3��UTR
		        	end=LOCdetial.numberend-Coordinate;//���뱾������ֹ
		        	start=Coordinate-LOCdetial.getcdslist(i).get(cdsnum-1);//���������
		        	cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//װ��list
		        	break;//���п���
		        }	  
			 
			 }
			
		 	/**
		 	 * ��������Ǵ�3'-5'����ת¼���Ƿ���
		 	 */
			 else
			 {
				  for(int j=1; j<cdsnum; j++)  
			         {
			        	if(Coordinate>LOCdetial.getcdslist(i).get(j)&&j%2==1)//��������֮ǰ��������Ϊ�� 1,2  3,4  5,6  7,8  
						{ flag=true;
							if(j==1)//��5'-UTR��
							{
							     position=1;//��5��UTR��
							     ExIntronnum=-1;//��5��UTR��
							     end=Coordinate-LOCdetial.getcdslist(i).get(j);//���������
	                             start=LOCdetial.numberend-Coordinate;//����������,��Ϊgff�ļ��л���ı���Ǵ�С����ģ�������㷴���Ǵ���Ǹ���
							  cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//װ��list
							  break;//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
							}
							else //���ں�����
							{  
								position=3;//���ں�����
							     ExIntronnum=(j-1)/2;//�ڵ�(j-1)/2���ں�����
							     end=Coordinate-LOCdetial.getcdslist(i).get(j);//���һ��������
	                             start=LOCdetial.getcdslist(i).get(j-1)-Coordinate;//��ǰһ��������
	                            cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//װ��list
								
								break;	//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��	
									
							}
						}
						else if(Coordinate>LOCdetial.getcdslist(i).get(j)&&j%2==0) //��������֮��
						{  flag=true;
						   position=2;//��������֮��
					       ExIntronnum=j/2;//�ڵ�j/2����������
					       end=Coordinate-LOCdetial.getcdslist(i).get(j);//���뱾��������ֹ
	                       start=LOCdetial.getcdslist(i).get(j-1)-Coordinate;//���뱾��������ʼ
	                    cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//װ��list 
							  break;//������ת¼���ļ�飬��ʼ��һ���ѭ���������һ��ת¼��
						}
			         }
				  if (flag==false)
				  {
					  position=4;//��3��UTR
				      ExIntronnum=-2;//��3��UTR
				      end=Coordinate-LOCdetial.numberstart;//���뱾������ֹ,��Ϊgff�ļ��л���ı���Ǵ�С����ģ������յ㷴����С���Ǹ���
                      start=LOCdetial.getcdslist(i).get(cdsnum-1)-Coordinate;//���������
                     cordInsideGeneInfo.addingeneinfo(splitID, position, ExIntronnum, start, end);//װ��list
						break;//���п���
				  }	  
			 }
		}
		return cordInsideGeneInfo;
	}
	
	
	/**
	 * ��λ�㴦�ڻ����ⲿʱ�ľ������,����GffCodInfoGeneʵ��������
	 * @param Coordinate ����
	 * @param Genlist ĳ��Ⱦɫ���list��
	 * @param beginnum����������
	 * @param endnum��һ����������
	 */
	protected GffCodInfo SearchLOCoutside(int Coordinate,ArrayList<GffDetail> Genlist,int beginnum,int endnum)
    {   
	
		GffDetailGene endnumlist=(GffDetailGene) Genlist.get(endnum);
		GffDetailGene beginnumlist=(GffDetailGene) Genlist.get(beginnum);
	
		int distanceend=-1; int distancebegin=-1;
		
		GffCodInfoGene cordOutSideGeneInfo=new GffCodInfoGene();
		cordOutSideGeneInfo.result=true;//�ҵ�λ��
		cordOutSideGeneInfo.begincis5to3=beginnumlist.cis5to3;//�ϸ�������
		cordOutSideGeneInfo.endcis5to3=endnumlist.cis5to3;//�¸�������
		cordOutSideGeneInfo.insideLOC=false;//�ڻ����
		cordOutSideGeneInfo.LOCID[1]=beginnumlist.locString;//�ϸ������ID
		cordOutSideGeneInfo.LOCID[2]=endnumlist.locString;//�¸������ID
     
		
        
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
        
        
		/** 
		 * @param splitnum:ת¼����Ŀ
		 * @param position���������ڴ��λ�� 1. 5��UTR 2.������ 3.�ں���  4.3��UTR 
		 * @param ExIntronnum: �û����ں���/�����ӵľ���λ�� ��5��UTRΪ-1��3��UTRΪ-2
		 * @param start:�����ں���/������ �������룬5��UTRΪ��gene�����룬 3��UTRΪ�����һ��CDS����
		 * @param end:�����ں���/�����ӵ��յ���룬��5��UTRΪ��ATG�� 3��UTRΪ��geneβ������
		*/
		int splitID=-1; //int position; int ExIntronnum; int start; int end;
		
		int endsplitnumber=endnumlist.getSplitlistNumber();//��һ�������ת¼����Ŀ
		int begsplitnumber=beginnumlist.getSplitlistNumber();//ǰһ�������ת¼����Ŀ
		
	/**
	 * ���¸�����ľ���
	 */
		if (endnumlist.cis5to3)//����,��ô���ǵ�ATG�ľ���
		{  
           for(int i=0;i<endsplitnumber;i++)
           {     splitID= endnumlist.getcdslist(i).get(0);
        	     distanceend=endnumlist.getcdslist(i).get(1)-Coordinate;
        	     cordOutSideGeneInfo.addenddistance(splitID, distanceend); 
           }
		}
		else //������ô���ǵ�����β���ľ���
		{            
			//�����¸�������ֹ,��Ϊ���򣬶�gff�ļ��л���ı���Ǵ�С����ģ������յ㷴����С���Ǹ���
			distanceend=endnumlist.numberstart-Coordinate;
			cordOutSideGeneInfo.addenddistance(splitID, distanceend);
	           
		}
		
		/**
		 * ���ϸ�����ľ���
		 */
   		if (!beginnumlist.cis5to3)//����
		{  
           for(int i=0;i<begsplitnumber;i++)
           {
        	   
               //���peak��ǰһ������ı���������ȷ���ķ�Χ�ڣ���ôҲ�Ǻ���ʼ��cds�ľ���
        	   splitID=  beginnumlist.getcdslist(i).get(0);
        	   distancebegin=beginnumlist.getcdslist(i).get(1);
        	   cordOutSideGeneInfo.addbegindistance(splitID, distancebegin);
           }			
		}
   		else //����
   		{
   		    distancebegin=Coordinate-beginnumlist.numberend;
   		 cordOutSideGeneInfo.addbegindistance(splitID, distancebegin);
		}
			return cordOutSideGeneInfo;
    }
	
}






																								