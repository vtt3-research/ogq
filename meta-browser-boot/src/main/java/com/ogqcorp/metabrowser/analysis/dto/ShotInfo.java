package com.ogqcorp.metabrowser.analysis.dto;

import com.ogqcorp.metabrowser.content.dto.ShotDTO;
import lombok.Data;

import java.util.List;

@Data
public class ShotInfo{
    private List tags;

    public ShotInfo(){

    }

    /*public ShotDTO(Shot2 shot){
        this.contentId = shot.getContentId();
        this.time = shot.getTime();
        this.seekPos = shot.getSeekPos();
        if(shot.getTags() != null && shot.getTags().length() > 0) {
            this.tags = Arrays.stream(shot.getTags().split("\\|")).collect(Collectors.toList());
        }
    }*/
}
