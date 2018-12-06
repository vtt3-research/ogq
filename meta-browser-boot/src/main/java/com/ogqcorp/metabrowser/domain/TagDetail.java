package com.ogqcorp.metabrowser.domain;

import com.ogqcorp.metabrowser.content.dto.TagDTO;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tag")
public class TagDetail{
    @Id
    private Long id;

    private String str;
    private String strKor;

    public TagDetail(){}

    public TagDetail(Long id, String str) {
        this.id = id;
        this.str = str;
    }


    public TagDetail(TagDTO tagDTO) {
        System.out.println(tagDTO.getId() + "   "+ tagDTO.getStr());
        this.id = tagDTO.getId();
        this.str = tagDTO.getStr();
    }
}
