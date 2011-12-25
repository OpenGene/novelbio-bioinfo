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
		user.setEmail("fwefe@fewef.com");// 这时候表单里面对应选项就会出现字
		user.setPassword("fssees");
		//本步骤必须要有，意思将对象传入
		mav.addObject("user", user);
	    mav.addObject("courses", courses);
	    
	    
	    
	  return mav;
	}  
 
	  
    //redirect: 连接传给浏览器，浏览器再次访问，网址会发生变化，参数通过session传递
	//forward：服务器端传输
//    两个只能用一个
    @RequestMapping( method = RequestMethod.POST)  
    public String post(User user, BindingResult result,Model model) {
 	  // Model model 是将post的这个视图整个的传递了进来，可以对其进行一些处理，不过不处理也没关系,该参数可以省
    	
// 	   userValidator.validate(user, result);
//        if (result.hasErrors()) {
//        	return "courseList";
//        }
        model.addAttribute("message", user.getEmail());//装入session,通过session传递，也可以通过url传递
        // Use the redirect-after-post pattern to reduce double-submits. 
        return "test";
//        return "forward:test.htm?method=test2";
    	}
 }