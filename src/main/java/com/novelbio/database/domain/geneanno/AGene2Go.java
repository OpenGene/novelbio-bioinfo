package com.novelbio.database.domain.geneanno;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.validator.util.Flags;
import org.apache.log4j.Logger;

import com.novelbio.database.service.ServAnno;
import com.novelbio.database.service.servgeneanno.ServGo2Term;

/**
 * ��д��equal��hash
 * Ҫ����ncbiid��geneID��GOID����ͬ������Ϊ������NCBIID��ͬ
 * �������geneIDΪ0��Ҳ����NCBIID����û�г�ʼ������ôֱ�ӷ���false
 * 	@Override
 */
public abstract class AGene2Go {
	private static Logger logger = Logger.getLogger(AGene2Go.class);
	public static final String EVIDENCE_IEA = "IEA";
	public static final String SEP = "//";
	private String myGoID;
	private String evidence;
	private String qualifier;
	private String reference;
	private String dataBase;
	private int taxID;
	private ServGo2Term servGo2Term = new ServGo2Term();
	public abstract String getGeneUniId();
	public abstract void setGeneUniID(String geneUniID);
	
	public String getGOID() {
		try {
			String goID = servGo2Term.getHashGo2Term().get(myGoID).getGoID();
			if (!goID.equals(myGoID)) {
				System.out.println("stop");
			}
			return goID;
		} catch (Exception e) {
			return null;
		}
	}
//	/**
//	 * �������ݿ�ʹ��
//	 * @return
//	 */
//	@Deprecated
//	public String getGOIDNorm() {
//		return GoID;
//	}
	
