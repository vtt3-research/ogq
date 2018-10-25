package com.ogqcorp.metabrowser.account.controller;

import com.ogqcorp.metabrowser.account.dto.UserDTO;
import com.ogqcorp.metabrowser.account.service.UserService;
import com.ogqcorp.metabrowser.common.EmailService;
import com.ogqcorp.metabrowser.common.ResponseResult;
import com.ogqcorp.metabrowser.content.service.ContentService;
import com.ogqcorp.metabrowser.domain.User;
import com.ogqcorp.metabrowser.utils.Base62;
import com.ogqcorp.metabrowser.utils.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Controller
public class UserInfoController {

    @Autowired
    private UserService userService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private EmailService emailService;


    @Value("${ogq.mail.register.callback.host}")
    private String REGISTER_CALLBACK_HOST;

    @Value("${spring.mail.username}")
    private String MAIL_USER_NAME;

    @Value("${ogq.mail.username.alias}")
    private String MAIL_USER_NAME_ALIAS;


    @GetMapping("/account/users/detail")
    public String viewUserInfo(Model model, @PageableDefault(sort = "contentId",direction = Sort.Direction.DESC) Pageable pageable){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //model.addAttribute("user",userService.findById(auth.getName()));
        model.addAttribute("page",contentService.findAllByUserId(pageable,auth.getName()));

        return "users/profile";

    }

    @GetMapping("/registration")
    public String viewRegister(){

        return "users/register";
    }

    @PostMapping("/account/users/detail")
    @ResponseBody
    public ResponseEntity postUserInfo(@ModelAttribute UserDTO userDTO){

        if(userDTO.getUserName().length() == 0){
            userDTO.setUserName(userDTO.getUserName().replaceAll("@.*",""));
        }

        userService.save(userDTO.getId(), userDTO.getUserName(), userDTO.getInstitution());

        return ResponseEntity.ok().body(new ResponseResult(true,20000, null, ""));

    }


    @PostMapping("/account/users/detail/password")
    @ResponseBody
    public ResponseEntity postPaasword(String userOldPassword, Integer id, String userPassword){

        System.out.println(id);

        UserDTO userDTO = userService.findById(id);
        if(!userDTO.getPassword().equals(bCryptPasswordEncoder.encode(userOldPassword))){
            return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).body(new ResponseResult(false,30002, null, ""));
        }
        userService.savePassword(id, userPassword);

        return ResponseEntity.ok().body(new ResponseResult(true,20000, null, ""));

    }


    @GetMapping("/registration/users/{email}")
    public ResponseEntity duplicateCheckEmail(@PathVariable String email){

        UserDTO userDTO = userService.findByEmail(email);


        if(userDTO.getCertify() == null){
            return ResponseEntity.ok().body(new ResponseResult(true,20000, null, ""));
        }

        if(userDTO.getCertify().equals("CERTIFIED")){
            return ResponseEntity.badRequest().body(new ResponseResult(false,30002, null, ""));
        }

        return ResponseEntity.ok().body(new ResponseResult(true,20001, null, ""));

    }

    @PostMapping(value="/registration/users")
    @ResponseBody
    @ExceptionHandler(value = UnsupportedEncodingException.class)
    public ResponseEntity register(@ModelAttribute UserDTO paramDTO) throws UnsupportedEncodingException {

        UserDTO userDTO = userService.findByEmail(paramDTO.getEmail());

        if(userDTO.getCertify() != null && userDTO.getCertify().equals("CERTIFIED")){
            return ResponseEntity.badRequest().body(new ResponseResult(false,30002, null, ""));
        }

        userDTO.setUserName(paramDTO.getUserName());
        userDTO.setEmail(paramDTO.getEmail());
        userDTO.setPassword(paramDTO.getPassword());
        userDTO.setInstitution(paramDTO.getInstitution());
        if(userDTO.getUserName().length() == 0){
            userDTO.setUserName(paramDTO.getUserName().replaceAll("@.*",""));
        }







        //String randomStr = UUID.randomUUID().toString().replaceAll("-","");

        String authStr = EncryptionUtils.generateUrl62UUIDv4();
        userDTO.setCertify(bCryptPasswordEncoder.encode(authStr));
        userService.save(userDTO, 4);

        String encodeEmail = Base62.encode(userDTO.getEmail().getBytes());

        String encodeCertify = Base62.encode(userDTO.getCertify().getBytes());

        InternetAddress internetAddress = new InternetAddress(MAIL_USER_NAME, MAIL_USER_NAME_ALIAS);
        Thread sendMailThread = new Thread(() -> {
            emailService.sendMimeMessage(
                    internetAddress,
                    userDTO.getEmail(),
                    userService.getJoinTitle() ,
                    userService.getJoinBody(REGISTER_CALLBACK_HOST, userDTO.getUserName(), encodeEmail,encodeCertify));
        });
        sendMailThread.start();





        return ResponseEntity.ok().body(new ResponseResult(true,20000, null, ""));


    }



    @GetMapping("/account/users/videos/detail/{id}")
    public String redirectContentDetail(@PathVariable Long id){

        return "redirect:/content/videos/detail/"+id;
    }
}
