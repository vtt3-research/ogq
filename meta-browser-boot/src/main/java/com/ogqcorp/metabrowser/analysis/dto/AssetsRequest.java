package com.ogqcorp.metabrowser.analysis.dto;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;

@Data
public class AssetsRequest {
    @NotEmpty
    private String request_id;
    @NotEmpty @URL(message = "비디오 URL을 입력하세요")
    private String video_url;
    @NotEmpty @URL(message = "callback URL을 입력하세요")
    private String callback_url;

    public AssetsRequest(){
    }
}
