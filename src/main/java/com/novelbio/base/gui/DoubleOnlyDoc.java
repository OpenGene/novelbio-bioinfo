package com.novelbio.base.gui;

import java.awt.event.KeyEvent;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
/**
 * jtxtFeildֻ����������
 * ����
 * ����jtxtFeild.setDocument(new DoubleOnlyDoc());<br>
 * Ȼ�����ü��̼���<br>
 * 	public void keyTyped(KeyEvent evt) {<br>
								String old = jTxtUpValueGo.getText();<br>
								if (old.contains(".")&&evt.getKeyChar() == '.') {<br>
									evt.setKeyChar('\0');//��Ĭ<br>
								}<br>
							}<br>
 * @author zong0jie
 *
 */
public class DoubleOnlyDoc extends PlainDocument {
	private static final long serialVersionUID = 84564523057L;

	public void insertString(int offset, String s, AttributeSet attrSet)
			throws BadLocationException {
		
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			if (!s.equals(".")) {
			return;	
			}
			
		}
		super.insertString(offset, s, attrSet);
	}
}
