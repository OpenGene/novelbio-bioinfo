package com.novelbio.webtest.example;

import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.webtest.model.User;
import com.novelbio.webtest.validator.UserValidator;
@SessionAttributes("currUser")
@Controller
@RequestMapping("/test")
// 访问地址 http://localhost:8080/Novelbio/test.htm
public final class CntCtrl2 {

	@Autowired
	private UserValidator userValidator;

	public void setValidator(UserValidator userValidator) {
		this.userValidator = userValidator;
	}


	@RequestMapping(method = RequestMethod.GET)
	// 访问地址 http://localhost:8080/Novelbio/test.htm
	public ModelAndView getFormNoparam() {
		// 1.指定视图并返回
		ModelAndView mav = new ModelAndView("form");
		// 可以往里面直接插入一个 新建对象，这样对应的表中就会填上相关的信息
		User user = new User();
		user.setName("fsefs");
		user.setText("zongjie");
		user.setEmail("fwefe@fewef.com");// 这时候表单里面对应选项就会出现字
		mav.addObject("user", user);
		return mav;
	}
	// <如果URL请求中包括"method=tnaknsses"的参数，由本方法进行处理
    @RequestMapping(params = "method=thanksses",method = RequestMethod.GET )//只有当请求以get方法提交时才有结果
    public ModelAndView getThanksPost(Integer topicId) {
    	ModelAndView mav = new ModelAndView("test");
		//ModelAndView mav = new ModelAndView();//默认添加test控制器视图
		System.out.println(mav.getViewName());
		return mav;
    }

    @ModelAttribute("myuser")//向模型对象中添加一个名为items的属性
    public User propUser() {
    	User user = new User();
		user.setName("fsefs");
		user.setText("zongjie");
		user.setEmail("fwefe@fewef.com");// 这时候表单里面对应选项就会出现字
        return user;
    }
  
    
    
    
    //用propUser()中准备好的元素来填充表格
	@RequestMapping("/listAllBoard.htm")
	// 访问地址 http://localhost:8080/Novelbio/test/listAllBoard.htm
	public ModelAndView getFormUrl(ModelMap model) {
		// 以下几种可以的情况
		// 1.指定视图并返回
		ModelAndView mav = new ModelAndView();
		mav.setViewName("form");
		// 可以往里面直接插入一个 新建对象，
		// 也可以指定对象名，再插入对象
		mav.addObject(model.get("myuser"));// 返回一个空表
		return mav;
	}
    
    
    //redirect: 连接传给浏览器，浏览器再次访问，网址会发生变化，参数通过session传递
	//forward：服务器端传输
//    两个只能用一个
    @RequestMapping( method = RequestMethod.POST)  
    public String post(User user, BindingResult result,Model model) {
 	  // Model model 是将post的这个视图整个的传递了进来，可以对其进行一些处理，不过不处理也没关系,该参数可以省
 	   userValidator.validate(user, result);
        if (result.hasErrors()) {
        	return "form";
        }
        model.addAttribute("currUser", user);//装入session,通过session传递，也可以通过url传递
        // Use the redirect-after-post pattern to reduce double-submits. 
        return "redirect:test.htm?method=test2";
//        return "forward:test.htm?method=test2";
    }
//    
//    两个只能用一个
//    @RequestMapping( method = RequestMethod.POST)  
//    public ModelAndView post2(User user, BindingResult result,Model model) {
// 	  // Model model 是将post的这个视图整个的传递了进来，可以对其进行一些处理，不过不处理也没关系,该参数可以省
// 	   userValidator.validate(user, result);
//        if (result.hasErrors()) {
//        	ModelAndView mav = new ModelAndView("form");
//        	user.setName("fsefs");
//        	user.setText("zongjie");
//        	user.setEmail("fwefe@fewef.com");// 这时候表单里面对应选项就会出现字
//        	mav.addObject("user",user);//这里很智能，出错的地方不变的
//        	return mav;
//        }
//        model.addAttribute("user", user);//装入session
//
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("message", "sfefsefefee");
//        ModelAndView mav = new ModelAndView("thanks",map);
////      return mav;
//      return getThanks(2, user.getName()); 
//    }
    
  


