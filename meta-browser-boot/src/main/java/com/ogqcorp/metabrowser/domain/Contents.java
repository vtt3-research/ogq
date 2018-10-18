package com.ogqcorp.metabrowser.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class Contents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;
    private String accountId;
    private String agreeYn;
    private String contentTitle;
    private String entireTags;
    private String explanation;
    private String lastUpdateDt;
    private String metaFileUrl;
    private String previewImagePath;
    private String registeredDt;
    private Long videoFileSize;
    private String videoFileUrl;
    private String videoRunningTime;
}
