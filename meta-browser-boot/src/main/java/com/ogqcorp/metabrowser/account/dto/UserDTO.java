package com.ogqcorp.metabrowser.account.dto;

import com.ogqcorp.metabrowser.domain.User;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class UserDTO {
    private Integer id;
    private String userName;
    private String institution;
    @Email(message = "*Please provide a valid Email")
    @NotEmpty(message = "*Please provide an email")
    private String email;
    private String certify;
    @NotEmpty(message = "*Please provide your password")
    private String password;
    public UserDTO(){
    }

    public UserDTO(User accounts){
        this.id = accounts.getId();
        this.userName = accounts.getUserName();
        this.institution = accounts.getInstitution();
        this.email = accounts.getEmail();
        this.certify = accounts.getCertify();

    }

    public UserDTO(Integer id, String userName){
        this.id = id;
        this.userName = userName;
    }

}
