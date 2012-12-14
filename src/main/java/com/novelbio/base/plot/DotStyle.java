package com.novelbio.base.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import de.erichseifert.gral.util.GraphicsUtils;

/**
 * �����ݵĵ�ֳɼ��飬ÿһ�鶼��ǲ�ͬ����ɫ�͵����ʽ
 * ��д��equal����û����дhash
 * �����clone��ʹ��
 * @author zong0jie
 *
 */
public class DotStyle implements Cloneable{
	/**
	 * �����ͼ���ڻ������ϲ��Թ��ˣ�Ч������
	 * Areaû����
	 */
	public static final int STYLE_AREA = 2;
	public static final int STYLE_CYCLE = 4;
	public static final int STYLE_RECTANGLE = 8;
	public static final int STYLE_TRIANGLE = 16;
	public static final int STYLE_LINE = 32;
	public static final int STYLE_BAR = 64;
	public static final int STYLE_BOX = 128;
	
	
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
	 * ��ɫ
	 */
	Paint color = Color.BLACK;
	/**
	 * ��״
	 */
	int style = STYLE_CYCLE;
	int size = SIZE_M;
	/**
	 * whether the point can be seen
	 */
	boolean valueVisible = false;
	/**
	 * whether the value of a point can be seen
	 * default is false
	 * @param visible
	 */
	public void setValueVisible(boolean valueVisible) {
		this.valueVisible = valueVisible;
	}
	/**
	 * whether the value of a point can be seen
	 * default is false
	 * @param visible
	 */
	public boolean isValueVisible() {
		return valueVisible;
	}
	
	/**
	 * �趨��С
	 * SIZE_M��
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}
	public void setStyle(int style) {
		this.style = style;
	}
	/**
	 * if want to set the point unvisible, just set color as blank
	 * @param color
	 */
	public void setColor(Paint color) {
		this.color = color;
	}
	public Paint getColor() {
		return color;
	}
	public int getSize() {
		return size;
	}
	public int getStyle() {
		return style;
	}
	/**
	 * whether dot have a string name
	 */
	String dotname = null;
	/**
	 * ���øõ���ʾ�����֣���������ã���Ϊ�õ��y������
	 * @param dotname
	 */
	public void setName(String dotname) {
		this.dotname = dotname;
	}
	/**
	 * �õ���ʾ�����֣���������ã��򷵻�null
	 * @return
	 */
	public String getName() {
		if (dotname == null) {
			return "";
		}
		return dotname;
	}
	/**
	 * �����Ҫ��ͼ�Σ�Ŀǰ��֧��line��circle��rectangle
	 * @param ��Ҫ����ı������߾��Ⱦ������3-5��
	 * @return
	 * ��������ͣ�����Ҫ����������һ��
	 */
	public Shape getShape() {
		if (style == STYLE_AREA || style == STYLE_BAR) {
			return null;
		}
		else if (style == STYLE_CYCLE) {
			double x = 0; double y = 0;
			double w = 0; double h = 0;
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
		else if (style == STYLE_LINE) {
			double x = 0; double y = 0;
			double w = 0; double h = 0;
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
		return null;
	}
	/**
	 * �����Ҫ��ͼ�Σ�Ŀǰ��֧��line��circle��rectangle
	 * @param ��Ҫ����ı������߾��Ⱦ������3-5��
	 * @return
	 * ��������ͣ�����Ҫ����������һ��
	 */
	public BasicStroke getBasicStroke() {
		if (style == STYLE_LINE) {
			double x = 0; double y = 0;
			double w = 0; double h = 0;
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
			return new BasicStroke((float)w);
		}
		return null;
	}
	/**
	 * ����һ��dotstyle
	 */
	@Override
	public DotStyle clone() {
		try {
			DotStyle dotStyle = (DotStyle) super.clone();
			dotStyle.color = color;
			dotStyle.dotname = dotname;
			dotStyle.size = size;
			dotStyle.style = style;
			dotStyle.circle = circle;
			dotStyle.line = line;
			dotStyle.rectangele = rectangele;
			dotStyle.TRIANGLE = TRIANGLE;
			dotStyle.valueVisible = valueVisible;
			return dotStyle;	
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
				&& this.dotname.equals(otherObj.dotname)
				&& this.size == otherObj.size
				&& this.style == otherObj.style
				) {
			return true;
		}
		return false;
	}
	
	public static Paint getGridentColorBrighter(Color color) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { color, GraphicsUtils.deriveBrighter(color) }
			                      );
	}
	public static Paint getGridentColorBrighterTrans(Color color) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { GraphicsUtils.deriveBrighter(color), color }
			                      );
	}
	public static Paint getGridentColorDarker(Color color) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { color, GraphicsUtils.deriveDarker(color) }
			                      );
	}
	public static Paint getGridentColorDarkerTrans(Color color) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { GraphicsUtils.deriveDarker(color) ,color}
			                      );
	}
	public static Paint getGridentColor(Color color1, Color color2) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { color1, color2 }
			                      );
	}
}
