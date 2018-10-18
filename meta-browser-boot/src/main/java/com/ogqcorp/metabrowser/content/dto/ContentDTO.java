package com.ogqcorp.metabrowser.content.dto;

import com.ogqcorp.metabrowser.account.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentDTO {
    private Long id;
    private String userId;
    private String description;
    private String title;
    private UserDTO userDTO;

    protected ContentDTO(){}

    protected ContentDTO(Long id, String userId, String title, String description, UserDTO userDTO){
        this.id = id;
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.userDTO = userDTO;
    }
}
