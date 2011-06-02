package com.novelbio.web;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

import com.novelbio.base.dataOperate.TxtReadandWrite;
/**
* Adds a greeting message to the model and increases a counter by one every
* time this controller is invoked.
*/
 //@Controller、@Service 以及 @Repository 和 @Component 注解的作用是等价的：将一个类成为 Spring 容器的 Bean。
//由于 Spring MVC 的 Controller 必须事先是一个 Bean，
//所以 @Controller 注解是不可缺少的。
//也可以是@Component，如果采用<bean class = "org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping"/>
//那么必须是@Component
@Component

//真正让 BbtForumController 具备 Spring MVC Controller 功能的是 @RequestMapping 这个注解。
//@RequestMapping 可以标注在类定义处，将 Controller 和特定请求关联起来；
//还可以标注在方法签名处，以便进一步对请求进行分流。
//在本处，我们让 TestController 关联“/test.htm”的请求，必须添加("/test.htm")
//@RequestMapping("/test.htm")
//如果采用@RequestMapping("/test.htm")，spring.xml中配置：<bean class="org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter"/>
//如果不用，spring.xml中配置：<bean class = "org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping"/>
//我觉得用@RequestMapping好，方便后期修改controller的时候不需要修改请求名
public class TestController extends AbstractWizardFormController {
	/**
	* Stores the counter value. This will only work when the controller is a
	* singleton. The application context should be used to make this more
	* solid.
	*/
	private static int counter = 0;
	/**
	* The parameter string that is used to retrieve the greeting message from
	* the request.
	*/
	private static final String PARAM_MSG = "aaa";//查询的参数，譬如test.htm?aaa=string
    
	//我们具体地指定 handleRequestInternal() 方法来处理请求
	//@RequestMapping 相当于让 POJO 实现了 Controller 接口，
	//而在方法定义处的 @RequestMapping 相当于让 POJO 扩展 Spring 预定义的 Controller（如 SimpleFormController 等）。
	//这个必须存在，让spirng知道该用哪个方法来接受请求
	@RequestMapping
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws Exception 
	{

//		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
//		txtReadandWrite.setParameter("/media/winE/Bioinformatics/Kegg/genes/organisms/ath/ath_cazy.list", false, true);
//		BufferedReader aaString = txtReadandWrite.readfile();
//		String aa=aaString.readLine();
//		

		String message = ServletRequestUtils.getRequiredStringParameter(req, PARAM_MSG);
		increaseCounter();
		ModelAndView mav = new ModelAndView("test");
		//ModelAndView mav = new ModelAndView();//默认添加test控制器视图
		mav.addObject("message", message);
		mav.addObject("counter", counter);
		System.out.println(mav.getViewName());
		return mav;
	}
	/**
	* A very simplistic counter implementation.
	*/
	private void increaseCounter() {
		counter++;
	}
	@Override
	protected ModelAndView processFinish(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, BindException arg3)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}

