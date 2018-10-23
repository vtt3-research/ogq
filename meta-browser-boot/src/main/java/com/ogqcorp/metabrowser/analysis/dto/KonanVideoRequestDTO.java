package com.ogqcorp.metabrowser.analysis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KonanVideoRequestDTO {
    private String requestId;
    private String videoUrl;
    private String callbackUrl;
}
