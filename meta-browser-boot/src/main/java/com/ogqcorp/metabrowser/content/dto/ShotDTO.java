package com.ogqcorp.metabrowser.content.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ogqcorp.metabrowser.domain.Shot;
import lombok.Data;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ShotDTO {
    private Long id;
    private Integer startframeindex;
    private String starttimecode;
    private Double starttime;
    private Integer endframeindex;
    private String endtimecode;
    private Double endtime;
    private String image;

    private String location;

    @JsonIgnore
    private String object;


    private List<Map<String, Object>> shotDetail;

    private List<?> tags;

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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss;SS");
        LocalTime localStarttime = LocalTime.parse(this.starttimecode, formatter);
        LocalTime localEndttime = LocalTime.parse(this.endtimecode, formatter);
        System.out.println(localStarttime.toSecondOfDay());

        this.starttime = Double.parseDouble(String.valueOf(localStarttime.toSecondOfDay()) +"." + String.valueOf(localStarttime.getNano()));
        this.endtime =  Double.parseDouble(String.valueOf(localEndttime.toSecondOfDay()) +"." + String.valueOf(localEndttime.getNano()));

        this.tags = shot.getTags().stream().map(s -> s.getStr()).collect(Collectors.toList());

        ObjectMapper om = new ObjectMapper();

        try {
            this.shotDetail = om.readValue(shot.getObject(), new TypeReference<List<Map<String, Object>>>() {
            });

        }catch (Exception e) {
            e.printStackTrace();
        }

        //this.tags = shot.getTags().stream().map(s -> new TagDTO(s.getId(), s.getStr())).collect(Collectors.toList());


    }
}
