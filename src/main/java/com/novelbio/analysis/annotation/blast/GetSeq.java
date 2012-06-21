package com.novelbio.analysis.annotation.blast;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.annotation.pathway.kegg.prepare.KGprepare;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.GeneID;
/**
 * ��NCBI��nt��nr���ݿ��л��ָ�����ֵ����в�����Ϊfasta��ʽ
	 * �����:<br>
	 * >geneID<br>
	 * ����...............<br>.
	 * �ĸ�ʽ
 * Ϊ������blast��׼��
 * @author zong0jie
 *
 */
public class GetSeq {
	

	
	/**
	 * ����nr���ݿ�ı����У����磺
	 * >gi|71002820|ref|XP_756091.1| conidial pigment biosynthesis 1,3,6,8-tetrahydroxynaphthalene reductase Arp
	 * 2 [Aspergillus fumigatus Af293]gi|6090729|gb|AAF03314.1|AF099736_1 1,3,6,8-tetrahydroxynaphthalene reductase [Asper
	 * gillus fumigatus]gi|5689604|emb|CAB51900.1| 1,3,6,8-tetrahydroxynaphthalene reductase [Aspergillus fumiga
	 * tus]gi|66853729|gb|EAL94053.1| conidial pigment biosynthesis 1,3,6,8-tetrahydroxynaphthalene reductase Arp2 [Asper
	 * gillus fumigatus Af293]gi|159130146|gb|EDP55260.1| 1,3,6,8-tetrahydroxynaphthalene reductase [Aspergillus fumigatus A1163]<br>
	 * ע��֮������ֵķָ����ָ�����
	 * @param geneTitle �������ı��⣬Ҳ����nr��gene�ĵ�һ��
	 * @param TaxName ������
	 * @param hashAcc2GenID accID��geneID�Ķ��ձ�
	 * @param maxGeneIDNum һ��accID�ܿ��ܶ�Ӧ���geneID�����Ƕ�Ӧ̫���˾ͻ������⣬��������ָ��һ������Ӧ�����Ҿ���3�ȽϺ��ʣ�����˵һ��accID����Ӧ3��geneID��������������оͲ�Ҫ��
	 * @return  ��󷵻ظñ�������Ӧ��geneID���������ָ�������ֻ�û�ж�Ӧ��geneID���򷵻�null.�����Ӧ��geneID��Ŀ���� maxGeneIDNum ����ôҲ����null
	 * ��󷵻ص���>geneID
	 * ����
	 * ���ָ�ʽ������ļ�ֻ������blast��ѯ������������blast����������Ϊһ������ܿ��ܻᱻ�гɺü���Ȼ��ֱ��и��Ե�refseq
	 */
	private static String getNrGenID(String geneTitle,String TaxName,HashMap<String, String> hashAcc2GenID,int maxGeneIDNum) 
	{
		String[] ss = geneTitle.split("");
		ArrayList<String> lsGeneID = new ArrayList<String>();
		for (String string : ss)
		{	//������ָ�����ֵ����б���װ��list
			if (string.contains(TaxName)) 
				lsGeneID.add(string);
		}
		//��list����Ϣ�ֱ����hash����ң���������ֻ��һ��geneID����ôֱ�����������ظ�geneID��
		//���������ж���geneID����ô������ȫ�������mache��geneID�����ǲ���С��maxGeneIDNum��
		//���С�ڵĻ�����ôgeneID��Ϊ�Ǹ���С�ģ�Ҳ������"//"��������ʽ����������󷽱���������ݿ⣬�����geneID����Ϣһ�����
		int minGenNum = 100; //��¼���ж�Ӧ���ٵ�geneID������
		int minGenID = -10;//��¼��geneID��λ����Ϣ�������lsGeneID����ȡ
		int minGeneIDsub = -10; //��¼��geneID�ھ���ĳ��gi|71002820|ref|XP_756091.1| conidial pigment biosynthesis 1,3,6,8-tetrahydroxynaphthalene reductase Arp 2 [Aspergillus fumigatus Af293]�е�λ��
		for (int i = 0; i< lsGeneID.size(); i++) 
		{
			String string = lsGeneID.get(i); 
			String[] ss2 = string.split("\\|");
			for (int j = 0; j < ss2.length; j++)
			{
				String string2 = ss2[j];
				String accID = GeneID.removeDot(string2);
				String geneID = hashAcc2GenID.get(accID);
				if (geneID == null) 
					continue;
				else if (!geneID.contains("//")) {
					return geneID;
				}
				else {
					String[] ss3 = geneID.split("//");
					//�ҵ����ٵ��Ǹ�geneID�����ĸ�accID�鵽��
					if (minGenNum > ss3.length) {
						minGenNum = ss3.length;
						minGenID = i;
						minGeneIDsub = j;
					}
				}
			}
		}
		if (minGeneIDsub >= 0) {
			String tmpAccID = lsGeneID.get(minGenID).split("\\|")[minGeneIDsub];
			tmpAccID = GeneID.removeDot(tmpAccID);
			String resultGeneID = hashAcc2GenID.get(tmpAccID);
			if (resultGeneID.split("//").length <= maxGeneIDNum) 
				return resultGeneID;
			else 
				return null;
		}
		else
			return null;
	}
	
