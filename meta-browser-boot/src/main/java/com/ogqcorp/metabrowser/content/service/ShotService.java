package com.ogqcorp.metabrowser.content.service;

import com.ogqcorp.metabrowser.analysis.dto.ShotInfo;
import com.ogqcorp.metabrowser.content.dto.ShotDTO;
import com.ogqcorp.metabrowser.content.dto.TagDTO;
import com.ogqcorp.metabrowser.content.repository.ShotRepository;
import com.ogqcorp.metabrowser.domain.Shot;
import com.ogqcorp.metabrowser.domain.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShotService {

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private TagService tagService;


    public ShotDTO save(ShotDTO shotDTO){

        Shot shot = new Shot();

        if(shotDTO.getId() != null){
            shot.setId(shotDTO.getId());
        }

        shot.setStartframeindex(shotDTO.getStartframeindex());
        shot.setStarttimecode(shotDTO.getStarttimecode());
        shot.setEndframeindex(shotDTO.getEndframeindex());
        shot.setEndtimecode(shotDTO.getEndtimecode());
        shot.setObject(shotDTO.getObject());
        shot.setImage(shotDTO.getImage());
        shot.setLocation(shotDTO.getLocation());



        List<TagDTO> tagsDTOs = new ArrayList<>();

        for(Object str : shotDTO.getTags()){
            TagDTO tagDTO = tagService.save((String) str);
            tagsDTOs.add(tagDTO);


        }
        shot = shotRepository.save(shot);


        shotDTO.setTags(new ArrayList<TagDTO>());
        shotDTO.setTags(tagsDTOs);

//        shot.setTags(tagsDTOs.stream().map(s -> new Tag(s)).collect(Collectors.toSet()));
//        shot = shotRepository.save(shot);
        shotDTO.setId(shot.getId());

        return shotDTO;
    }

}
