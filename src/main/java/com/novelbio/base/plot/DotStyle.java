package com.novelbio.base.plot;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

/**
 * �����ݵĵ�ֳɼ��飬ÿһ�鶼��ǲ�ͬ����ɫ�͵����ʽ
 * @author zong0jie
 *
 */
public class DotStyle {

	public static final int STYLE_LINE = 2;
	public static final int STYLE_CYCLE = 4;
	public static final int STYLE_RECTANGLE = 8;
	public static final int STYLE_TRIANGLE = 16;
	
	public static final int SIZE_S = 128;
	public static final int SIZE_SM = 256;
	public static final int SIZE_M = 512;
	public static final int SIZE_MB = 1024;
	public static final int SIZE_B = 2048;
	
	Ellipse2D.Double circle = null;
	Rectangle2D.Double rectangele = null;
	Rectangle2D.Double line = null;
	Polygon TRIANGLE = null;
	/**
	 * �����������ʾ���style��������һ��
	 */
	String group = "";
	/**
	 * ��ɫ
	 */
	Color color = Color.BLACK;
	/**
	 * ��״
	 */
	int style = 2;
	int size = 100;
	/**
	 * �趨��С
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}
	public void setStyle(int style) {
		this.style = style;
	}
	double lineLength = 0;
	/**
	 * ���sizeΪline��������趨������
	 * ����˵һ���趨�˱���������Ĭ��ͼ���Ϊline
	 * �����ÿ������Ϊ�ߵĴ��ڣ���ô�ߵĳ����Ƕ���
	 * @param length
	 */
	public void setLineLength(double length) {
		this.lineLength = length;
	
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getGroup() {
		return group;
	}
	public Color getColor() {
		return color;
	}
	public int getSize() {
		return size;
	}
	public int getStyle() {
		return style;
	}
	/**
	 * �����Ҫ��ͼ�Σ�Ŀǰ��֧��line��circle��rectangle
	 * @return
	 */
	public Shape getShape() {
		if (style == STYLE_LINE) {
			line = new Rectangle2D.Double();
			double x = 0; double y = 0;
			double w = 0; double h = 0;
			//�߱��趨���ٳ�һ�㣬��ֹ��ͼ��ʱ�򴩰�
			h = lineLength*1.2;
			if (size == SIZE_S) 
				w = 0.5;
			else if (size == SIZE_SM) 
				w = 1.0;
			else if (size == SIZE_M)
				w = 2.0;
			else if (size == SIZE_MB)
				w = 3.0;
			else if (size == SIZE_B)
				w = 4.0;
			line = new Rectangle2D.Double(x, y, w, h);
			return line;
		}
		else if (style == STYLE_CYCLE) {
			double x = 0; double y = 0;
			double w = 0; double h = 0;
			//�߱��趨���ٳ�һ�㣬��ֹ��ͼ��ʱ�򴩰�
			h = lineLength*1.2;
			if (size == SIZE_S) {
				w = 0.5; h = 0.5;
			}
			else if (size == SIZE_SM) {
				w = 1.0; h = 1.0;
			}
			else if (size == SIZE_M) {
				w = 2.0; h = 2.0;
			}
			else if (size == SIZE_MB) {
				w = 3.0; h = 3.0;
			}
				
			else if (size == SIZE_B) {
				w = 4.0; h = 4.0;
			}
			circle = new Ellipse2D.Double(x, y, w, h);
			return circle;
		}
		else if (style == STYLE_RECTANGLE) {
			double x = 0; double y = 0;
			double w = 0; double h = 0;
			//�߱��趨���ٳ�һ�㣬��ֹ��ͼ��ʱ�򴩰�
			h = lineLength*1.2;
			if (size == SIZE_S) {
				w = 0.5; h = 0.5;
			}
			else if (size == SIZE_SM) {
				w = 1.0; h = 1.0;
			}
			else if (size == SIZE_M) {
				w = 2.0; h = 2.0;
			}
			else if (size == SIZE_MB) {
				w = 3.0; h = 3.0;
			}
				
			else if (size == SIZE_B) {
				w = 4.0; h = 4.0;
			}
			rectangele = new Rectangle2D.Double(x, y, w, h);
			return rectangele;
		}
		else {
		}
		return null;
	}
	@Override
	public DotStyle clone() {
		DotStyle dotStyle = new DotStyle();
		dotStyle.color = color;
		dotStyle.group = group;
		dotStyle.size = size;
		dotStyle.style = style;
		return dotStyle;
	}
	/**
	 * ��дequals
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		DotStyle otherObj = (DotStyle) obj;

		if (this.color.equals(otherObj.color)
				&& this.group.equals(otherObj.group)
				&& this.size == otherObj.size
				&& this.style == otherObj.style
				) {
			return true;
		}
		return false;
	}
	/**
	 * ��дhash
	 */
	public int hashCode()
	{
		return size + style + group.hashCode() + color.hashCode()*100;
	}

}
