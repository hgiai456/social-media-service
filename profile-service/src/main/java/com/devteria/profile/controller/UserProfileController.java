package com.devteria.profile.controller;

import com.devteria.profile.dto.ApiResponse;
import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.request.ProfileUpdateRequest;
import com.devteria.profile.dto.request.SearchUserRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.service.UserProfileService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)//Không khai báo thì mặc định là private,
// Access Modifiers(bộ điều khiển truy cập), dùng để quy định phạm vi truy cập của biến và phương thức
@Builder
public class UserProfileController {
    UserProfileService userProfileService;

    @PostMapping("/users")
    ApiResponse<UserProfileResponse>  createProfile(@RequestBody ProfileCreationRequest request){
        return  ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.createProfile(request))
                .build();
    }

    @GetMapping("/users/{profileId}")
    ApiResponse<UserProfileResponse>  getProfile(@PathVariable String profileId) //@PathVariable is params on url api link like(/al2k1jd - /id)
    {
        return  ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getProfile(profileId))
                .build();
    }

    @GetMapping("/users")
    ApiResponse<List<UserProfileResponse>>  getAllProfile() //@PathVariable is params on url api link like(/al2k1jd - /id)
    {
        return  ApiResponse.<List<UserProfileResponse>>builder()
                .result(userProfileService.getProfiles())
                .build();

    }

    @GetMapping("/users/my-profile")
    ApiResponse<UserProfileResponse>  getMyProfile() //@PathVariable is params on url api link like(/al2k1jd - /id)
    {
        return  ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getMyProfile())
                .build();

    }



    @PutMapping("/users/my-profile")
    ApiResponse<UserProfileResponse>  updateMyProfile(@RequestBody ProfileUpdateRequest request) //@PathVariable is params on url api link like(/al2k1jd - /id)
    {
        return  ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.updateMyProfile(request))
                .build();

    }

    @PutMapping("/users/{profileId}")
    ApiResponse<UserProfileResponse>  updateProfile(@PathVariable String profileId, @RequestBody ProfileUpdateRequest request){
        return  ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.updateProfile(profileId, request))
                .build();
    }


    @DeleteMapping("/users/{profileId}")
    ApiResponse<String>  deleteProfile(@PathVariable String profileId){
         userProfileService.deleteProfile(profileId);
         return ApiResponse.<String>builder()
                 .message("Profile has been deleted")
                 .build();
    }

    @PutMapping("/users/avatar")
    ApiResponse<UserProfileResponse>  updateAvatar(@RequestParam("file")MultipartFile file) //@PathVariable is params on url api link like(/al2k1jd - /id)
    {
        return  ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.updateAvatar(file))
                .build();
    }

    @PostMapping("/users/search")
    ApiResponse<List<UserProfileResponse>> search(@RequestBody SearchUserRequest request){
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(userProfileService.search(request))
                .build();
    }



}
