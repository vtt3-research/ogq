package com.ogqcorp.metabrowser.content.service;

import com.ogqcorp.metabrowser.content.dto.ShotDTO;
import com.ogqcorp.metabrowser.content.dto.TagDTO;
import com.ogqcorp.metabrowser.content.repository.ShotRepository;
import com.ogqcorp.metabrowser.domain.Shot;
import com.ogqcorp.metabrowser.domain.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ShotService {

    @Autowired
    private ShotRepository shotRepository;

    @Autowired
    private TagService tagService;

    public ShotDTO save(ShotDTO shotDTO){

        Shot shot = new Shot();

        if(shot.getId() != null){
            shot.setId(shotDTO.getId());
        }

        shot.setStartframeindex(shotDTO.getStartframeindex());
        shot.setStarttimecode(shotDTO.getStarttimecode());
        shot.setEndframeindex(shotDTO.getEndframeindex());
        shot.setEndtimecode(shotDTO.getEndtimecode());
        shot.setObject(shotDTO.getObject());
        shot.setImage(shotDTO.getImage());
        shot.setLocation(shotDTO.getLocation());



        List<Tag> tags = new ArrayList<Tag>();

        for(String str : shotDTO.getTags()){
            TagDTO tagDTO = tagService.save(str);
            tags.add(new Tag(tagDTO.getId(), tagDTO.getStr()));
        }

        shot.setTags(tags);

        shotRepository.save(shot);

        return shotDTO;
    }

}
