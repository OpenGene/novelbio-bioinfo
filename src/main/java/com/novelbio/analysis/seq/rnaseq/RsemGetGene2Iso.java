package com.novelbio.analysis.seq.rnaseq;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.omg.CosNaming._BindingIteratorImplBase;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 
 * denovo的RNAseq，用trinity跑完后会出来 result.gene 
 * 可以从这个文件中获得iso2gene的对照表
 * 方便后续blast使用
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
	public String getGeneName(String iso) {
		return mapIso2Gene.get(iso);
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
	
	/** 把生成的iso2gene表导入数据库 */
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
