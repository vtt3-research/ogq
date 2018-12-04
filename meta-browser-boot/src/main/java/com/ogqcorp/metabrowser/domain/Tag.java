package com.ogqcorp.metabrowser.domain;

import com.ogqcorp.metabrowser.content.dto.TagDTO;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class Tag {

    private Long id;
    private String str;
    private String strKor;

    public Tag(){}

    public Tag(Long id, String str) {
        this.id = id;
        this.str = str;
    }


    public Tag(TagDTO tagDTO) {
        this.id = tagDTO.getId();
        this.str = tagDTO.getStr();
    }
}
