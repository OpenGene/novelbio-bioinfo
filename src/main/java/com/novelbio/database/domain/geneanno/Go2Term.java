package com.novelbio.database.domain.geneanno;

import java.util.HashSet;

import com.novelbio.database.service.servgeneanno.ServGo2Term;

public class Go2Term {
	public static final String RELATION_IS = "IS";
	public static final String RELATION_PARTOF = "PART_OF";
	public static final String RELATION_REGULATE = "REGULATE";
	public static final String RELATION_REGULATE_POS = "REGULATE_POS";
	public static final String RELATION_REGULATE_NEG = "REGULATE_NEG";
	public static final String FUN_SHORT_BIO_P = "P";
	public static final String FUN_SHORT_CEL_C = "C";
	public static final String FUN_SHORT_MOL_F = "F";
	public static final String GO_CC = "cellular component";
	public static final String GO_MF = "molecular function";
	public static final String GO_BP = "biological process";
	public static final String GO_ALL = "all gene ontology";
	private static final String SEP_ID = "@//@";
	private static final String SEP_INFO = "@@";
	private ServGo2Term servGo2Term = new ServGo2Term();
    private String queryGoID;
	private String GoID;
	private String GoTerm;
	private String GoFunction;
	private String Parent;
	private String Child;
	private String Definition;

	/**
	 * �洢��GOID����GOID֮��Ĺ�ϵ
	 * Ʃ��RELATION_IS��RELATION_PARTOF��
	 * ����������GO֮��Ż��и���Ϣ
	 */
	private String flag;
	/**
	 * �洢��GOID����GOID֮��Ĺ�ϵ
	 * Ʃ��RELATION_IS��RELATION_PARTOF��
	 * ����������GO֮��Ż��и���Ϣ
	 */
	private void setFlag(String flag) {
		this.flag = flag;
	}
	/**
	 * go��Ϣ�ľ��嶨��
	 * @param definition
	 */
	public void setDefinition(String definition) {
		Definition = definition;
	}
	/**
	 * go��Ϣ�ľ��嶨��
	 * @param definition
	 */
	public String getDefinition() {
		return Definition;
	}
	/**
	 * �洢��GOID����GOID֮��Ĺ�ϵ
	 * Ʃ��RELATION_IS��RELATION_PARTOF��
	 * ����������GO֮��Ż��и���Ϣ
	 */
	public String getFlag() {
		return flag;
	}
	/**
	 * �趨�����θ���GO����Ϣ
	 * @param go2Term
	 * @param relation ������RELATION�е�һ��
	 */
	public void setParent(Go2Term go2Term, String relation ) {
		if (Parent == null) {
			Parent = relation + SEP_INFO + go2Term.getGoID();
		}
		else {
			Parent = Parent + SEP_ID + relation + SEP_INFO + go2Term.getGoID();
		}
	}
	/**
	 * �趨�����θ���GO����Ϣ�����������趨�����趨�Ļ�׷�Ӷ����Ǹ���
	 * @param go2Term
	 * @param relation ������RELATION�е�һ��
	 */
	public void setParent(String goid, String relation ) {
		if (Parent == null) {
			Parent = relation + SEP_INFO + goid;
		}
		else {
			Parent = Parent + SEP_ID + relation + SEP_INFO + goid;
		}
	}
	/**
	 * �趨����������GO����Ϣ�����������趨�����趨�Ļ�׷�Ӷ����Ǹ���
	 * @param go2Term
	 * @param relation ������RELATION�е�һ��
	 */
	public void setChild(Go2Term go2Term, String relation ) {
		if (Child == null) {
			Child = relation + SEP_INFO + go2Term.getGoID();
		}
		else {
			Child = Child + SEP_ID + relation + SEP_INFO + go2Term.getGoID();
		}
	}
	/**
	 * �趨����������GO����Ϣ
	 * @param go2Term
	 * @param relation ������RELATION�е�һ��
	 */
	public void setChild(String goid, String relation ) {
		if (Child == null) {
			Child = relation + SEP_INFO + goid;
		}
		else {
			Child = Child + SEP_ID + relation + SEP_INFO + goid;
		}
	}
	
