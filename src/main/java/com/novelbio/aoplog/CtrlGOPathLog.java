package com.novelbio.aoplog;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.novelbio.nbcgui.controltest.CtrlGO;
import com.novelbio.nbcgui.controltest.CtrlGOPath;

/**
 * 给GO添加日志等
 * @author zong0jie
 *
 */
@Component
@Aspect
public class CtrlGOPathLog {
	 @Pointcut("execution (* com.novelbio.nbcgui.controltest.CtrlGOPath.running()) && target(ctrlGOPath)")
	 public void logRun(CtrlGOPath ctrlGOPath) {
		 
		 
		 
	 }
	 
	 
	 private void copeGO(CtrlGO ctrlGO) {
		 ctrlGO.
	 }
}
