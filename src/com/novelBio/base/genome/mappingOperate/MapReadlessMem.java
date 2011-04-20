package com.novelBio.base.genome.mappingOperate;

import java.io.BufferedReader;
import java.util.LinkedList;

import com.novelBio.base.dataOperate.TxtReadandWrite;



/**
 * �����ڴ�Ƚ��ٵİ汾������������
 * @author zong0jie
 *
 */
public class MapReadlessMem {
	
	
	/**
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 * @param chrLengthFile ���汣��ÿ��chr�ж೤�����߳�������ɶ೤��short����
	 * @param colChrID ChrID�ڵڼ��У���0��ʼ
	 * @param colStartNum mapping����ڵڼ��У���0��ʼ
	 * @param colEndNum mapping�յ��ڵڼ��У���0��ʼ
	 * @param invNum ÿ������λ����
	 * @throws Exception 
	 */
	public void  ReadMapFile(String mapFile,String chrLengthFile,String sep,int colChrID,int colStartNum,int colEndNum,int invNum) throws Exception 
	{
		//��ν�������˵ÿ��invNum��bp�Ͱ���invNumbp��ÿ��bp��Reads������ȡƽ������λ���������chrBpReads��
		int m=100000000;
		short[] chrBpReads=new short[m];
		TxtReadandWrite txtmap=new TxtReadandWrite();
		txtmap.setParameter(mapFile,false, true);
		BufferedReader bufmap=txtmap.readfile();
		String content="";
		//��λ���飬��һά��¼�ڼ���bp���ڶ�ά��¼��bp���ж��ٸ�reads����
		LinkedList<int[]> lstmpBpNum=new LinkedList<int[]>();
		
		//�ȼ���mapping����Ѿ�����ã�����Ⱦɫ���Ѿ��ֿ��á�
		while ((content=bufmap.readLine())!=null) 
		{
			String[] tmp=content.split(sep);
			if (colChrID>0) //��ÿ�����溬��chrID��ʱ��
			{
				//tmp[colChrID];
			}
			int tmpStart=Short.parseShort(tmp[colStartNum]);//��reads �����
			int tmpEnd=Short.parseShort(tmp[colEndNum]);//��reads���յ�
			
			int lengthLsTmp= lstmpBpNum.size();//��ʱlist�ĳ���
			int lastlsBpNum=lstmpBpNum.get(lengthLsTmp-1)[0];//��ʱlist�м�¼�����һλbp������
			
			//����µ�reads��lstmp�����һλ+invNumС����ô��reads������ȥ��ͬʱ���Ƿ�����ǰ���bp
			int restNum=invNum-lastlsBpNum%invNum;//�����һλ�������bp���м�λ
			if (restNum==invNum) //������һλ�Ϳ��Խ����ˣ���������bpΪ0λ
				restNum=0;
			
			if (tmpStart<=lastlsBpNum+restNum)//��һ��reads�����lstmp���һ��bp�Ľ��㷶Χ��
			{
				int coverStartBpNum=lengthLsTmp-(lastlsBpNum-tmpStart)-1;//��ls�ĵڼ���bp�Ͽ�ʼ˳���һ��ע�������Ǵ�0��ʼ������
				if(coverStartBpNum>lengthLsTmp-1)//��һ��reads����㳬����ls�ķ�Χ
				{
					int tmpBpLoc=lastlsBpNum+1;//bp���꣬�����һ��+1��ʼ����
					while (tmpBpLoc<tmpStart) //����ȱ�ĵط�����0
					{
						int[] tmpAddBp=new int[2];
						tmpAddBp[0]=tmpBpLoc;
						tmpAddBp[1]=0;
						lstmpBpNum.add(tmpAddBp);
						tmpBpLoc++;
					}
					//�����ѭ��������tmpBpLoc==tmpStart
					while (tmpBpLoc<=tmpEnd) 
					{
						int[] tmpAddBp=new int[2];
						tmpAddBp[0]=tmpBpLoc;
						tmpAddBp[1]=1;
						lstmpBpNum.add(tmpAddBp);
						tmpBpLoc++;
					}
				}
				else//��һ��reads�������ls�ķ�Χ��
				{
					int tmpBpLoc=lstmpBpNum.get(coverStartBpNum)[0];//ֱ�Ӵ����￪ʼ
					while (tmpBpLoc<=lastlsBpNum&&tmpBpLoc<=tmpEnd) //������ls�ڲ�ʱ
					{
						int[] tmpCod=lstmpBpNum.get(coverStartBpNum);
						tmpCod[1]++;
						tmpBpLoc++;
						coverStartBpNum++;
					}
					if (coverStartBpNum!=lengthLsTmp) {
						System.out.println("error");
					}
					while (tmpBpLoc<=tmpEnd) //������ls��Χʱ
					{
						int[] tmpAddBp=new int[2];
						tmpAddBp[0]=tmpBpLoc;
						tmpAddBp[1]=1;
						lstmpBpNum.add(tmpAddBp);
						tmpBpLoc++;
					}	
				}				
			}
			else //��һ��reads��㳬����lstmp���һ��bp�Ľ��㷶Χ
			{
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
			}
			
			
			
			
			
			for (int i = 0; i < lstmpBpNum.size(); i++) {
				
			}
			
			
			
			
			
			if (tmpStart%10==0) 
			{
				int shortNum=tmpStart/10;
				
				
				
			}
			
			
			
			
			
		}
		
		
		
		
		
		
	}
	
	
	
	
	
	

}
