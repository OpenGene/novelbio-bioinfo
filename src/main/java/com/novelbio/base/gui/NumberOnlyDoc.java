package com.novelbio.base.gui;

import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.jasper.tagplugins.jstl.core.If;
import org.apache.poi.hssf.OldExcelFormatException;
/**
 * jtxtFeild只能输入小数点，负号和数字
 * 方法
 * 首先jtxtFeild.setDocument(new DoubleOnlyDoc());<br>
 * @author zong0jie
 *
 */
public class NumberOnlyDoc extends PlainDocument {
	private static final long serialVersionUID = 84564523057L;
	 int maxLength = 16;// 默认的是十六

	 int decLength = 0;

	 double minRange = -Double.MAX_VALUE;

	 double maxRange = Double.MAX_VALUE;
	 /**
	  * @param maxLen
	  *            int 最大长度(含小数位)
	  * @param decLen
	  *            int 小数位长度
	  */
	 public NumberOnlyDoc(int maxLen, int decLen) {
	  maxLength = maxLen;
	  decLength = decLen;
	 }

	 /**
	  * @param maxLen
	  *            int 最大长度(含小数位)
	  * @param decLen
	  *            int 小数位长度
	  * @param minRange
	  *            double 最小值
	  * @param maxRange
	  *            double 最大值
	  */
	 public NumberOnlyDoc(int maxLen, int decLen, double minRange,
	   double maxRange) {
	  this(maxLen, decLen);
	  this.minRange = minRange;
	  this.maxRange = maxRange;
	 }

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
		// 不能为f,F,d,D
		s.equals("F")
				|| s.equals("f")
				|| s.equals("D")
				|| s.equals("d")
				// 第一位是0时,第二位只能为小数点
				|| (str.trim().equals("0") && !s.substring(0, 1).equals(".") && offset != 0)
				// 整数模式不能输入小数点
				|| (s.equals(".") && decLength == 0)) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		String strIntPart = "";
		String strDecPart = "";
		String strNew = str.substring(0, offset) + s + str.substring(offset, getLength());
		//-号必须在第一位出现
		if (!strNew.startsWith("-") && strNew.contains("-")) {
			return;
		}
		strNew = strNew.replaceFirst("-", ""); // 控制能输入负数
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
			if (!strNew.equals("") && !strNew.equals("-")) {// 控制能输入负数
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
	  *            int 小数位长度
	  * @param maxLen
	  *            int 最大长度(含小数位)
	  * @param minRange
	  *            double 最小值
	  * @param maxRange
	  *            double 最大值
	  */

}