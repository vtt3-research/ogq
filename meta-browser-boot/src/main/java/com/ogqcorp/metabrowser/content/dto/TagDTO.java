package com.ogqcorp.metabrowser.content.dto;

import lombok.Data;

@Data
public class TagDTO {
    private Long id;
    private String str;

    public TagDTO(Long id, String str){

        this.id = id;
        this.str = str;
    }

    public TagDTO(){

    }
}