	public HashSet<Go2Term> getParent() {
		return getParentChild(Parent);
	}
	public HashSet<Go2Term> getChild() {
		return getParentChild(Child);
	}
	/**
	 * ���ظ���
	 * @return
	 */
	private HashSet<Go2Term> getParentChild(String ParentChild) {
		HashSet<Go2Term> hashResult = new HashSet<Go2Term>();
		if (ParentChild == null) {
			return hashResult;
		}
		String[] ss = ParentChild.split(SEP_ID);
		for (String string : ss) {
			String[] ssInfo = string.split(SEP_INFO);
			Go2Term go2Term = servGo2Term.queryGo2Term(ssInfo[1]);
			go2Term.setFlag(ssInfo[0]);
			hashResult.add(go2Term);
		}
		return hashResult;
	}
	
	
	public String getGoIDQuery() {
		return queryGoID;
	}
	public void setGoIDQuery(String GoIDQuery) {
		this.queryGoID = GoIDQuery;
	}

	public String getGoID() {
		return GoID;
	}
	/**
	 * �����趨
	 * @param GoID
	 */
	public void setGoID(String GoID) {
		this.GoID = GoID;
	}
	
	public String getGoTerm() {
		return GoTerm;
	}
	/**
	 * ȫ�������ڴ��hash���ʡ���һ���ٶ���������Ч�ʺܸ�
	 * @param GOID
	 * @return
	 */
	public static Go2Term queryGo2Term(String GOID) {
		ServGo2Term servGo2Term = new ServGo2Term();
		return servGo2Term.queryGo2Term(GOID);
	}
	/**
	 * ͨ���������ݿ��ѯ��Ч����Ե�
	 * @param GOID
	 * @return
	 */
	public static Go2Term queryGo2TermDB(String GOID) {
		ServGo2Term servGo2Term = new ServGo2Term();
		Go2Term go2Term = new Go2Term();
		go2Term.setGoIDQuery(GOID);
		return servGo2Term.queryGo2Term(go2Term);
	}
	/**
	 * ͨ���������ݿ��ѯ��Ч����Ե�
	 * @param go2Term
	 * @return
	 */
	public static Go2Term queryGo2TermDB(Go2Term go2Term) {
		ServGo2Term servGo2Term = new ServGo2Term();
		return servGo2Term.queryGo2Term(go2Term);
	}
	public void setGoTerm(String GoTerm) {
		this.GoTerm = GoTerm;
	}  
	
	public String getGoFunction() {
		return GoFunction;
	}
	public void setGoFunction(String GoFunction) {
		this.GoFunction = GoFunction;
	}
	/**
	 * ���Ƚ�GOID
	 * �����������null���򷵻�false
	 * @param other
	 * @return
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		
		if (other == null) return false;
		
		if (getClass() != other.getClass()) return false;
		Go2Term otherObj = (Go2Term)other;
		if (GoID == null) {
			return false;
		}
		if (otherObj.getGoID() == null) {
			return false;
		}
		if (GoID.trim().equals(otherObj.getGoID())) {
			return true;
		}
		return false;
	}
	/**
	 * ����queryGOID��flag�⣬ȫ���Ƚ�
	 * �����������null���򷵻�false
	 * @param other
	 * @return
	 */
	public boolean equalsAll(Object other) {
		if (this == other) return true;
		if (other == null) return false;
		if (getClass() != other.getClass()) return false;
		Go2Term otherObj = (Go2Term)other;
		
		if (cmpString(GoFunction, otherObj.getGoFunction())
			&&
			cmpString(GoID, otherObj.getGoID())
			&&
			cmpString(GoTerm, otherObj.getGoTerm())	
			&&
			cmpString(Definition, otherObj.getDefinition())
			&&
			cmpString(Parent, otherObj.Parent)	
			&&
			cmpString(Child, otherObj.Child)	
				) {
			return true;
		}
		return false;
	}
	
	private boolean cmpString(String a, String b)
	{
		if (a == b) {
			return true;
		}
		if ((a == null && b != null) || (a != null && b == null)) {
			return false;
		}
		if (a != null && b != null) {
			return a.trim().equals(b.trim());
		}
		return false;
	}
	
	/**
	 * 	���Ƚ�GOID
	 */
	@Override
	public int hashCode()
	{
		String result = GoID;
		return result.hashCode();
	}
	/**
	 * ��������ģʽ����go2term���goconvert��go2term�ֱ�������� ����Ѿ����˾Ͳ�������
	 * û�в����� ������ʽ�Ǽ򵥵ĸ��ǣ�
	 * ����geneInfo��add�ȷ�ʽ �������go2term��������û�У�������
	 * @return
	 */
	public boolean update() {
		return servGo2Term.updateGo2TermComb(this);
	}
}
