package com.novelbio.web.model.task;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 计算机的实体类
 * 
 * @author novelbio
 * 
 */
@Document(collection = "computer")
public class Computer implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	private String id;
	private String ip;
	/** 端口 */
	private int port;
	/** 计算机的名字 如:slaver1、master1.. */
	private String name;
	/**
	 * 计算机的类型  0、普通服务器，1、web服务器
	 */
	private int type;
	/** 启用状态 0、未启用 1、已启用 2、已禁用 */
	private int openState;
	/** 本机器可处理任务的总大小 */
	private int enableTaskSize;
	/** 当前处理的总任务大小 */
	private int currentTaskSize;
	/** 机器信息更新时间 */
	private String modifyDate;
	/** 机器描述 */
	private String description;
 
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getOpenState() {
		return openState;
	}

	public void setOpenState(int openState) {
		this.openState = openState;
	}

	public int getEnableTaskSize() {
		return enableTaskSize;
	}

	public void setEnableTaskSize(int enableTaskSize) {
		this.enableTaskSize = enableTaskSize;
	}

	public int getCurrentTaskSize() {
		return currentTaskSize;
	}

	public void setCurrentTaskSize(int currentTaskSize) {
		this.currentTaskSize = currentTaskSize;
	}
	
	public String getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
