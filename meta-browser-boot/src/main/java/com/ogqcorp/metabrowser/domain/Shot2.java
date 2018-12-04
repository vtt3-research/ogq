package com.ogqcorp.metabrowser.domain;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Data
@Entity
@IdClass(Shot2Id.class)
public class Shot2 {
    @Id
    private Long contentId;
    @Id
    private Integer startframeindex;
    private String starttimecode;
    @Id
    private Integer endframeindex;
    private String endtimecode;
    private String image;
    private String tags;
    private String location;
}



@Data
class Shot2Id implements Serializable {
    private Long contentId;
    private Integer startframeindex;
    private Integer endframeindex;

}