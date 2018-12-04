package com.ogqcorp.metabrowser.analysis.dto;

import com.ogqcorp.metabrowser.content.dto.ShotDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class VideoTagDTO {
    private List<String> tags;
    private List<ShotDTO> shots;

}
