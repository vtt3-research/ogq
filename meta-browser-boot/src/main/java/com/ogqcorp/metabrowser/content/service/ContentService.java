package com.ogqcorp.metabrowser.content.service;

import com.ogqcorp.metabrowser.account.dto.UserDTO;
import com.ogqcorp.metabrowser.account.service.UserService;
import com.ogqcorp.metabrowser.content.dto.VideoDTO;
import com.ogqcorp.metabrowser.content.repository.ContentRepository;
import com.ogqcorp.metabrowser.domain.Content;
import com.ogqcorp.metabrowser.domain.Shot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserService userService;

    public Page<VideoDTO> findAll(Pageable pageable) {
        //List<Contents> contents = contentRepository.findAll(pageable).getContent();
        Page<Content> page = contentRepository.findAll(pageable);
        List<Content> contents = page.getContent();
        int totalElements = (int) page.getTotalElements();
        Map<String, UserDTO> usersMap = userService.getUsersMap();
        //List<VideoDTO> videoDTOs = page.stream().map(s -> new VideoDTO(s)).collect(Page<Contents>);
        return new PageImpl<VideoDTO>(contents.stream().map(s -> new VideoDTO(s, usersMap)).collect(Collectors.toList()), pageable, totalElements);
    }

    public Page<VideoDTO> findAllByUserId(Pageable pageable, Integer userId) {
        //List<Contents> contents = contentRepository.findAll(pageable).getContent();
        Page<Content> page = contentRepository.findAllByUserId(pageable, userId);

        List<Content> contents = page.getContent();
        int totalElements = (int) page.getTotalElements();
        Map<String, UserDTO> usersMap = userService.getUsersMap();
        //List<VideoDTO> videoDTOs = page.stream().map(s -> new VideoDTO(s)).collect(Page<Contents>);
        return new PageImpl<VideoDTO>(contents.stream().map(s -> new VideoDTO(s, usersMap)).collect(Collectors.toList()), pageable, totalElements);
    }

    public VideoDTO findById(Long id) {
        //List<Contents> contents = contentRepository.findAll(pageable).getContent();

        Optional<Content> optional = contentRepository.findById(id);

        Map<String, UserDTO> usersMap = userService.getUsersMap();
        VideoDTO videoDTO = optional.map(s -> new VideoDTO(s,usersMap)).orElse(new VideoDTO());

        return videoDTO;
    }

    public void deleteById(Long id){
        contentRepository.deleteById(id);
    }


    public VideoDTO save(VideoDTO videoDTO){

        Content content = new Content();
        if(videoDTO.getId() != null){
            content.setId(videoDTO.getId());
        }
        content.setUserId(videoDTO.getUserId());
        content.setAgreeYn(videoDTO.getAgreeYn());
        content.setTitle(videoDTO.getTitle());
        content.setExplanation(videoDTO.getDescription());
        content.setMetaFileUrl(videoDTO.getMetaFileUrl());
        content.setVideoFileUrl(videoDTO.getVideoFileUrl());
        content.setVideoFileSize(videoDTO.getVideoFileSize());
        content.setVideoRunningTime(videoDTO.getVideoDuration());
        content.setStatus(videoDTO.getStatus());
        content.setLastUpdateDate(new Date());


        content = contentRepository.save(content);

        videoDTO.setRegisteredDate(content.getRegisteredDate());
        videoDTO.setId(content.getId());
        return videoDTO;


    }

    public void saveDuration(Long id, String duration){
        Content content = contentRepository.findById(id).get();

        content.setVideoRunningTime(duration);
        contentRepository.save(content);

    }


}
