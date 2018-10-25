package com.ogqcorp.metabrowser.account.service;

import com.ogqcorp.metabrowser.account.dto.UserDTO;
import com.ogqcorp.metabrowser.account.repository.RoleRepository;
import com.ogqcorp.metabrowser.account.repository.UserRepository;
import com.ogqcorp.metabrowser.domain.Role;
import com.ogqcorp.metabrowser.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private static String JOIN_TITLE = "메타데이터 브라우저 회원가입을 축하합니다.";

    private static String ID_TITLE = "\t[요청] 아이디 찾기 요청입니다.";
    private static String ID_BODY = "<BR>" +
            "<BR>관리자님 안녕하세요.\n" +
            "<BR>\n" +
            "<BR>메타데이터 회원님이 아래 정보로 아이디 찾기를 요청하였습니다.\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>소속: \"#ORGAN#\"\n" +
            "<BR>\n" +
            "<BR>이름: \"#USERNAME#\"\n" +
            "<BR>\n" +
            "<BR>이메일: \"#MAIL_ADDRESS#\"\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>감사합니다.\n" +
            "<BR>\n" +
            "<BR>(주)OGQ 드림" +
            "<BR>";

    private final static String PW_TITLE = "메타데이터 브라우저 임시 비밀번호 발급 알림입니다.";
    private final static String PW_BODY = "<BR>" +
            "<BR>\"#USERNAME#\"님 안녕하세요.\n" +
            "<BR>\n" +
            "<BR>메타데이터 브라우저 운영자 입니다.\n" +
            "<BR>\n" +
            "<BR>임시 비밀번호를 보내드립니다.\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>#PASSWORD#" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>비밀번호는 \"마이페이지\"에 접근하시어 변경이 가능합니다.\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>감사합니다.\n" +
            "<BR>\n" +
            "<BR>(주)OGQ 드림" +
            "<BR>";


    public List<UserDTO> findAll(){
        Iterable<User> iterable = userRepository.findAll();


        //List<UserDTO> userDTOs = Stream.of(iterable).map(account -> new UserDTO(account.iterator().next())).collect(Collectors.toList());
        List<UserDTO> userDTOs = StreamSupport.stream(iterable.spliterator(),false).map(user -> new UserDTO(user)).collect(Collectors.toList());
        return userDTOs;
    }

/*

    public UserDTO findById(String id){

        return userRepository.findById(id).map(s -> new UserDTO(s)).orElse(new UserDTO());
    }
*/


    public UserDTO findById(Integer id){

        return userRepository.findById(id).map(s -> {
            UserDTO userDTO = new UserDTO(s);
            userDTO.setPassword(s.getPassword());
            return userDTO;
        }).orElse(new UserDTO());
    }


    public UserDTO findByEmail(String email){
        return userRepository.findByEmail(email).map(s -> new UserDTO(s)).orElse(new UserDTO());
    }


    public User findByEntityId(Integer id){
        Optional<User> optionalUser = userRepository.findById(id);

        return optionalUser.orElse(new User());
    }


    public Map<String, UserDTO> getUsersMap(){

        Iterable<User> iterable = userRepository.findAll();

        Map<String, UserDTO> userDTOmap = StreamSupport.stream(iterable.spliterator(),false).map(user -> new UserDTO(user)).collect(Collectors.toMap(user -> String.valueOf(user.getId()), u->u));

        return userDTOmap;
    }

    public User saveAccount(User user){
        User res = userRepository.save(user);
        return res;
    }


    public String getJoinTitle(){

        return JOIN_TITLE;
    }

    public String getJoinBody(String host, String userName, String email, String certification){
        String message =  "<BR>" +
                "<BR>\""+userName+"\"님 안녕하세요.\n" +
                "<BR>\n" +
                "<BR>메타데이터 브라우저에 회원 가입하신 것을 축하합니다.\n" +
                "<BR>\n" +
                "<BR>회원 가입을 완료 하시려면 아래 회원가입 완료 링크를 클릭해주세요.\n" +
                "<BR>\n" +
                "<BR>\n" +
                "<BR>\n" +
//            "<BR><A HREF='http://ec2-13-125-78-13.ap-northeast-2.compute.amazonaws.com:8080/metavideo/account/login?certification=#CERTIFICATION#'>회원가입 완료하러가기 ></A>\n" +
//                "<BR><A HREF='http://"+host+"/metavideo/account/login?certification="+certification+"'>회원가입 완료하러가기 ></A>\n" +
                "<BR><A HREF='http://"+host+"/authentications/users/"+email+"/autifications/"+certification+"'>회원가입 완료하러가기 ></A>\n" +
                "<BR>\n" +
                "<BR>\n" +
                "<BR>\n" +
                "<BR>감사합니다.\n" +
                "<BR>\n" +
                "<BR>(주)OGQ 드림" +
                "<BR>";
        return message;
    }



    public String getIdTitle(){
        return ID_TITLE;
    }

    public String getIdBody(){
        return ID_BODY;
    }

    public String getPwTitle(){
        return PW_TITLE;
    }

    public String getPwBody(){
        return PW_BODY;
    }



    public void save(UserDTO userDTO, Integer roleId){
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
        user.setCertify(userDTO.getCertify());
        user.setInstitution(userDTO.getInstitution());
        user.setEmail(userDTO.getEmail());
        user.setUserName(userDTO.getUserName());
        Role userRole = roleRepository.findById(roleId).get();
        user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
        userRepository.save(user);
    }

    public void saveAuth(Integer id,  String certify){
        User user = userRepository.findById(id).get();
        user.setCertify(certify);
        userRepository.save(user);
    }

    public void save(Integer id, String userName, String institution) {
        User user = userRepository.findById(id).get();
        user.setUserName(userName);
        user.setInstitution(institution);
        userRepository.save(user);
    }

    public void savePassword(Integer id, String userPassword) {
        User user = userRepository.findById(id).get();
        user.setPassword(bCryptPasswordEncoder.encode(userPassword));
        userRepository.save(user);
    }
}
