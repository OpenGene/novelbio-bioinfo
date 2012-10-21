package com.novelbio.analysis.seq.rnaseq;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.omg.CosNaming._BindingIteratorImplBase;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 
 * denovo��RNAseq����trinity��������� result.gene 
 * ���Դ�����ļ��л��iso2gene�Ķ��ձ�
 * �������blastʹ��
 * @author zong0jie
 *
 */
public class RsemGetGene2Iso {
	String rsemGeneResult;
	String result;
	
	HashMap<String, String> mapIso2Gene = new LinkedHashMap<String, String>();
	
	public void setRsemGeneResult(String rsemGeneResult) {
		this.rsemGeneResult = rsemGeneResult;
		if (result == null) {
			result = FileOperate.changeFileSuffix(rsemGeneResult, "_iso2gene", "tab");
		}
	}
	
	public void calculateResult() {
		TxtReadandWrite txtRead = new TxtReadandWrite(rsemGeneResult, false);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			String geneID = ss[0];
			String[] isos = ss[3].split(",");
			for (String iso : isos) {
				mapIso2Gene.put(iso, geneID);
			}
		}
		txtRead.close();
		writeMapToFile();
	}
	
	/** �����ɵ�iso2gene�������ݿ� */
	private void writeMapToFile() {
		if (result == null) {
			return;
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(result, true);
		for (Entry<String, String> entryIso2Gene : mapIso2Gene.entrySet()) {
			txtWrite.writefileln(entryIso2Gene.getKey() + "\t" + entryIso2Gene.getValue());
		}
		txtWrite.close();
	}
	
}
