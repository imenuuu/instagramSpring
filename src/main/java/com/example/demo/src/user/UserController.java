package com.example.demo.src.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;


import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/app/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;




    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService){
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * 회원 조회 API
     * [GET] /users
     * 회원 번호 및 이메일 검색 조회 API
     * [GET] /users? Email=
     * @return BaseResponse<List<GetUserRes>>
     */
    //Query String
    @ResponseBody
    @GetMapping("") // (GET) 127.0.0.1:9000/app/users
    public BaseResponse<List<GetUserRes>> getUsers(@RequestParam(required = false) String Email) {
        try{
            if(Email == null){
                List<GetUserRes> getUsersRes = userProvider.getUsers();
                return new BaseResponse<>(getUsersRes);
            }
            // Get Users
            List<GetUserRes> getUsersRes = userProvider.getUsersByEmail(Email);
            return new BaseResponse<>(getUsersRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 회원 검색기능 API
     * [GET] app/users/search?searchContent=
     * @return
     */
    @ResponseBody
    @GetMapping("/search") //(GET) localhost:9000/app/users/search?serachContent=
    public BaseResponse<List<GetSearchUserRes>> getSearchUsers(@RequestParam(value="searchContent") String searchContent){
        try{
            GetSearchUserReq getSearchUserReq=new GetSearchUserReq(searchContent,searchContent);
            List<GetSearchUserRes> getSearchUserRes=userProvider.getSearchUsers(getSearchUserReq);
            System.out.println(getSearchUserReq.getUserNickname());
            return new BaseResponse<>(getSearchUserRes);
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }

    }

    /**
     * 회원 1명 조회 API
     * [GET] /users/:userIdx
     * @return BaseResponse<GetUserRes>
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{userIdx}") // (GET) 127.0.0.1:9000/app/users/:userIdx
    public BaseResponse<GetUserRes> getUser(@PathVariable("userIdx") Long userIdx) {
        // Get Users
        try{

            GetUserRes getUserRes = userProvider.getUser(userIdx);
            return new BaseResponse<>(getUserRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }

    }
    //user/apps/profile/userNickname=?
    @ResponseBody
    @GetMapping("/profile")
    public BaseResponse<LinkedHashMap<String, Object>> getUserProfile(@RequestParam(value="userNickname") String userNickname){
        try {
            List<GetUserProfileRes> getUserProfileRes=userProvider.getUserProfile(userNickname);
            List<GetUserHighlightRes> getUserHighlightRes =userProvider.getUserHighlight(userNickname);
            List<GetUserBoardRes> getUserBoardRes=userProvider.getUserBoard(userNickname);

            LinkedHashMap<String,Object> resultMap=new LinkedHashMap<>();

            resultMap.put("Profile",getUserProfileRes);
            resultMap.put("Highlightlist", getUserHighlightRes);
            resultMap.put("Boards",getUserBoardRes);

            return new BaseResponse<>(resultMap);

        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }

    }

    /**
     * 회원가입 API
     * [POST] /users
     * @return BaseResponse<PostUserRes>
     */
    // Body
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq) {
        // TODO: email 관련한 짧은 validation 예시입니다. 그 외 더 부가적으로 추가해주세요!
        if(postUserReq.getUserEmail() == null){
            return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
        }
        //이메일 정규표현
        if(!isRegexEmail(postUserReq.getUserEmail())){
            return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
        }
        try{
            PostUserRes postUserRes = userService.createUser(postUserReq);
            return new BaseResponse<>(postUserRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }
    @ResponseBody
    @PostMapping({"/follow/{follow_id}"})
    public BaseResponse<String> createUserFollow(@PathVariable("follow_id")Long follow_id){
        try {
            int userIdx=jwtService.getUserIdx();
            GetUserFollowReq getUserFollowReq = new GetUserFollowReq(follow_id,(long)userIdx);
            userService.createUserFollow(getUserFollowReq);
            String result="";
            return new BaseResponse<>(result);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 로그인 API
     * [POST] /users/logIn
     * @return BaseResponse<PostLoginRes>
     */

    @ResponseBody
    @PostMapping("/logIn")
    public BaseResponse<PostLoginRes> logIn(@RequestBody PostLoginReq postLoginReq){

        try{
            // TODO: 로그인 값들에 대한 형식적인 validatin 처리해주셔야합니다!
            // TODO: 유저의 status ex) 비활성화된 유저, 탈퇴한 유저 등을 관리해주고 있다면 해당 부분에 대한 validation 처리도 해주셔야합니다.

            PostLoginRes postLoginRes = userProvider.logIn(postLoginReq);
            return new BaseResponse<>(postLoginRes);
        } catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 유저이름변경 API
     * [PATCH] /users/:userIdx
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userIdx}")
    public BaseResponse<String> modifyUserName(@PathVariable("userIdx") Long userIdx, @RequestBody User user){

        try {
            //jwt에서 idx 추출.
            int userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            //같다면 유저네임 변경
            PatchUserReq patchUserReq = new PatchUserReq(userIdx,user.getUserNickname());
            userService.modifyUserName(patchUserReq);
            String result = "";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
    @ResponseBody
    @PatchMapping("/user-status/{userIdx}")
    public BaseResponse<String> modifyUserStatus(@PathVariable("userIdx") Long userIdx){
        try {
            int userIdxByJwt=jwtService.getUserIdx();
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            PatchUserStatusReq patchUserStatusReq=new PatchUserStatusReq(userIdx);
            userService.modifyUserStatus(patchUserStatusReq);
            String result="";
            return new BaseResponse<>(result);
        } catch (BaseException e) {
           return new BaseResponse<>(e.getStatus());
        }

    }

    //유저정보변경 API

    @ResponseBody
    @PutMapping("/user-info/{userIdx}")
    public BaseResponse<String> modifyUserInfo(@PathVariable("userIdx") Long userIdx,@RequestBody User user){
        try{
            int userIdxByJwt=jwtService.getUserIdx();
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            PutUserReq putUserReq = new PutUserReq(userIdx,user.getProfileImgUrl(),user.getUserNickname(),user.getUserName(),user.getUserIntroduce(),user.getUserWebsite());
            userService.modifyUserInfo(putUserReq);
            String result = "";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));

        }
    }
    @ResponseBody
    @GetMapping("/oauth")
    public void home(@RequestParam String code)throws Exception{
        System.out.println(code);

    }



}
