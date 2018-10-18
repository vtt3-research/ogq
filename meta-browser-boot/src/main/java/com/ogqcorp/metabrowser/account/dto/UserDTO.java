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
    private String userId;
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
        this.userId = accounts.getAccountId();
        this.userName = accounts.getAccountName();
        this.institution = accounts.getOrgan();
        this.email = accounts.getEmail();
        this.certify = accounts.getCertify();

    }

    public UserDTO(Integer id, String userId, String userName){
        this.id = id;
        this.userId = userId;
        this.userName = userName;
    }

}
