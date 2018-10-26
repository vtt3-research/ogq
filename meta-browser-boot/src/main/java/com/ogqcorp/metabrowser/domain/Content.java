package com.ogqcorp.metabrowser.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
}
