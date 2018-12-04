package com.ogqcorp.metabrowser.content.dto;

import com.ogqcorp.metabrowser.domain.Shot;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ShotDTO {
    private Long id;
    private Integer startframeindex;
    private String starttimecode;
    private Integer endframeindex;
    private String endtimecode;
    private String image;

    private String location;
    private String object;

    private List<String> tags;

    public ShotDTO(){
    }

    public ShotDTO(Shot shot){
        this.id = shot.getId();
        this.startframeindex = shot.getStartframeindex();
        this.starttimecode = shot.getStarttimecode();
        this.endframeindex = shot.getEndframeindex();
        this.endtimecode = shot.getEndtimecode();
        this.image = shot.getImage();
        this.location = shot.getLocation();
        this.object = shot.getObject();

        //this.tags = shot.getTags().stream().map(s -> new TagDTO(s.getId(), s.getStr())).collect(Collectors.toList());


    }
}