	/**
	 * @param TaxName ��������ע��Ҫ��nr��������ֶ�Ӧ
	 * @param taxID ����ID
	 * @param nrFile nr�����ļ�
	 * @param outFile ����ļ�
	 * @param maxGeneIDNum һ��accID�ܿ��ܶ�Ӧ���geneID�����Ƕ�Ӧ̫���˾ͻ������⣬��������ָ��һ������Ӧ�����Ҿ���3�ȽϺ��ʣ�����˵һ��accID����Ӧ3��geneID��������������оͲ�Ҫ��
	 * @throws Exception 
	 */
	public static void getNrTaxSeq(String TaxName , int taxID, String nrFile, String outFile, int maxGeneIDNum) throws Exception 
	{
		HashMap<String, String> hashAcc2GenID = HashDB.getHashGenID(taxID);
		TxtReadandWrite txtNrRead = new TxtReadandWrite();
		txtNrRead.setParameter(nrFile, false, true);
		
		TxtReadandWrite txtOutWrite = new TxtReadandWrite();
		txtOutWrite.setParameter(outFile, true, false);
		
		BufferedReader readNr = txtNrRead.readfile();
		String content = ""; boolean flag = false;//������Ǹ�Seq�Ƿ�д���ı�
		while ((content = readNr.readLine()) != null) {
			if (content.startsWith(">")) 
			{
				flag = false; //ÿ�������µ����о�����flag
				String geneID = getNrGenID(content, TaxName, hashAcc2GenID, maxGeneIDNum);
				if (geneID == null) 
					continue;
				else //�ҵ���geneID
				{
					flag = true;//˵������д���ı�
					txtOutWrite.writefile(">"+geneID +"\n", false);
				}
				continue;
			}
			if (flag) {
				txtOutWrite.writefile(content+"\n", false);
			}
		}
		txtOutWrite.writefile("");//ȫ��д���ı�
		txtNrRead.close();
		txtOutWrite.close();
	}
	
