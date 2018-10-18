package com.ogqcorp.metabrowser.content.service;

import com.ogqcorp.metabrowser.account.dto.UserDTO;
import com.ogqcorp.metabrowser.account.service.UserService;
import com.ogqcorp.metabrowser.content.dto.ContentDTO;
import com.ogqcorp.metabrowser.content.dto.VideoDTO;
import com.ogqcorp.metabrowser.content.repository.ContentRepository;
import com.ogqcorp.metabrowser.domain.Contents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserService userService;

    public Page<VideoDTO> findAll(Pageable pageable) {
        //List<Contents> contents = contentRepository.findAll(pageable).getContent();
        Page<Contents> page = contentRepository.findAll(pageable);
        List<Contents> contents = page.getContent();
        int totalElements = (int) page.getTotalElements();
        Map<String, UserDTO> usersMap = userService.getUsersMap();
        //List<VideoDTO> videoDTOs = page.stream().map(s -> new VideoDTO(s)).collect(Page<Contents>);
        return new PageImpl<VideoDTO>(contents.stream().map(s -> new VideoDTO(s, usersMap)).collect(Collectors.toList()), pageable, totalElements);
    }

    public Page<VideoDTO> findAllByUserId(Pageable pageable, String userId) {
        //List<Contents> contents = contentRepository.findAll(pageable).getContent();
        Page<Contents> page = contentRepository.findAllByAccountId(pageable, userId);

        List<Contents> contents = page.getContent();
        int totalElements = (int) page.getTotalElements();
        Map<String, UserDTO> usersMap = userService.getUsersMap();
        //List<VideoDTO> videoDTOs = page.stream().map(s -> new VideoDTO(s)).collect(Page<Contents>);
        return new PageImpl<VideoDTO>(contents.stream().map(s -> new VideoDTO(s, usersMap)).collect(Collectors.toList()), pageable, totalElements);
    }

    public VideoDTO findById(Long id) {
        //List<Contents> contents = contentRepository.findAll(pageable).getContent();

        Optional<Contents> optional = contentRepository.findById(id);

        Map<String, UserDTO> usersMap = userService.getUsersMap();
        VideoDTO videoDTO = optional.map(s -> new VideoDTO(s,usersMap)).orElse(new VideoDTO());

        return videoDTO;
    }

    public void deleteById(Long id){
        contentRepository.deleteById(id);
    }


    public void save(VideoDTO videoDTO){

        Contents contents = new Contents();
        contents.setAccountId(videoDTO.getUserId());
        contents.setAgreeYn(videoDTO.getAgreeYn());
        contents.setContentTitle(videoDTO.getTitle());
        contents.setExplanation(videoDTO.getDescription());
        contents.setMetaFileUrl(videoDTO.getMetaFileUrl());
        contents.setVideoFileUrl(videoDTO.getVideoFileUrl());
        contents.setVideoFileSize(videoDTO.getVideoFileSize());
        contents.setVideoRunningTime(videoDTO.getVideoDuration());

        contentRepository.save(contents);

    }

    public void saveDuration(Long id, String duration){
        Contents contents = contentRepository.findById(id).get();

        contents.setVideoRunningTime(duration);
        contentRepository.save(contents);

    }


}
