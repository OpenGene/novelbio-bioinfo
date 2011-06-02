   package com.novelbio.web;  
     
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.novelbio.web.model.User;
     
   @Component
   @RequestMapping(value = "/")
   @SessionAttributes("user")
   public final class ContactController {  
         
       @Autowired  
         private UserValidator userValidator;
       public void setValidator(UserValidator userValidator) {  
           this.userValidator = userValidator;  
       }
         
       @RequestMapping(value = "/form",method = RequestMethod.GET)  //访问地址为   域名/servlet/class的value/本value + 后缀
//       @RequestMapping(method = RequestMethod.GET)  
       public ModelAndView get() {
    	   //以下几种可以的情况
    	   //1.指定视图并返回
           ModelAndView mav = new ModelAndView("form");
           //mav.setViewName("form");
           //可以往里面直接插入一个 新建对象，
      	   User user = new User();
      	   user.setName("fsefs");
      	   user.setText("zongjie");
    	   user.setEmail("fwefe@fewef.com");//这时候表单里面对应选项就会出现字
           mav.addObject("user",user);
           //也可以指定对象名，再插入对象
           //mav.addObject( new User());
           
           //2. 返回一个ModelMap，ModelMap似乎不能指定视图，
           //那么如果前面的@RequestMapping 设置合理，这里spring会默认将视图对应到form上去，所以直接在ModelMap中插入一个对象就好。
           //注意此时的return对象是ModelMap
           //return new ModelMap("user", new User());  
           return mav;
       }  
      
//     @RequestMapping(value = "form22",method = RequestMethod.GET)  //访问地址为   域名/servlet/class的value/本value + 后缀
       @RequestMapping(method = RequestMethod.GET)  
       public ModelMap get(ModelMap model) {
//    	   此时model直接就是form视图,可以往里面添加user对象,可以先设定一些值
//    	   当然也可以ModelAndView mav = new ModelAndView("form");,再往里面添加user
    	   User user = new User();
    	   user.setEmail("fwefe@fewef");//这时候表单里面对应选项就会出现字
           model.addAttribute(user);
           return model;
       }
       
       
       
       
//     @RequestMapping(value = "form",method = RequestMethod.GET)  //访问地址为   域名/servlet/class的value/本value + 后缀
       @RequestMapping(value ="/formhuih",method =RequestMethod.GET )  
       public ModelAndView get(@RequestBody String body) {
    	   System.out.println(body);
    	   //以下几种可以的情况
    	   //1.指定视图并返回
           ModelAndView mav = new ModelAndView("form");
           //mav.setViewName("form");
           //可以往里面直接插入一个 新建对象，
           mav.addObject("user", new User());
           //也可以指定对象名，再插入对象
           //mav.addObject( new User());
           
           //2. 返回一个ModelMap，ModelMap似乎不能指定视图，
           //那么如果前面的@RequestMapping 设置合理，这里spring会默认将视图对应到form上去，所以直接在ModelMap中插入一个对象就好。
           //注意此时的return对象是ModelMap
           //return new ModelMap("userMessage", new UserMessage());  
           return mav;
       }
       
       
       @RequestMapping(value = "/something", method = RequestMethod.PUT)
       public String helloWorld()  {
         return "Hello World";
       }
       
       @RequestMapping( method = RequestMethod.POST)  
       public String post(@ModelAttribute("user") User user, BindingResult result,Model model) {
    	  // Model model 是将post的这个视图整个的传递了进来，可以对其进行一些处理，不过不处理也没关系,该参数可以省
    	   userValidator.validate(user, result);  
           if (result.hasErrors()) { return "form"; }  
             
           // Use the redirect-after-post pattern to reduce double-submits. 
           model.addAttribute("user", user);//装入session
           return "redirect:thanks.htm";  
       }
        static int aa = 1;
       @RequestMapping("thanks")  
       public String thanks() {
    	   if (aa<5) {
    		   aa++;
    		   System.out.println(aa);
    		   return "thanks";
		}
    	  else {
			return "redirect:thanks2.htm";
		}
//    	   return "redirect:thanks2.htm";
    	   
       }
       //将session中的user提取出来
       @RequestMapping(value ="thanks2", method = RequestMethod.GET)  
       public ModelAndView thanks2(@ModelAttribute("user") User user)
       {
    		ModelAndView mav = new ModelAndView("test");
    		//ModelAndView mav = new ModelAndView();//默认添加test控制器视图
    		mav.addObject("message", user.getEmail());
    		mav.addObject("counter", user.getName());
    		System.out.println(mav.getViewName());
    		return mav;
       }
   }  