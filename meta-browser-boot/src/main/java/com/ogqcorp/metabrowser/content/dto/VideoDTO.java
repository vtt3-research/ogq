package com.ogqcorp.metabrowser.content.dto;

import com.ogqcorp.metabrowser.account.dto.UserDTO;
import com.ogqcorp.metabrowser.StorageConstants;
import com.ogqcorp.metabrowser.domain.Contents;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.util.Map;

@Getter
@Setter
public class VideoDTO extends ContentDTO{

    public VideoDTO(){
        super();

    }


    public VideoDTO(Contents contents, Map<String, UserDTO> usersMap){

        super(contents.getContentId(),contents.getAccountId(), contents.getContentTitle(), contents.getExplanation(), usersMap.get(contents.getAccountId()) );

        this.videoFileSize = contents.getVideoFileSize();

        String videoFileUrl = contents.getVideoFileUrl();
        if(!videoFileUrl.matches("movie_attach\\/.*")){
            videoFileUrl = "movie_attach/"+contents.getContentId()+"_"+videoFileUrl;
        }

        this.videoFileUrl = contents.getVideoFileUrl();
        this.videoDuration = contents.getVideoRunningTime();
        this.thumbnailUri = contents.getPreviewImagePath();
        this.videoFileName = contents.getVideoFileUrl();
        this.videoFileUrl = StorageConstants.FILE_PATH + videoFileUrl;
        this.videoFileDisplaySize = FileUtils.byteCountToDisplaySize(contents.getVideoFileSize());
        this.explanation = contents.getExplanation();
    }


    private String agreeYn;
    private String entireTags;
    private String explanation;
    private String lastUpdateDt;
    private String metaFileUrl;
    private String thumbnailUri;
    private String registeredDt;
    private Long videoFileSize;
    private String videoFileDisplaySize;
    private String videoFileName;
    private String videoFileUrl;
    private String videoDuration;

}
