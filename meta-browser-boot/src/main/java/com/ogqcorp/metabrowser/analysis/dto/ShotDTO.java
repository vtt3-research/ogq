package com.ogqcorp.metabrowser.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private Double time;
    @JsonProperty("seek_pos")
    private Long seekPos;
    private List<String> tags;

    public ShotDTO(){
    }

    public ShotDTO(Shot shot){
        this.contentId = shot.getContentId();
        this.time = shot.getTime();
        this.seekPos = shot.getSeekPos();
        if(shot.getTags() != null && shot.getTags().length() > 0) {
            this.tags = Arrays.stream(shot.getTags().split("\\|")).collect(Collectors.toList());
        }
    }
}
