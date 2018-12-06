package com.ogqcorp.metabrowser.content.service;

import com.ogqcorp.metabrowser.content.dto.TagDTO;
import com.ogqcorp.metabrowser.content.repository.TagRepository;
import com.ogqcorp.metabrowser.domain.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;




    public TagDTO save(String str){
        TagDTO tagDTO  = new TagDTO();

        Tag tag = tagRepository.findByStr(str);
        if(tag == null){
            tag = new Tag();

            tag.setStr(str);
            tag = tagRepository.save(tag);

        }

        tagDTO.setId(tag.getId());
        tagDTO.setStr(tag.getStr());

        return tagDTO;

    }



}