	public void setGOID(String GoID) {
		if (GoID == null) {
			System.out.println("stop");
		}
		GoID = GoID.trim();
		if (GoID == null || GoID.trim().equals("")) {
			return;
		}
		try {
			this.myGoID = servGo2Term.getHashGo2Term().get(GoID).getGoID();
		} catch (Exception e) {
			this.myGoID = GoID;
		}
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	public int getTaxID() {
		return taxID;
	}
	public String getEvidence() {
		return evidence;
	}
	public void setEvidence(String evidence) {
		if (evidence == null || evidence.trim().equals("")) {
			return;
		}
		this.evidence = evidence.trim();
	}
	
	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String qualifier) {
		if (qualifier == null) {
			return;
		}
		qualifier = qualifier.trim();
		if (qualifier.equals("") || qualifier.equals("-")) {
			return;
		}
		this.qualifier = qualifier;
	}
	/**
	 * ����Go2Term������У��
	 * û�оͷ���null
	 * @return
	 */
	public String getGOTerm() {
		try {
			return servGo2Term.getHashGo2Term().get(myGoID).getGoTerm();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * ���ܻᱻsep�ָ�
	 * @return
	 */
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	/**
	 * ����Go2Term������У��
	 * û�оͷ���null
	 * @return
	 */
	public String getFunction() {
		try {
			return servGo2Term.getHashGo2Term().get(myGoID).getGoFunction();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * ���ܻᱻsep�ָ�
	 * @return
	 */
	public String getDataBase() {
		return dataBase;
	}
	public void setDataBase(String dataBase) {
		this.dataBase = dataBase;
	}
	
	private void addDataBase(String dataBase)
	{
		this.dataBase = validate(this.dataBase, dataBase);
	}
	private void addReference(String reference)
	{
		this.reference = validate(this.reference, reference);
	}
	private void addQualifier(String qualifier)
	{
		this.qualifier = validate(this.qualifier, qualifier);
	}
	private void addTaxID(int taxID) {
		if (taxID == 0) {
			return;
		}
		if (this.taxID == 0) {
			this.taxID = taxID;
			return;
		}
		if (this.taxID != taxID) {
			logger.error("������������geneInfo�е�taxID��һ�£�ԭtaxID��"+this.taxID + " ��taxID��" + taxID );
		}
	}
	private void addEvidence(String evidence)
	{
		if (evidence == null || evidence.equals("") || evidence.equals(EVIDENCE_IEA)) {
			return;
		}
		this.qualifier = validate(this.qualifier, qualifier);
	}
	/**
	 * true˵��ȷʵ���¶���
	 * �����Ϣ�ظ����Ͳ���Ҫ�������򷵻�false
	 * @param gene2Go
	 * @return
	 */
	public void copyInfo(AGene2Go gene2Go)
	{
		setDataBase(gene2Go.getDataBase());
		setEvidence(gene2Go.getEvidence());
		setGeneUniID(gene2Go.getGeneUniId());
		setGOID(gene2Go.getGOID());
//		setGOTerm(gene2Go.getGOTerm());
		setQualifier(gene2Go.getQualifier());
		setReference(gene2Go.getReference());
		setTaxID(gene2Go.getTaxID());
	}
	
	
	/**
	 * true˵��ȷʵ���¶���
	 * �����Ϣ�ظ����Ͳ���Ҫ�������򷵻�false
	 * �����Database��Qualifier��Reference��Evidence
	 * @param gene2Go
	 * @return
	 */
	public boolean addInfo(AGene2Go gene2Go)
	{
		if (!validateUpdate(getDataBase(), gene2Go.getDataBase())
			&& 
			!validateUpdate(getQualifier(), gene2Go.getQualifier())
			&&
			!validateUpdate(getReference(), gene2Go.getReference())
			&&
			!validateUpdate(getEvidence(), gene2Go.getEvidence())
		) {
			return false;
		}
		addDataBase(gene2Go.getDataBase());
		addQualifier(gene2Go.getQualifier());
		addReference(gene2Go.getReference());
		addEvidence(gene2Go.getEvidence());
		addTaxID(gene2Go.getTaxID());
		return true;
	}
	/**
	 * �Ƿ���Ҫ����
	 * @param thisField
	 * @param inputField
	 * @return
	 * false����Ҫ����
	 * true ��Ҫ����
	 */
	private boolean validateUpdate(String thisField, String inputField)
	{
		if (thisField == null) {
			thisField = "";
		}
		if (inputField == null) {
			return false;
		}
		inputField = inputField.trim();
		if (inputField.equals("-") || inputField.equals("")) {
			return false;
		}
		if (thisField.contains(inputField)) {
			return false;
		}
		else {
			return true;
		}
	}
	
	private String validate(String thisField, String inputField)
	{
		String inputFieldFinal = "";
		if (inputField == null) {
			return thisField;
		}
		inputField = inputField.trim();
		if (inputField.equals("-") || inputField.equals("")) {
			return thisField;
		}
		else {
			inputFieldFinal = inputField;
		}
		if (thisField == null || thisField.equals("")) {
			return inputFieldFinal;
		}
		else {
			if (inputFieldFinal.equals("") || thisField.contains(inputField)) {
				return thisField;
			}
			else {
				return thisField + SEP + inputFieldFinal;
			}
		}
	}
	
	
	/**
	 * ֻҪ����gene2GO��geneID��ͬ������Ϊ������NCBIID��ͬ
	 * �������geneIDΪ0��Ҳ����NCBIID����û�г�ʼ������ôֱ�ӷ���false
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		AGene2Go otherObj = (AGene2Go)obj;
		
		if(getGeneUniId() == null || getGeneUniId().trim().equals("") || otherObj.getGeneUniId() == null || otherObj.getGeneUniId().trim().equals(""))
		{
			return false;
		}
		
		if (getGeneUniId().equals("0") || otherObj.getGeneUniId().equals("0") ) {
			return false;
		}
		
		return getGeneUniId().equals(otherObj.getGeneUniId())&&getGOID().equals(otherObj.getGOID());
	}
	/**
	 * ��дhashcode��Ҳ�ǽ����geneID
	 */
	public int hashCode(){
		String id = getGeneUniId()+getGOID();
		return id.hashCode(); 
	}
	
	
	
	
	
	
}
