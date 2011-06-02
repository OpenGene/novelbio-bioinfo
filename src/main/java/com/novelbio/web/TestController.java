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
 //@Controller��@Service �Լ� @Repository �� @Component ע��������ǵȼ۵ģ���һ�����Ϊ Spring ������ Bean��
//���� Spring MVC �� Controller ����������һ�� Bean��
//���� @Controller ע���ǲ���ȱ�ٵġ�
//Ҳ������@Component���������<bean class = "org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping"/>
//��ô������@Component
@Component

//������ BbtForumController �߱� Spring MVC Controller ���ܵ��� @RequestMapping ���ע�⡣
//@RequestMapping ���Ա�ע���ඨ�崦���� Controller ���ض��������������
//�����Ա�ע�ڷ���ǩ�������Ա��һ����������з�����
//�ڱ����������� TestController ������/test.htm�������󣬱������("/test.htm")
//@RequestMapping("/test.htm")
//�������@RequestMapping("/test.htm")��spring.xml�����ã�<bean class="org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter"/>
//������ã�spring.xml�����ã�<bean class = "org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping"/>
//�Ҿ�����@RequestMapping�ã���������޸�controller��ʱ����Ҫ�޸�������
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
	private static final String PARAM_MSG = "aaa";//��ѯ�Ĳ�����Ʃ��test.htm?aaa=string
    
	//���Ǿ����ָ�� handleRequestInternal() ��������������
	//@RequestMapping �൱���� POJO ʵ���� Controller �ӿڣ�
	//���ڷ������崦�� @RequestMapping �൱���� POJO ��չ Spring Ԥ����� Controller���� SimpleFormController �ȣ���
	//���������ڣ���spirng֪�������ĸ���������������
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
		//ModelAndView mav = new ModelAndView();//Ĭ�����test��������ͼ
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

