package com.ogqcorp.metabrowser.domain;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Data
@Entity
@IdClass(ShotId.class)
public class  Shot {
    @Id
    private Long contentId;
    @Id
    private Double time;
    private Long seekPos;
    private String tags;

    public Shot(Long contentId, Double time){
        this.contentId = contentId;
        this.time = time;
    }
    public Shot(){

    }


}

@Data
class ShotId implements Serializable{
    private Long contentId;
    private Double time;
}