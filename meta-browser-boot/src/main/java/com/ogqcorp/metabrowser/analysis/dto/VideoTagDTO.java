package com.ogqcorp.metabrowser.analysis.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VideoTagDTO {
    private List<String> tags;
    private List<ShotDTO> shots;

}
