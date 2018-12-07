package com.ogqcorp.metabrowser.analysis.service;

import com.ogqcorp.metabrowser.analysis.dto.*;
import com.ogqcorp.metabrowser.content.dto.ShotDTO;
import com.ogqcorp.metabrowser.analysis.repository.VideoAnalysisRepository;
import com.ogqcorp.metabrowser.content.dto.TagDTO;
import com.ogqcorp.metabrowser.content.repository.ContentRepository;
import com.ogqcorp.metabrowser.content.service.ContentService;
import com.ogqcorp.metabrowser.content.service.ShotService;
import com.ogqcorp.metabrowser.content.service.TagService;
import com.ogqcorp.metabrowser.domain.Content;
import com.ogqcorp.metabrowser.domain.Shot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class VideoAnalysisService {

    @Value("${vtt.konan.video.metadata.api}")
    private String _VIDEO_TAGGING_URL;

    @Autowired
    private VideoAnalysisRepository videoAnalysisRepository;


    @Autowired
    private ShotService shotService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private ContentRepository contentRepository;

    public List<ShotDTO> findAllByContentId(Long id){


        return null;
        //return StreamSupport.stream(videoAnalysisRepository.findAllByContentId(id).spliterator(),false).map(s -> new ShotInfo(s)).collect(Collectors.toList());
    }

    /*public void save(ShotDTO shotDTO){

        Shot shot = new Shot();

        shot.setContentId(shotDTO.getContentId());
        shot.setSeekPos(shotDTO.getSeekPos());
        shot.setTime(shotDTO.getTime());
        shot.setTags(String.join(",",shotDTO.getTags()));

        videoAnalysisRepository.save(shot);
    }*/

    public void save(Long contentId, VideoTagDTO videoTagDTO){

        Content content = contentRepository.findById(contentId).orElse(new Content());
        if(content.getShots().size() >0 ){
            System.out.println("Exist Shots");
            return;
        }

        List<ShotDTO> shotDTOs = new ArrayList<ShotDTO>();
        List<TagDTO> tagDTOs = new ArrayList<TagDTO>();
        for(String str: videoTagDTO.getTags()) {
            TagDTO tagDTO = tagService.save(str);
            if(tagDTO != null){
                tagDTOs.add(tagDTO);
            }
        }

        for(ShotDTO shotDTO: videoTagDTO.getShots()){
            shotDTOs.add(shotService.save(shotDTO));
        }


        contentService.save(contentId, shotDTOs, tagDTOs);
    }

    public void save(Long contentId, ShotDTO shotDTO){

        Shot shot = new Shot();

        shot.setId(contentId);
        shot.setStartframeindex(shotDTO.getStartframeindex());
        shot.setStarttimecode(shotDTO.getStarttimecode());
        shot.setEndframeindex(shotDTO.getEndframeindex());
        shot.setEndtimecode(shotDTO.getEndtimecode());
        shot.setImage(shotDTO.getImage());
        shot.setLocation(shotDTO.getLocation());
        shot.setObject(shotDTO.getObject());
        //shot.setLocation(String.join(",",shotDTO.getLocation()) );
        //shot.setTags(String.join(",",shotInfo.getTags()));

        videoAnalysisRepository.save(shot);
    }

    public void save(ShotDTO shotDTO){

        Shot shot = new Shot();

        if(shotDTO.getId() != null){
            shot.setId(shotDTO.getId());
        }
        shot.setStartframeindex(shotDTO.getStartframeindex());
        shot.setStarttimecode(shotDTO.getStarttimecode());
        shot.setEndframeindex(shotDTO.getEndframeindex());
        shot.setEndtimecode(shotDTO.getEndtimecode());
        shot.setImage(shotDTO.getImage());
        shot.setLocation(shotDTO.getLocation());
        shot.setObject(shotDTO.getObject());
        //shot.setLocation(String.join(",",shotDTO.getLocation()) );
        //shot.setTags(String.join(",",shotInfo.getTags()));

        videoAnalysisRepository.save(shot);
    }


    public ResponseEntity<KonanVideoResponseDTO> analyzeVideo(AssetsRequest assetsRequest){
        RestTemplate restTemplate = new RestTemplate();




        URI uri = URI.create(_VIDEO_TAGGING_URL);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<KonanVideoRequestDTO> entity = new HttpEntity(assetsRequest,headers);
        ResponseEntity<KonanVideoResponseDTO> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, KonanVideoResponseDTO.class);
        System.out.println(responseEntity.getStatusCode());

        //restTemplate.put(_VIDEO_TAGGING_URL, konanVideoRequestDTO);

        return responseEntity;

    }

    public ResponseEntity<KonanVideoRequestDTO> analyzeVideo(KonanVideoRequestDTO konanVideoRequestDTO){
        RestTemplate restTemplate = new RestTemplate();




        URI uri = URI.create(_VIDEO_TAGGING_URL);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<KonanVideoRequestDTO> entity = new HttpEntity(konanVideoRequestDTO,headers);
        ResponseEntity<KonanVideoRequestDTO> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, KonanVideoRequestDTO.class);
        System.out.println(responseEntity.getStatusCode());

        //restTemplate.put(_VIDEO_TAGGING_URL, konanVideoRequestDTO);

        return responseEntity;


    }

    public void analyzeVideoTest(KonanVideoRequestDTO konanVideoRequestDTO){
        RestTemplate restTemplate = new RestTemplate();

        URI uri = URI.create("http://localhost:8080/vtt/ogq/tagging");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<KonanVideoRequestDTO> entity = new HttpEntity(konanVideoRequestDTO,headers);
        ResponseEntity responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, KonanVideoRequestDTO.class);
        System.out.println(responseEntity.getStatusCode());
    }

}
