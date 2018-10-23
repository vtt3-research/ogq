package com.ogqcorp.metabrowser.analysis.service;

import com.ogqcorp.metabrowser.analysis.dto.KonanVideoRequestDTO;
import com.ogqcorp.metabrowser.analysis.dto.ShotDTO;
import com.ogqcorp.metabrowser.analysis.repository.VideoAnalysisRepository;
import com.ogqcorp.metabrowser.domain.Shot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class VideoAnalysisService {

    @Value("${vtt.konan.video.metadata.api}")
    private String _VIDEO_TAGGING_URL;

    @Autowired
    private VideoAnalysisRepository videoAnalysisRepository;


    public List<ShotDTO> findAllById(Long id){


        return StreamSupport.stream(videoAnalysisRepository.findAllById(id).spliterator(),false).map(s -> new ShotDTO(s)).collect(Collectors.toList());
    }

    public void save(ShotDTO shotDTO){

        Shot shot = new Shot();

        shot.setContentId(shotDTO.getContentId());
        shot.setSeekPos(shotDTO.getSeekPos());
        shot.setTime(shotDTO.getTime());
        shot.setTags(String.join(",",shotDTO.getTags()));

        videoAnalysisRepository.save(shot);
    }

    public void analyzeVideo(KonanVideoRequestDTO konanVideoRequestDTO){
        RestTemplate restTemplate = new RestTemplate();


        restTemplate.put(_VIDEO_TAGGING_URL, konanVideoRequestDTO);


    }

}
