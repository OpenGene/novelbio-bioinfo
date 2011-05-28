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
         
       @RequestMapping(value = "form2",method = RequestMethod.GET)  //���ʵ�ַΪ   ����/servlet/class��value/��value + ��׺
//       @RequestMapping(method = RequestMethod.GET)  
       public ModelAndView get() {
    	   
    	   //���¼��ֿ��Ե����
    	   //1.ָ����ͼ������
           ModelAndView mav = new ModelAndView("form");
           //����������ֱ�Ӳ���һ�� �½�����
           mav.addObject("user", new User());
           //Ҳ����ָ�����������ٲ������
           //mav.addObject( new User());
           
           //����һ��ModelMap��ModelMap�ƺ�����ָ����ͼ��
           //��ô���ǰ���@RequestMapping ���ú�������spring��Ĭ�Ͻ���ͼ��Ӧ��form��ȥ������ֱ����ModelMap�в���һ������ͺá�
           //ע���ʱ��return������ModelMap
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