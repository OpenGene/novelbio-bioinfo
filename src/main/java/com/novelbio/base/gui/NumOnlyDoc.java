package com.novelbio.base.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
/**
 * jtxt只能输入数字
 * 方法
 *  jtxtFeild.setDocument(new NumOnlyDoc());<br>
 * @author zong0jie
 *
 */
public class NumOnlyDoc extends PlainDocument{
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