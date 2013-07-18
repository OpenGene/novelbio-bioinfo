package com.novelbio.web.model.task;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "taskInfo")
public class TaskInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	/** 任务等待中 */
	public static final int STATE_WAIT = 0;
	/** 任务处理中 */
	public static final int STATE_HANDLING = 1;
	/** 任务完成 */
	public static final int STATE_FINISH = 2;
	/** 任务成功 */
	public static final int STATE_SUCCESS = 3;
	/** 任务失败 */
	public static final int STATE_FAIL = 4;
	/** 任务编号 */
	@Id
	private String taskId;
	/** 任务类型,对应了流程中的节点名 */
	private String type;
	/** 任务大小 */
	private int size;
	/** 任务阶段数 */
	private int stageIndex;
	/** 任务开始时间 */
	private String startDate;
	/** 任务结束时间 */
	private String finishDate;
	/** 执行任务的机器 */
	@DBRef
	private Computer computer;
	/** 任务根路径 */
	private String rootPath;
	/** 用户Id */
	private String userId;
	/**
	 * 任务进度 0-100
	 */
	private int progress;
	/**
	 * 任务状态 {@link TaskInfo.STATE_WAIT}
	 */
	private int state;
	/** 任务参数 */
	private Map<String, String> taskData;
	/** 任务基本信息id */
	private String taskFlowId;
	/** 异常信息 */
	private String failReason;
	
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public int getStageIndex() {
		return stageIndex;
	}

	public void setStageIndex(int stageIndex) {
		this.stageIndex = stageIndex;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(String finishDate) {
		this.finishDate = finishDate;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * 任务进度 
	 * 0-100之间<br>
	 */
	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	/** 任务状态 {@link TaskInfo.STATE_WAIT}　*/
	public int getState() {
		return state;
	}
	/** 设置任务状态 例如 {@link TaskInfo.STATE_WAIT}　*/
	public void setState(int state) {
		this.state = state;
	}

	public Map<String, String> getTaskData() {
		return taskData;
	}
	
	public void setTaskData(Map<String, String> taskData) {
		this.taskData = taskData;
	}
	/** 得到任务根路径 */
	public String getRootPath() {
		return rootPath;
	}
	/** 设置任务根路径 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public Computer getComputer() {
		return computer;
	}

	public void setComputer(Computer computer) {
		this.computer = computer;
	}

	public String getTaskFlowId() {
		return taskFlowId;
	}

	public void setTaskFlowId(String taskFlowId) {
		this.taskFlowId = taskFlowId;
	}

	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}
	
}
