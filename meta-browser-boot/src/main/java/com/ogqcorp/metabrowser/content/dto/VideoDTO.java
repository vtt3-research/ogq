package com.ogqcorp.metabrowser.content.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ogqcorp.metabrowser.account.dto.UserDTO;
import com.ogqcorp.metabrowser.StorageConstants;
import com.ogqcorp.metabrowser.domain.Content;
import com.ogqcorp.metabrowser.domain.Shot;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class VideoDTO extends ContentDTO{

    private String agreeYn;
    private String entireTags;
    private String explanation;
    private Date lastUpdateDate;
    private String metaFileUrl;
    private String thumbnailUri;
    private Date registeredDate;
    private Long videoFileSize;
    private String videoFileDisplaySize;
    private String videoFileName;
    private String videoFileUrl;
    private String videoDuration;
    private Integer status;
    private Long taggingId;

    private List<?> shots  = new ArrayList<>();;

//    private Map<String, ShotDTO> shot;
    private List<String> tags = new ArrayList<>();

    public VideoDTO(){
        super();

    }


    public VideoDTO(Content content, Map<String, UserDTO> usersMap){

        super(content.getId(),content.getUserId(), content.getTitle(), content.getExplanation(), usersMap.get(String.valueOf(content.getUserId())) );

        this.videoFileSize = content.getVideoFileSize();

        String videoFileUrl = content.getVideoFileUrl();
        if(!videoFileUrl.matches("movie_attach\\/.*")){
            videoFileUrl = "movie_attach/"+content.getId()+"_"+videoFileUrl;
        }

        this.videoDuration = content.getVideoRunningTime();
        this.thumbnailUri = content.getPreviewImagePath();
        this.videoFileName = content.getVideoFileUrl().replaceAll(".*/","");
        this.videoFileUrl = StorageConstants.FILE_PATH + videoFileUrl;
        this.videoFileDisplaySize = FileUtils.byteCountToDisplaySize(content.getVideoFileSize());
        this.explanation = content.getExplanation();
        this.registeredDate = content.getRegisteredDate();
        this.lastUpdateDate = content.getLastUpdateDate();
        this.shots = content.getShots().stream().map(s -> new ShotDTO(s) ).collect(Collectors.toList());
        //this.shot = content.getShots().stream().collect(Collectors.toMap(s -> String.valueOf(s.getStartframeindex()),s -> new ShotDTO(s)));
        this.tags = content.getTags().stream().map(s -> s.getStr()).collect(Collectors.toList());
    }


}
