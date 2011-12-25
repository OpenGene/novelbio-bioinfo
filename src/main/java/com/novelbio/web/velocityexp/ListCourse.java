package com.novelbio.web.velocityexp;

import java.util.List;  

import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;  

import com.novelbio.web.model.User;
import com.novelbio.web.validator.UserValidator;
 @Controller
@RequestMapping("/test222")
public class ListCourse{
	 @Autowired
	private CourseService courseService;  
	 

		@Autowired
		private UserValidator userValidator; 
	 
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handleRequest() throws Exception {  
	    List<Course> courses = courseService.getAllCourses();
	    ModelAndView mav = new ModelAndView("courseList");
//	    mav.addObject("courses", courses);
	    
		User user = new User();
		user.setName("fsefs");
		user.setText("zongjie");
		user.setEmail("fwefe@fewef.com");// ��ʱ��������Ӧѡ��ͻ������
		user.setPassword("fssees");
		//���������Ҫ�У���˼��������
		mav.addObject("user", user);
	    mav.addObject("courses", courses);
	    
	    
	    
	  return mav;
	}  
 
	  
    //redirect: ���Ӵ����������������ٴη��ʣ���ַ�ᷢ���仯������ͨ��session����
	//forward���������˴���
//    ����ֻ����һ��
    @RequestMapping( method = RequestMethod.POST)  
    public String post(User user, BindingResult result,Model model) {
 	  // Model model �ǽ�post�������ͼ�����Ĵ����˽��������Զ������һЩ��������������Ҳû��ϵ,�ò�������ʡ
    	
// 	   userValidator.validate(user, result);
//        if (result.hasErrors()) {
//        	return "courseList";
//        }
        model.addAttribute("message", user.getEmail());//װ��session,ͨ��session���ݣ�Ҳ����ͨ��url����
        // Use the redirect-after-post pattern to reduce double-submits. 
        return "test";
//        return "forward:test.htm?method=test2";
    	}
 }