package com.ogqcorp.metabrowser.analysis.service;

import com.ogqcorp.metabrowser.analysis.dto.ShotDTO;
import com.ogqcorp.metabrowser.analysis.repository.VideoAnalysisRepository;
import com.ogqcorp.metabrowser.domain.Shot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class VideoAnalysisService {

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

}
