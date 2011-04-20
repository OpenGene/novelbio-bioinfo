package com.novelBio.base.genome.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import com.novelBio.base.dataOperate.TxtReadandWrite;


public class GffHashRepeat extends GffHash{

	/**
	 * ��ײ��ȡgff�ķ�����������ֻ�ܶ�ȡUCSCRepeat�ļ�<br>
	 * ����Gff�ļ��������������ϣ���һ��list��,��ȡʱ�ӵڶ��ж���<br/>
	 * �ṹ���£�<br/>
     * ����Gff�ļ���<b>����peak���Բ�����˳�����У������ڲ��������</b>�������������ϣ���һ��list��, �ṹ���£�<br>
     * <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
    * ����LOCID����������Ŀ��ţ���UCSCkonwn gene����û��ת¼��һ˵��
	 * ֻ������LOCID����һ����������������ֻ�ܹ�������ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�LOCID,���ﲻ���Ƕ��ת¼����ÿһ��ת¼������һ��������LOCID <br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ��<br>
	 * @throws Exception 
	 */
	@Override
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(String gfffilename) throws Exception {

		  //ʵ����������
		   locHashtable =new Hashtable<String, GffDetail>();//�洢ÿ��LOCID���������Ϣ�Ķ��ձ�
		   Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//һ����ϣ�����洢ÿ��Ⱦɫ��
		   LOCIDList=new ArrayList<String>();//˳��洢ÿ������ţ��������������ȡ��������
		   LOCChrHashIDList=new ArrayList<String>();
		   //Ϊ���ļ���׼��
		   TxtReadandWrite txtgff=new TxtReadandWrite();
		   txtgff.setParameter(gfffilename,false, true);
		   BufferedReader reader=txtgff.readfile();//open gff file
	       
		   String[] ss = null;//�洢�ָ��������ʱ����
		   String content="";
		   //��ʱ����
		   ArrayList<GffDetail> LOCList=null ;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
		   String chrnametmpString=""; //Ⱦɫ�����ʱ����
		   
		   reader.readLine();//������һ��
		   while((content=reader.readLine())!=null)//������β
		   {
			   ss=content.split("\t");
			   chrnametmpString=ss[5].toLowerCase();//Сд
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			    //�µ�Ⱦɫ��
			   if (!Chrhash.containsKey(chrnametmpString)) //�µ�Ⱦɫ��
			   {
				   if(LOCList!=null)//����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�Ƚض̣�Ȼ��������gffGCtmpDetail.numberstart����
				   {
					   LOCList.trimToSize();
					   for (GffDetail gffDetail : LOCList) {
						   LOCChrHashIDList.add(gffDetail.locString);
					   }
				   }
				   LOCList=new ArrayList<GffDetail>();//�½�һ��LOCList������Chrhash
				   Chrhash.put(chrnametmpString, LOCList);
			   }
			  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			   //ÿһ�о���һ��repeat
			   GffDetailRepeat gffRepeatmpDetail=new GffDetailRepeat();
			   gffRepeatmpDetail.ChrID=chrnametmpString;//��Сд��
			   gffRepeatmpDetail.locString=ss[5]+"_"+ss[6]+"_"+ss[9];
			   gffRepeatmpDetail.numberstart=Integer.parseInt(ss[6]);
			   gffRepeatmpDetail.numberend=Integer.parseInt(ss[7]);
			   if(ss[9].equals("+"))
				   gffRepeatmpDetail.cis5to3=true;
			   else 
				   gffRepeatmpDetail.cis5to3=false;
			   //װ��LOCList��locHashtable
			   
			   gffRepeatmpDetail.repeatFamily=ss[12];
			   gffRepeatmpDetail.repeatClass=ss[11];
			   gffRepeatmpDetail.repeatName=ss[10];
			   LOCIDList.add(gffRepeatmpDetail.locString);
			   LOCList.add(gffRepeatmpDetail);  
			   locHashtable.put(gffRepeatmpDetail.locString, gffRepeatmpDetail);
		   }
		   /////////////////////////////////////////////////////////////////////////////////////////////
		   LOCList.trimToSize();
		   for (GffDetail gffDetail : LOCList) {
			   LOCChrHashIDList.add(gffDetail.locString);
		   }
		 /////////////////////////////////////////////////////////////////////////////////////////////////
	   	return Chrhash;//�������LOCarray��Ϣ
	}
	
	/**
	 * ���ظ���repeat�������Լ���Ӧ�ı�������hash����ʽ����
	 * @return
	 */
	public Hashtable<String, Integer> getLength() 
	{
		int LOCNum=LOCIDList.size();
		Hashtable<String, Integer> hashRepeatLength=new Hashtable<String, Integer>();
		
		for (int i = 0; i < LOCNum; i++) 
		{
			GffDetailRepeat gffDetailRepeat=(GffDetailRepeat)locHashtable.get(LOCIDList.get(i));
			int tmpLength=gffDetailRepeat.numberend-gffDetailRepeat.numberstart;
			String tmprepeatClass=gffDetailRepeat.repeatClass+"/"+gffDetailRepeat.repeatFamily;
			if (hashRepeatLength.containsKey(tmprepeatClass)) //������֪��repeat�����repeat�ĳ����ۼ���ȥ
			{
				tmpLength=tmpLength+hashRepeatLength.get(tmprepeatClass);
				hashRepeatLength.put(tmprepeatClass, tmpLength);
			}
			else//����������µ�repeat�ӽ�ȥ 
			{
				hashRepeatLength.put(tmprepeatClass,tmpLength);
			}
		}
		return hashRepeatLength;
	}

}
