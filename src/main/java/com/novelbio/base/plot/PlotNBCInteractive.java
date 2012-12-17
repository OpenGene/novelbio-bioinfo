package com.novelbio.base.plot;

import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.plots.XYPlot;

/**
 * ������plot������GRAL��ʵ��
 * @author zong0jie
 *
 */
public abstract class PlotNBCInteractive extends PlotNBC{
	   
    /**
     * set whether axis can be moved when moving or zooming
     * @param move true: can move and zoom X axis
     * false: cannot move and zoom X axis
     */
    boolean Xnavigator = true;
    /**
     * set which axis can be moved when moving or zooming
     * @param move true: can move and zoom Y axis
     * false: cannot move and zoom Y axis
     */
    boolean Ynavigator = true;
    
    /** �ܷ�Ŵ���С */
    boolean zoom = true;
    /** �ܷ��ƶ� */
    boolean pannable = true;
    /** check whether just figure the figure or all the picture area(include the axis margn)  */
	boolean plotareaAll = true;
    /**
     * set whether axis can be moved when moving or zooming
     * @param move true: can move and zoom X axis
     * false: cannot move and zoom X axis
     */
    public void setAxisXNavigator(boolean move) {
    	this.Xnavigator = move;
	}
    /**
     * set which axis can be moved when moving or zooming
     * @param move true: can move and zoom Y axis
     * false: cannot move and zoom Y axis
     */
    public void setAxisYNavigator(boolean move) {
    	this.Ynavigator = move;
	}

    /**
     * Ĭ��Ϊtrue
     * @return
     */
    protected boolean isZoom() {
		return zoom;
	}
    /**
     * Ĭ��Ϊtrue
     * @return
     */
    public void setZoom(boolean zoom) {
		this.zoom = zoom;
	}
    /**
     * �ܷ��ƶ���Ĭ��Ϊtrue
     * @param pannable
     */
    public void setPannable(boolean pannable) {
		this.pannable = pannable;
	}
    /**
     * �ܷ��ƶ���Ĭ��Ϊtrue
     * @param pannable
     */
    public boolean isPannable() {
		return pannable;
	}

	/**
	 * check whether just figure the figure or all the picture area(include the axis margn)
	 * @param plotareaAll default true
	 */
	public void setPlotareaAll(boolean plotareaAll) {
		this.plotareaAll = plotareaAll;
	}
	public boolean isPlotareaAll() {
		return plotareaAll;
	}
	/**
	 * needs check
	 * @return
	 */
	public abstract Drawable getPlot();
	
	
	
	
	
}
