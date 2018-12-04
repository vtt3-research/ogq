package com.ogqcorp.metabrowser.domain;

import com.ogqcorp.metabrowser.content.dto.ShotDTO;
import com.ogqcorp.metabrowser.content.dto.TagDTO;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Data
@Entity
public class  Shot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer startframeindex;
    private String starttimecode;
    private Integer endframeindex;
    private String endtimecode;
    private String image;

    private String location;
    private String object;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "shot_tag", joinColumns = @JoinColumn(name = "shot_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Collection<Tag> tags = new ArrayList<Tag>();

    public Shot(){}

    public Shot(ShotDTO shotDTO){
        if(shotDTO.getId() != null){
            this.id = shotDTO.getId();
        }
        this.startframeindex = shotDTO.getStartframeindex();
        this.starttimecode = shotDTO.getStarttimecode();
        this.endframeindex = shotDTO.getEndframeindex();
        this.endtimecode = shotDTO.getEndtimecode();
        this.image = shotDTO.getImage();
        this.location = shotDTO.getLocation();
        this.object = shotDTO.getObject();



    }
}
