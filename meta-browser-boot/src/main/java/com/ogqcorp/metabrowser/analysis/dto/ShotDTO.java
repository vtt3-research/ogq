package com.ogqcorp.metabrowser.analysis.dto;

import com.ogqcorp.metabrowser.domain.Shot;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ShotDTO {
    private Long contentId;

    private Double shotTime;
    private String seekPos;
    private List<String> tags;

    public ShotDTO(){
    }

    public ShotDTO(Shot shot){
        this.contentId = shot.getContentId();
        this.shotTime = shot.getShotTime();
        this.seekPos = shot.getSeekPos();
        if(shot.getTags() != null && shot.getTags().length() > 0) {
            this.tags = Arrays.stream(shot.getTags().split("\\|")).collect(Collectors.toList());
        }
    }
}
