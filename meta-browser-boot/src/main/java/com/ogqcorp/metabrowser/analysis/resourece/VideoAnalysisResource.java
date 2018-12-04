package com.ogqcorp.metabrowser.analysis.resourece;

import com.ogqcorp.metabrowser.analysis.dto.KonanVideoRequestDTO;
import com.ogqcorp.metabrowser.analysis.dto.ShotDTO;
import com.ogqcorp.metabrowser.analysis.dto.ShotInfo;
import com.ogqcorp.metabrowser.analysis.dto.VideoTagDTO;
import com.ogqcorp.metabrowser.analysis.service.VideoAnalysisService;
import com.ogqcorp.metabrowser.utils.Base62;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class VideoAnalysisResource {

    @Autowired
    private VideoAnalysisService videoAnalysisService;



    @PostMapping("/vtt/analysis/callback/{contentId}")
    public ResponseEntity callbackVideoAnalysis(@RequestBody VideoTagDTO videoTagDTO, @PathVariable String contentId){

        System.out.println("callback process start");
        System.out.println(contentId);
        for(ShotInfo shotInfo: videoTagDTO.getShots()){
            System.out.println(shotInfo.getEndframeindex());
            /*String decodeContentId =new String(Base62.decode(contentId));
            shotDTO.setContentId(Long.parseLong(decodeContentId));*/
            //System.out.println("contentId = "+Long.parseLong(decodeContentId));
            //shotInfo.setId(Long.parseLong(contentId));
            videoAnalysisService.save(Long.parseLong(contentId), shotInfo);
        }
        System.out.println("callback process finish");
        return ResponseEntity.ok(videoTagDTO);
    }

    @PutMapping("/vtt/ogq/tagging")
    public ResponseEntity sendMetadataBrowser(@RequestBody KonanVideoRequestDTO konanVideoRequestDTO){

        VideoTagDTO videoTagDTO = new VideoTagDTO();


        System.out.println("--Analysis Start--");
        System.out.println(konanVideoRequestDTO.getRequestId()+ " "+ konanVideoRequestDTO.getCallbackUrl() + " " + konanVideoRequestDTO.getVideoUrl());


        String[] tagStringArr = {"recreation","fireworks","event","caribbean","sea","bay","resort"};
        List<String> tags = Arrays.asList(tagStringArr);
        videoTagDTO.setTags(tags);

        System.out.println("Tag Add");
        List<ShotDTO> shotDTOs = new ArrayList<>();
        ShotDTO shotDTO;
        shotDTO = new ShotDTO();
        shotDTO.setTime(0D);
        shotDTO.setSeekPos(0L);
        shotDTO.setTags(Arrays.asList(tagStringArr));
        shotDTOs.add(shotDTO);

        shotDTO = new ShotDTO();
        shotDTO.setTime(12.03);
        shotDTO.setSeekPos(12233L);
        shotDTO.setTags(Arrays.asList(tagStringArr));
        shotDTOs.add(shotDTO);

        shotDTO = new ShotDTO();
        shotDTO.setTime(17.03);
        shotDTO.setSeekPos(134533L);
        shotDTO.setTags(Arrays.asList(tagStringArr));
        shotDTOs.add(shotDTO);

        //videoTagDTO.setShots(shotDTOs);

        System.out.println("Shot Add");

        RestTemplate restTemplate = new RestTemplate();
        URI uri = URI.create("http://localhost:8080/vtt/analysis/callback/"+konanVideoRequestDTO.getRequestId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<KonanVideoRequestDTO> entity = new HttpEntity(videoTagDTO,headers);
        System.out.println("Send Callback Data : " + "/vtt/analysis/callback/"+konanVideoRequestDTO.getRequestId());
        ResponseEntity resonseEntity = restTemplate.exchange(uri,HttpMethod.PUT, entity,VideoTagDTO.class);
        System.out.println(resonseEntity.getStatusCode());

        System.out.println("--Analysis Finish--");
        return ResponseEntity.ok(videoTagDTO);
    }

}
