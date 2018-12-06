package com.ogqcorp.metabrowser.domain;

import com.ogqcorp.metabrowser.content.dto.TagDTO;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String str;
    private String strKor;

    public Tag(){}

    public Tag(Long id, String str) {
        this.id = id;
        this.str = str;
    }


    public Tag(TagDTO tagDTO) {
        System.out.println(tagDTO.getId() + "   "+ tagDTO.getStr());
        this.id = tagDTO.getId();
        this.str = tagDTO.getStr();
    }
}
