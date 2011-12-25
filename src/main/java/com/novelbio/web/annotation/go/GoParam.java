package com.novelbio.web.annotation.go;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.novelbio.database.mapper.geneanno.MapFSTaxID;
import com.novelbio.database.service.servgeneanno.ServNCBIID;

import org.aopalliance.intercept.*;
import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;
import org.springframework.web.multipart.MultipartFile;
/**
 * GOµÄ²ÎÊý
 * @author zong0jie
 *
 */
public class GoParam {
	boolean blast;
	int queryTaxID = 0;
	int blastTaxID = 0;
	boolean elimGo = true;
	boolean novelGo = false;
	boolean cluster = false;

	String goType = "";
	
	int accCol = 1;
	int ExpCol = 2;
	double upValue = 0;
	double downValue = 0;
	MultipartFile inputFile = null;
	String bgFile = "";
	
	public void setBlast(boolean blast) {
		this.blast = blast;
	}
	public boolean isBlast() {
		return blast;
	}
	
	public void setAccCol(int accCol) {
		this.accCol = accCol;
	}
	public int getAccCol() {
		return accCol;
	}
	public void setBgFile(String bgFile) {
		this.bgFile = bgFile;
	}
	public String getBgFile() {
		return bgFile;
	}
	public void setBlastTaxID(int blastTaxID) {
		this.blastTaxID = blastTaxID;
	}
	public int getBlastTaxID() {
		return blastTaxID;
	}
	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}
	public boolean isCluster() {
		return cluster;
	}
	public void setDownValue(double downValue) {
		this.downValue = downValue;
	}
	public double getDownValue() {
		return downValue;
	}
	public void setElimGo(boolean elimGo) {
		this.elimGo = elimGo;
	}
	public boolean isElimGo() {
		return elimGo;
	}
	public void setNovelGo(boolean novelGo) {
		this.novelGo = novelGo;
	}
	public boolean isNovelGo() {
		return novelGo;
	}
	public void setExpCol(int expCol) {
		ExpCol = expCol;
	}
	public int getExpCol() {
		return ExpCol;
	}
	public void setGoType(String goType) {
		this.goType = goType;
	}
	public String getGoType() {
		return goType;
	}
	public void setInputFile(MultipartFile inputFile) {
		this.inputFile = inputFile;
	}
	public MultipartFile getInputFile() {
		return inputFile;
	}
	public void setQueryTaxID(int queryTaxID) {
		this.queryTaxID = queryTaxID;
	}
	public int getQueryTaxID() {
		return queryTaxID;
	}
	public void setUpValue(double upValue) {
		this.upValue = upValue;
	}
	public double getUpValue() {
		return upValue;
	}
	

}
