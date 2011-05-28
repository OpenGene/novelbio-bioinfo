   package com.novelbio.web;  
     
   import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Component;
   import org.springframework.stereotype.Controller;  
   import org.springframework.validation.BindingResult;  
   import org.springframework.web.bind.annotation.ModelAttribute;  
   import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.bind.annotation.RequestMethod;  
import org.springframework.web.servlet.ModelAndView;

import com.novelbio.web.model.User;
     
   @Component
   @RequestMapping(value = "form")
   public final class ContactController {  
         
       @Autowired  
         private UserValidator userValidator;
       public void setValidator(UserValidator userValidator) {  
           this.userValidator = userValidator;  
           
       }
         
       @RequestMapping(value = "form2",method = RequestMethod.GET)  //访问地址为   域名/servlet/class的value/本value + 后缀
//       @RequestMapping(method = RequestMethod.GET)  
       public ModelAndView get() {
    	   
    	   //以下几种可以的情况
    	   //1.指定视图并返回
           ModelAndView mav = new ModelAndView("form");
           //可以往里面直接插入一个 新建对象，
           mav.addObject("user", new User());
           //也可以指定对象名，再插入对象
           //mav.addObject( new User());
           
           //返回一个ModelMap，ModelMap似乎不能指定视图，
           //那么如果前面的@RequestMapping 设置合理，这里spring会默认将视图对应到form上去，所以直接在ModelMap中插入一个对象就好。
           //注意此时的return对象是ModelMap
           //return new ModelMap("userMessage", new UserMessage());  

           return mav;
       }  
         
       @RequestMapping( method = RequestMethod.POST)  
       public String post(@ModelAttribute("user") User user,  
               BindingResult result) {  
             
    	   userValidator.validate(user, result);  
           if (result.hasErrors()) { return "form"; }  
             
           // Use the redirect-after-post pattern to reduce double-submits.  
           return "redirect:thanks";  
       }  
         
       @RequestMapping("/thanks")  
       public void thanks() {  
       }  
   }  