package com.novelbio.base.gui;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
/**
 * JTextField ����չ�������趨����ַ��ȵ�
 * @author zong0jie
 *
 */
public class JTextFieldData extends JTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = -721264059920193811L;
	public void setNumOnly() {
		setDocument(new NumberOnlyDoc());
	}
	 /**
	  * @param maxLen
	  *            int ��󳤶�(��С��λ)
	  * @param decLen
	  *            int С��λ����
	  */
	public void setNumOnly(int maxLen, int decLen) {
		setDocument(new NumberOnlyDoc(maxLen, decLen));
	}
	 /**
	  * @param decLen
	  *            int С��λ����
	  */
	public void setNumOnly(int decLen) {
		setDocument(new NumberOnlyDoc(decLen));
	}
	 /**
	  * @param maxLen
	  *            int ��󳤶�(��С��λ) Ĭ��20
	  * @param decLen
	  *            int С��λ���� Ĭ��Ϊ0
	  * @param minRange
	  *            double ��Сֵ Ĭ������С
	  * @param maxRange 
	  *            double ���ֵ Ĭ�����޴�
	  */
	public void setNumOnly(int maxLen, int decLen, double minRange, double maxRange) {
		setDocument(new NumberOnlyDoc(maxLen, decLen, minRange, maxRange));
	}
	
	
}
/**
 * jtxtFeildֻ������С���㣬���ź�����
 * ����
 * ����jtxtFeild.setDocument(new DoubleOnlyDoc());<br>
 * @author zong0jie
 *
 */
class NumberOnlyDoc extends PlainDocument {
	private static final long serialVersionUID = 84564523057L;
	/**
	 * ��󳤶� 
	 */
	int maxLength = 20;// Ĭ�ϵ���20
	 /**
	  * С��λ��
	  */
	 int decLength = 0;

	 double minRange = -Double.MAX_VALUE;

	 double maxRange = Double.MAX_VALUE;
	 /**
	  * @param maxLen
	  *            int ��󳤶�(��С��λ) Ĭ��20
	  * @param decLen
	  *            int С��λ���� Ĭ��Ϊ0
	  */
	 public NumberOnlyDoc(int maxLen, int decLen) {
	  maxLength = maxLen;
	  decLength = decLen;
	 }

	 /**
	  * @param maxLen
	  *            int ��󳤶�(��С��λ) Ĭ��20
	  * @param decLen
	  *            int С��λ���� Ĭ��Ϊ0
	  * @param minRange
	  *            double ��Сֵ Ĭ������С
	  * @param maxRange 
	  *            double ���ֵ Ĭ�����޴�
	  */
	 public NumberOnlyDoc(int maxLen, int decLen, double minRange, double maxRange) {
	  this(maxLen, decLen);
	  this.minRange = minRange;
	  this.maxRange = maxRange;
	 }
	 /**
	  * С��λ����Ĭ��Ϊ0
	  * @param decLen
	  */
	 public NumberOnlyDoc(int decLen) {
	  decLength = decLen;
	 }

	public NumberOnlyDoc() {
	}

	public void insertString(int offset, String s, AttributeSet a)
			throws BadLocationException {
		String str = getText(0, getLength());
		if (str.startsWith("-") && s.equals("-")) {
			return;
		}
		if (
		// ����Ϊf,F,d,D
		s.equals("F")
				|| s.equals("f")
				|| s.equals("D")
				|| s.equals("d")
				// ��һλ��0ʱ,�ڶ�λֻ��ΪС����
				|| (str.trim().equals("0") && !s.substring(0, 1).equals(".") && offset != 0)
				// ����ģʽ��������С����
				|| (s.equals(".") && decLength == 0)) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		String strIntPart = "";
		String strDecPart = "";
		String strNew = str.substring(0, offset) + s + str.substring(offset, getLength());
		//-�ű����ڵ�һλ����
		if (!strNew.startsWith("-") && strNew.contains("-")) {
			return;
		}
		strNew = strNew.replaceFirst("-", ""); // ���������븺��
		int decPos = strNew.indexOf(".");
		if (decPos > -1) {
			strIntPart = strNew.substring(0, decPos);
			strDecPart = strNew.substring(decPos + 1);
		} else {
			strIntPart = strNew;
		}
		if (strIntPart.length() > (maxLength - decLength)
				|| strDecPart.length() > decLength
				|| (strNew.length() > 1 && strNew.substring(0, 1).equals("0") && !strNew.substring(1, 2).equals("."))) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		try {
			if (!strNew.equals("") && !strNew.equals("-")) {// ���������븺��
				double d = Double.parseDouble(strNew);
				if (d < minRange || d > maxRange) {
					throw new Exception();
				}
			}
		} catch (Exception e) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		super.insertString(offset, s, a);
	}
	
	 /**
	  * @param decLen
	  *            int С��λ����
	  * @param maxLen
	  *            int ��󳤶�(��С��λ)
	  * @param minRange
	  *            double ��Сֵ
	  * @param maxRange
	  *            double ���ֵ
	  */

}

/**
 * jtxtֻ����������
 * ����
 *  jtxtFeild.setDocument(new NumOnlyDoc());<br>
 * @author zong0jie
 *
 */	
class NumOnlyDoc extends PlainDocument{
	/**
	 * 
	 */
	private static final long serialVersionUID = 14563457L;

	public void insertString(int offset, String s, AttributeSet attrSet)
			throws BadLocationException {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			return;
		}
		super.insertString(offset, s, attrSet);
	}
}