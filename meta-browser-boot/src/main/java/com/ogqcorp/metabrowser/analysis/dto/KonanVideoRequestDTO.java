package com.ogqcorp.metabrowser.analysis.dto;

import lombok.Data;

@Data
public class KonanVideoRequestDTO {
    private String request_id;
    private String video_url;
    private String callback_url;
}
