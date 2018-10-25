package com.ogqcorp.metabrowser.content.dto;

import com.ogqcorp.metabrowser.account.dto.UserDTO;
import com.ogqcorp.metabrowser.StorageConstants;
import com.ogqcorp.metabrowser.domain.Content;
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


    public VideoDTO(Content content, Map<String, UserDTO> usersMap){

        super(content.getId(),content.getUserId(), content.getTitle(), content.getExplanation(), usersMap.get(String.valueOf(content.getId())) );

        this.videoFileSize = content.getVideoFileSize();

        String videoFileUrl = content.getVideoFileUrl();
        if(!videoFileUrl.matches("movie_attach\\/.*")){
            videoFileUrl = "movie_attach/"+content.getId()+"_"+videoFileUrl;
        }

        this.videoFileUrl = content.getVideoFileUrl();
        this.videoDuration = content.getVideoRunningTime();
        this.thumbnailUri = content.getPreviewImagePath();
        this.videoFileName = content.getVideoFileUrl();
        this.videoFileUrl = StorageConstants.FILE_PATH + videoFileUrl;
        this.videoFileDisplaySize = FileUtils.byteCountToDisplaySize(content.getVideoFileSize());
        this.explanation = content.getExplanation();
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