    @RequestMapping(params = "method=test")
    // 访问地址：无参数：http://localhost:8080/Novelbio/test.htm
    //有参数  http://localhost:8080/Novelbio/test.htm?method=test&topicId=10&message=aaa
    //参数可以是一个或多个
    public ModelAndView getThanks(Integer topicId,String message) 
    { 
    	ModelAndView mav = new ModelAndView("test");
    	HashMap<String, Object> hashMap = new HashMap<String, Object>();
    	
    	GeneID copedID = new GeneID(message, 9606, false);
    	
    	hashMap.put("message", copedID.getDescription());
    	hashMap.put("counter", topicId);
		mav.addAllObjects(hashMap);
		System.out.println(mav.getViewName());
		return mav;
    }

    @RequestMapping(params = "method=userTest")
       //有参数  http://localhost:8080/Novelbio/test.htm?method=userTest&topicId=10&name=zongjie
    //或者  http://localhost:8080/Novelbio/test.htm?method=userTest&topicId=10&Name=zongjie
    public ModelAndView getThanks(int topicId,User user) {
    	ModelAndView mav = new ModelAndView("test");
    	HashMap<String, Object> hashMap = new HashMap<String, Object>();
    	hashMap.put("message", user.getName());
    	hashMap.put("counter", topicId);
		mav.addAllObjects(hashMap);
		System.out.println(mav.getViewName());
		return mav;
	}

	@RequestMapping(params = "method=userTestID")
	// 将 id 绑定到 topicId，同时加入session
	// 有参数
	// http://localhost:8080/Novelbio/test.htm?method=userTestID&id=10&name=zongjie
	// 或者
	// http://localhost:8080/Novelbio/test.htm?method=userTestID&id=10&Name=zongjie
	public ModelAndView getThanks2(@RequestParam("id") int topicId, User user,
			Model model) {
		ModelAndView mav = new ModelAndView("test");
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("message", user.getName());
		hashMap.put("counter", topicId);
		mav.addAllObjects(hashMap);
		System.out.println(mav.getViewName());
		model.addAttribute("currUser", user);
		return mav;
	}
	

	 // <-- 如果URL请求中包括"method=listAllBoard"的参数，由本方法进行处理
    @RequestMapping(params = "method=test3") //访问地址 http://localhost:8080/Novelbio/test.htm?method=test2
    public ModelAndView getThanks(Map map) {
    	ModelAndView mav = new ModelAndView("test");
    	mav.addObject("message", map.get("name"));
		
		
		return mav;
    }
	
	
	  //先调用含有session的方法getThanks2 地址 http://localhost:8080/Novelbio/test.htm?method=userTestID&id=10&Name=zongjie
	//然后访问  http://localhost:8080/Novelbio/test.htm?method=test2
	 // <-- 如果URL请求中包括"method=listAllBoard"的参数，由本方法进行处理
    @RequestMapping(params = "method=test2") //访问地址 http://localhost:8080/Novelbio/test.htm?method=test2
    public ModelAndView getThanks(@ModelAttribute("currUser") User sessionValue) {
    	ModelAndView mav = new ModelAndView("test");
    	if (sessionValue !=null) {
    		mav.addObject("message", sessionValue.getName());
    		System.out.println(sessionValue.getName());
		}
		
		return mav;
    }

	  //先调用含有session的方法getThanks2 地址 http://localhost:8080/Novelbio/test.htm?method=userTestID&id=10&Name=zongjie
	//然后访问  http://localhost:8080/Novelbio/test.htm?method=test3
	 // <-- 如果URL请求中包括"method=listAllBoard"的参数，由本方法进行处理
    @RequestMapping(params = "method=test3") //访问地址 http://localhost:8080/Novelbio/test.htm?method=test3
    public ModelAndView getThanks(@ModelAttribute("currUser") User sessionValue, SessionStatus status) {
    	ModelAndView mav = new ModelAndView("test");
    	if (sessionValue.getName().equals("test")) {
			status.setComplete(); // 清空session 中放在 session 级别的模型属性数据
			//但是sessionValue已经被赋值，所以依然存在
		}
     	mav.addObject("message", sessionValue.getName());
		System.out.println(sessionValue.getName());
		return mav;
    }

	
	
}
