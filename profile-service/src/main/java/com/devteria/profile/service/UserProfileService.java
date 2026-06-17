package com.devteria.profile.service;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.request.ProfileUpdateRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.entity.UserProfile;
import com.devteria.profile.mapper.UserProfileMapper;
import com.devteria.profile.repository.UserProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {
    UserProfileRepository userProfileRepository;

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
                -> new RuntimeException("User isn't exist."));
        userProfileMapper.updateProfile(user, request);

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(user));
    }

    public void deleteProfile(String id){
        userProfileRepository.deleteById(id);
    }





}
