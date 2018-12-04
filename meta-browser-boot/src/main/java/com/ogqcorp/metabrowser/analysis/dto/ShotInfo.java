package com.ogqcorp.metabrowser.analysis.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShotInfo {
    private Integer startframeindex;
    private String starttimecode;
    private Integer endframeindex;
    private String endtimecode;
    private String image;
    private List<String> tags;
    private List<String> location;
    private Object object;

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
