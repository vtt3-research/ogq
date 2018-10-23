package com.ogqcorp.metabrowser.analysis.resourece;

import com.ogqcorp.metabrowser.analysis.dto.ShotDTO;
import com.ogqcorp.metabrowser.analysis.dto.VideoTagDTO;
import com.ogqcorp.metabrowser.analysis.service.VideoAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
public class VideoAnalysisResource {

    @Autowired
    private VideoAnalysisService videoAnalysisService;



    @PutMapping("/vtt/analysys/callback/{contentId}")
    public ResponseEntity callbackVideoAnalysis(@ModelAttribute VideoTagDTO videoTagDTO, @PathVariable Long contentId){

        for(ShotDTO shotDTO: videoTagDTO.getShots()){
            shotDTO.setContentId(contentId);
            videoAnalysisService.save(shotDTO);
        }
        return ResponseEntity.ok(videoTagDTO);
    }

}