	/**
	 * @param taxID ����ID
	 * @param ntFile nr����t�ļ�
	 * @param outFile ����ļ�
	 * @param maxGeneIDNum һ��accID�ܿ��ܶ� Ӧ���geneID�����Ƕ�Ӧ̫���˾ͻ������⣬��������ָ��һ������Ӧ�����Ҿ���3�ȽϺ��ʣ�����˵һ��accID����Ӧ3��geneID��������������оͲ�Ҫ��
	 * @throws Exception 
	 */
	public static void getNtTaxSeq( int taxID, String ntFile, String outFile, int maxGeneIDNum) throws Exception 
	{
		HashMap<String, String> hashAcc2GenID = HashDB.getHashGenID(taxID);
		TxtReadandWrite txtNrRead = new TxtReadandWrite();
		txtNrRead.setParameter(ntFile, false, true);
		
		TxtReadandWrite txtOutWrite = new TxtReadandWrite();
		txtOutWrite.setParameter(outFile, true, false);
		
		BufferedReader readNr = txtNrRead.readfile();
		String content = ""; boolean flag = false;//������Ǹ�Seq�Ƿ�д���ı�
		while ((content = readNr.readLine()) != null) {
			if (content.startsWith(">"))
			{
				flag = false; //ÿ�������µ����о�����flag
				String geneID = getNtGenID(content, hashAcc2GenID, maxGeneIDNum);
				if (geneID == null) 
					continue;
				else //�ҵ���geneID
				{
					flag = true;//˵������д���ı�
					txtOutWrite.writefile(">"+geneID +"\n", false);
				}
				continue;
			}
			if (flag) {
				txtOutWrite.writefile(content+"\n", false);
			}
		}
		txtOutWrite.writefile("");//ȫ��д���ı�
		txtNrRead.close();
		txtOutWrite.close();
	}
	/**
	 * ����nt���ݿ�ı����У����磺
	 * >gi|4|emb|X17276.1| Giant Panda satellite 1 DNA<br>
	 * @param geneTitle �������ı��⣬Ҳ����nt��gene�ĵ�һ��
	 * @param TaxName ������
	 * @param hashAcc2GenID accID��geneID�Ķ��ձ�
	 * @param maxGeneIDNum һ��accID�ܿ��ܶ�Ӧ���geneID�����Ƕ�Ӧ̫���˾ͻ������⣬��������ָ��һ������Ӧ�����Ҿ���3�ȽϺ��ʣ�����˵һ��accID����Ӧ3��geneID��������������оͲ�Ҫ��
	 * @return  ��󷵻ظñ����е�accID������������blast�������������ָ�������ֻ�û�ж�Ӧ��geneID���򷵻�null.�����Ӧ��geneID��Ŀ���� maxGeneIDNum ����ôҲ����null
	 */
	private static String getNtGenID(String geneTitle,HashMap<String, String> hashAcc2GenID,int maxGeneIDNum) 
	{
		//��list����Ϣ�ֱ����hash����ң���������ֻ��һ��geneID����ôֱ�����������ظ�geneID��
		//���������ж���geneID����ô������ȫ�������mache��geneID�����ǲ���С��maxGeneIDNum��
		//���С�ڵĻ�����ôgeneID��Ϊ�Ǹ���С�ģ�Ҳ������"//"��������ʽ����������󷽱���������ݿ⣬�����geneID����Ϣһ�����
		int minGenNum = 100; //��¼���ж�Ӧ���ٵ�geneID������
		int minGeneIDsub = -10; //��¼��geneID�ھ���ĳ��gi|71002820|ref|XP_756091.1| conidial pigment biosynthesis 1,3,6,8-tetrahydroxynaphthalene reductase Arp 2 [Aspergillus fumigatus Af293]�е�λ��
		String[] ss2 = geneTitle.split("\\|");
		for (int j = 0; j < ss2.length; j++)
		{
			String string2 = ss2[j];
			String accID = KGprepare.removeDot(string2);
			String geneID = hashAcc2GenID.get(accID);
			if (geneID == null) 
				continue;
			else if (!geneID.contains("//")) {
				return accID;
			}
			else {
				String[] ss3 = geneID.split("//");
				//�ҵ����ٵ��Ǹ�geneID�����ĸ�accID�鵽��
				if (minGenNum > ss3.length) {
					minGenNum = ss3.length;
					minGeneIDsub = j;//�ǵڼ���ID
				}
			}
		}
		
		if (minGeneIDsub >= 0) {
			String tmpAccID = geneTitle.split("\\|")[minGeneIDsub];
			tmpAccID = KGprepare.removeDot(tmpAccID);
			String resultGeneID = hashAcc2GenID.get(tmpAccID);
			if (resultGeneID.split("//").length <= maxGeneIDNum) 
				return tmpAccID;
			else 
				return null;
		}
		else
			return null;
	}
	
	
}
