package com.devteria.profile.controller;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.request.ProfileUpdateRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.service.UserProfileService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.catalina.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)//Không khai báo thì mặc định là private,
// Access Modifiers(bộ điều khiển truy cập), dùng để quy định phạm vi truy cập của biến và phương thức

public class UserProfileController {
    UserProfileService userProfileService;

    @PostMapping("/users")
    UserProfileResponse createProfile(@RequestBody ProfileCreationRequest request){
        return userProfileService.createProfile(request);
    }

    @GetMapping("/users/{profileId}")
    UserProfileResponse getProfile(@PathVariable String profileId) //@PathVariable is params on url api link like(/al2k1jd - /id)
    {
        return userProfileService.getProfile(profileId);
    }

    @GetMapping("/users")
    List<UserProfileResponse> getAllProfile() //@PathVariable is params on url api link like(/al2k1jd - /id)
    {
        return userProfileService.getProfiles();
    }

    @PutMapping("/users/{profileId}")
    UserProfileResponse updateProfile(@PathVariable String profileId, @RequestBody ProfileUpdateRequest request){
        return userProfileService.updateProfile(profileId, request);
    }



    @DeleteMapping("/users/{profileId}")
    String deleteProfile(@PathVariable String profileId){
         userProfileService.deleteProfile(profileId);
         return "User has been deleted";
    }


}
