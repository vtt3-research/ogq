package com.ogqcorp.metabrowser.analysis.service;

import com.ogqcorp.metabrowser.analysis.dto.AssetsRequest;
import com.ogqcorp.metabrowser.analysis.dto.KonanVideoRequestDTO;
import com.ogqcorp.metabrowser.analysis.dto.ShotDTO;
import com.ogqcorp.metabrowser.analysis.repository.VideoAnalysisRepository;
import com.ogqcorp.metabrowser.domain.Shot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class VideoAnalysisService {

    @Value("${vtt.konan.video.metadata.api}")
    private String _VIDEO_TAGGING_URL;

    @Autowired
    private VideoAnalysisRepository videoAnalysisRepository;


    public List<ShotDTO> findAllByContentId(Long id){


        return StreamSupport.stream(videoAnalysisRepository.findAllByContentId(id).spliterator(),false).map(s -> new ShotDTO(s)).collect(Collectors.toList());
    }

    public void save(ShotDTO shotDTO){

        Shot shot = new Shot();

        shot.setContentId(shotDTO.getContentId());
        shot.setSeekPos(shotDTO.getSeekPos());
        shot.setTime(shotDTO.getTime());
        shot.setTags(String.join(",",shotDTO.getTags()));

        videoAnalysisRepository.save(shot);
    }


    public void analyzeVideo(AssetsRequest assetsRequest){
        RestTemplate restTemplate = new RestTemplate();




        URI uri = URI.create(_VIDEO_TAGGING_URL);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<KonanVideoRequestDTO> entity = new HttpEntity(assetsRequest,headers);
        ResponseEntity responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, AssetsRequest.class);
        System.out.println(responseEntity.getStatusCode());

        //restTemplate.put(_VIDEO_TAGGING_URL, konanVideoRequestDTO);


    }

    public void analyzeVideo(KonanVideoRequestDTO konanVideoRequestDTO){
        RestTemplate restTemplate = new RestTemplate();




        URI uri = URI.create(_VIDEO_TAGGING_URL);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<KonanVideoRequestDTO> entity = new HttpEntity(konanVideoRequestDTO,headers);
        ResponseEntity responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, KonanVideoRequestDTO.class);
        System.out.println(responseEntity.getStatusCode());

        //restTemplate.put(_VIDEO_TAGGING_URL, konanVideoRequestDTO);


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
