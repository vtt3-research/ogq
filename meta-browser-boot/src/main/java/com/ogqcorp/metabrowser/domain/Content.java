package com.ogqcorp.metabrowser.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer userId;
    private String agreeYn;
    private String title;
    private String entireTags;
    private String explanation;
    private Date lastUpdateDate;
    private String metaFileUrl;
    private String previewImagePath;
    private Date registeredDate;
    private Long videoFileSize;
    private String videoFileUrl;
    private String videoRunningTime;
    private Integer status;


    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "content_shot", joinColumns = @JoinColumn(name = "content_id"), inverseJoinColumns = @JoinColumn(name = "shot_id"))
    private Collection<Shot> shots = new ArrayList<Shot>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "content_tag", joinColumns = @JoinColumn(name = "content_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Collection<Tag> tags = new ArrayList<Tag>();;
}
