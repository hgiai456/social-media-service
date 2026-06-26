package com.devteria.profile.service;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.request.ProfileUpdateRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.entity.UserProfile;
import com.devteria.profile.exception.AppException;
import com.devteria.profile.exception.ErrorCode;
import com.devteria.profile.mapper.UserProfileMapper;
import com.devteria.profile.repository.UserProfileRepository;
import com.devteria.profile.repository.httpclient.FileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {
    UserProfileRepository userProfileRepository;
    FileClient fileClient;
    UserProfileMapper userProfileMapper;

    public UserProfileResponse createProfile(ProfileCreationRequest request){

        UserProfile userProfile = userProfileMapper.toUserProfile(request);
        userProfile = userProfileRepository.save(userProfile);

        log.info("Profile request: {}", request);
        log.info("Profile Entity: {}", userProfile.toString());

        return userProfileMapper.toUserProfileResponse(userProfile);

    }

    public UserProfileResponse getProfile(String id){
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileResponse> getProfiles(){
        return userProfileRepository.findAll()
                .stream() //Stream<UserProfile> to List<UserProfile>
//              .map(userProfile -> userProfileMapper.toUserProfileResponse(userProfile))
                .map(userProfileMapper::toUserProfileResponse)//Convert from Stream<UserProfile> to List<UserResponse>
                .toList();
    }

    public UserProfileResponse updateProfile(String id ,ProfileUpdateRequest request){
        UserProfile user = userProfileRepository.findById(id).orElseThrow(()
                -> new AppException(ErrorCode.USER_EXISTED));
        userProfileMapper.updateProfile(user, request);

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(user));
    }

    public UserProfileResponse getMyProfile(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        log.info("Get user by Security Context: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userProfileMapper.toUserProfileResponse(profile);

    }

    public UserProfileResponse updateMyProfile(ProfileUpdateRequest request){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        UserProfile user = userProfileRepository.findByUserId(userId).orElseThrow(()
                -> new AppException(ErrorCode.USER_EXISTED));

        userProfileMapper.updateProfile(user, request);

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(user));
    }

    public UserProfileResponse updateAvatar(MultipartFile file){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow(()
                -> new AppException(ErrorCode.USER_EXISTED));

        var response = fileClient.uploadMedia(file);

        profile.setAvatar(response.getResult().getUrl());
        //Upload file - invoke an api File Service

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(profile));
    }


    public UserProfileResponse getByUserId(String userId){
    UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(()
            -> new AppException(ErrorCode.USER_NOT_EXISTED));

    return userProfileMapper.toUserProfileResponse(userProfile);

    }

    public void deleteProfile(String id){
        userProfileRepository.deleteById(id);
    }





}
