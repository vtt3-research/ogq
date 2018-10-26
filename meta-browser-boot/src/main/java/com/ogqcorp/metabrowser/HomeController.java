package com.ogqcorp.metabrowser;

import com.ogqcorp.metabrowser.account.dto.UserDTO;
import com.ogqcorp.metabrowser.account.service.UserService;
import com.ogqcorp.metabrowser.utils.Base62;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;


    @GetMapping({"/","/home"})
    public String home() {
        return "redirect:/content/videos";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth.getName());
        if(auth.getName().equals("anonymousUser")){
            return "login";
        }
        return "redirect:/home";
    }

    @GetMapping("/authentications/users/{authKey}/autifications/{code}")
    public String authentication(@PathVariable String authKey, @PathVariable String code) {

        String email = new String(Base62.decode(authKey));
        String authCode = new String(Base62.decode(code));
        UserDTO userDTO = userService.findByEmail(email);


        if(userDTO.getCertify().equals(authCode)){
            userService.saveAuth(userDTO.getId(),"CERTIFIED");
            return "redirect:/authentications?status=success";
        }else if(userDTO.getCertify().equals("CERTIFIED")){
            return "redirect:/authentications?status=expired";
        }

        return "redirect:/authentications?status=error";
    }

    @GetMapping("/authentications")
    public String authentication() {
        return "authentication";
    }


}
