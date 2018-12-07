package com.ogqcorp.metabrowser.content.controller;

import com.ogqcorp.metabrowser.StorageConstants;
import com.ogqcorp.metabrowser.account.dto.UserDTO;
import com.ogqcorp.metabrowser.account.service.UserService;
import com.ogqcorp.metabrowser.analysis.dto.AssetsRequest;
import com.ogqcorp.metabrowser.analysis.dto.KonanVideoRequestDTO;
import com.ogqcorp.metabrowser.analysis.dto.KonanVideoResponseDTO;
import com.ogqcorp.metabrowser.analysis.service.VideoAnalysisService;
import com.ogqcorp.metabrowser.common.ResponseResult;
import com.ogqcorp.metabrowser.content.dto.VideoDTO;
import com.ogqcorp.metabrowser.content.service.ContentService;
import com.ogqcorp.metabrowser.domain.User;
import com.ogqcorp.metabrowser.storage.AWSS3Service;
import com.ogqcorp.metabrowser.storage.StorageService;
import com.ogqcorp.metabrowser.utils.Base62;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class ContentController {
    @Autowired
    private ContentService contentService;

    @Autowired
    private UserService userService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private AWSS3Service awsS3Service;

    @Autowired
    private VideoAnalysisService videoAnalysisService;

    @Value("${vtt.ogq.server.api}")
    private String _OGQ_SERVER_API;

    @GetMapping("/content/videos")
    public String getVideos(Model model, String keyword, @PageableDefault(sort = "id",direction = Sort.Direction.DESC) Pageable pageable){
        Page<VideoDTO> videoDTOs= contentService.findAll(pageable, keyword);

        model.addAttribute("keyword",keyword);
        model.addAttribute("page",videoDTOs);

        return "contents/videoList";
    }



    @GetMapping("/content/videos/detail/{id}")
    public String getVideos(Model model, @PathVariable Long id){

        VideoDTO videoDTO = contentService.findById(id);

        model.addAttribute("board", videoDTO);
        return "contents/videoRead";
    }

    @GetMapping("/content/videos/write")
    public String writeVideos(Model model){

        return "contents/videoWrite";
    }


    @DeleteMapping("/content/videos/{ids}")
    @ResponseBody
    public ResponseEntity deleteVideos(@PathVariable List<Long> ids){

       for(Long id: ids){
           //System.out.println(id);
           contentService.deleteById(id);
       }

        return ResponseEntity.ok().body(new ResponseResult(true,20000, null, ""));
    }

    @PutMapping("/content/videos/{id}/duration/{duration}")
    @ResponseBody
    public ResponseEntity putDuration(@PathVariable Long id, @PathVariable String duration){

        contentService.saveDuration(id, duration);

        return ResponseEntity.ok().body(new ResponseResult(true,20000, null, ""));
    }

    @PostMapping("/content/videos")
    @ResponseBody
    public ResponseEntity postVideos(@Valid VideoDTO videoDTO,
                                           @RequestParam("videoFile") MultipartFile videoFile,
                                           @RequestParam("metaFile") MultipartFile metaFile) throws IOException {


        Path metaFilePath;
        Path videoFilePath;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDTO userDTO = userService.findByEmail(auth.getName());
        videoDTO.setUserId(userDTO.getId());




        if(videoDTO.getAgreeYn() == null || !videoDTO.getAgreeYn().equals("Y")){
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ResponseResult(false,31003, null, ""));
        }

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");


        String formattedDate=Integer.toString(Integer.parseInt(dateFormat.format(date)),36);


        if(!metaFile.isEmpty()) {
            String metaDir = "meta_attach/"+formattedDate;
            metaFilePath = storageService.store(metaFile);
            videoDTO.setMetaFileUrl(metaDir+"/"+metaFilePath.getFileName());


            //awsS3Service.store(metaFilePath,videoDTO.getMetaFileUrl());


            Thread metaUploadThread = new Thread(() -> {
                awsS3Service.store(metaFilePath,videoDTO.getMetaFileUrl(), path -> storageService.delete(path));
            });
            metaUploadThread.start();
        }

        videoFilePath = storageService.store(videoFile);
        String videoDir = "movie_attach/"+formattedDate;
        String videoFileName = ""+videoFilePath.getFileName();
        videoDTO.setVideoFileUrl(videoDir+"/"+videoFileName);
        videoDTO.setVideoFileSize(videoFile.getSize());
        videoDTO.setStatus(100);

        String videoKey = Base62.encode(videoFileName.getBytes());

        VideoDTO resultVideoDTO = contentService.save(videoDTO);
        //String callbackId = Base62.encode(String.format("%018d",resultVideoDTO.getId()).getBytes());
        String callbackId = String.valueOf(resultVideoDTO.getId());

        System.out.println("test="+Base62.encode(String.format("%018d",18).getBytes()));
        System.out.println(resultVideoDTO.getId() + "  " +callbackId + " " + (new String(Base62.decode(callbackId))));

        AssetsRequest assetsRequest = new AssetsRequest();
        assetsRequest.setCallback_url(_OGQ_SERVER_API+"/vtt/analysis/callback/"+callbackId);
        assetsRequest.setRequest_id(callbackId);
        assetsRequest.setVideo_url(StorageConstants.FILE_PATH+videoDTO.getVideoFileUrl());
        /*KonanVideoRequestDTO konanVideoRequestDTO = new KonanVideoRequestDTO();
        konanVideoRequestDTO.setCallbackUrl(_OGQ_SERVER_API+"/vtt/analysis/callback/"+callbackId);
        konanVideoRequestDTO.setRequestId(callbackId);
        konanVideoRequestDTO.setVideoUrl(StorageConstants.FILE_PATH+videoDTO.getVideoFileUrl());*/

        Thread videoUploadThread = new Thread(() -> {
            awsS3Service.store(videoFilePath,videoDTO.getVideoFileUrl(), path ->
            {


                //videoAnalysisService.analyzeVideoTest(konanVideoRequestDTO);
                //videoAnalysisService.analyzeVideo(konanVideoRequestDTO);
                ResponseEntity<KonanVideoResponseDTO> responseEntity = videoAnalysisService.analyzeVideo(assetsRequest);
                resultVideoDTO.setStatus(200);
                resultVideoDTO.setTaggingId(responseEntity.getBody().getUuid());
                contentService.save(resultVideoDTO);
                return storageService.delete(path);
            });

        });
        videoUploadThread.start();




        return ResponseEntity.ok().body(new ResponseResult(true,20000, null, ""));
    }

}
