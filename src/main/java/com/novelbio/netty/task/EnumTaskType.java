package com.novelbio.netty.task;

import com.novelbio.netty.task.thread.TaskThreadGATKCalling;
import com.novelbio.netty.task.thread.TaskThreadGATKRealign;
import com.novelbio.netty.task.thread.TaskThreadGatkDuplicate;
import com.novelbio.netty.task.thread.TaskThreadGoAnalysis;
import com.novelbio.netty.task.thread.TaskThreadMapping;
import com.novelbio.netty.task.thread.TaskThreadRScript;
import com.novelbio.web.model.task.TaskInfo;
/**
 * 根据不同的任务类型选择调用不同的线程
 * 
 * @author novelbio
 * 
 */
public enum EnumTaskType {

	// TODO 完善这个枚举
	GoAnalysis("GoAnalysis"), Mapping("Mapping"), RScript("RScript"), GatkDuplicate("GatkDuplicate"),
	GatkRealign("GatkRealign"),GatkCalling("GatkCalling");
	
	String type = "";

	EnumTaskType(String type) {
		this.type = type;
	}

	/**
	 * 任务类型的对照关系 忽略大小写
	 * @return
	 */
	private static EnumTaskType get(String type) {
		EnumTaskType[] values = EnumTaskType.values();
		for (EnumTaskType object : values) {
			if (object.type.toLowerCase().equals(type.toLowerCase())) {
				return object;
			}
		}
		return null;
	}

	public static Runnable getTaskThread(TaskInfo taskInfo) {
		Runnable taskThread = null;
		EnumTaskType enumTaskType = get(taskInfo.getType());
		if (enumTaskType == EnumTaskType.GoAnalysis) {
			taskThread = new TaskThreadGoAnalysis(taskInfo);
		} else if(enumTaskType == EnumTaskType.Mapping) {
			taskThread = new TaskThreadMapping(taskInfo);
		} else if(enumTaskType == EnumTaskType.RScript) {
			taskThread = new TaskThreadRScript(taskInfo);
		} else if(enumTaskType == EnumTaskType.GatkDuplicate) {
			taskThread = new TaskThreadGatkDuplicate(taskInfo);
		} else if(enumTaskType == EnumTaskType.GatkRealign) {
			taskThread = new TaskThreadGATKRealign(taskInfo);
		} else if(enumTaskType == EnumTaskType.GatkCalling) {
			taskThread = new TaskThreadGATKCalling(taskInfo);
		}
		return taskThread;
	}
}