// package com.ledgerx.auth.application.service;
//
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.ledgerx.auth.domain.model.UserSocialAccount;
// import com.ledgerx.auth.infra.persistence.entity.UserEntity;
// import com.ledgerx.auth.infra.repository.UserRepository;
// import com.ledgerx.auth.infra.repository.UserSocialAccountRepository;
// import com.ledgerx.auth.security.UserPrincipal;
// import org.apache.commons.lang3.StringUtils;
// import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
// import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
// import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
// import org.springframework.security.oauth2.core.user.OAuth2User;
// import org.springframework.stereotype.Service;
//
// import java.util.Map;
// import java.util.Optional;
// import java.util.UUID;
//
// @Service
// public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    private final UserRepository userRepository;
//    private final UserSocialAccountRepository userSocialAccountRepository;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public CustomOAuth2UserService(
//            UserRepository userRepository, UserSocialAccountRepository
// userSocialAccountRepository) {
//        this.userRepository = userRepository;
//        this.userSocialAccountRepository = userSocialAccountRepository;
//    }
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
// {
//
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//
//        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // github
//        if (!"github".equalsIgnoreCase(registrationId)) {
//            // 以后扩展别的 provider 时可以在这里分发
//            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
//        }
//
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        // GitHub 返回字段示例：https://api.github.com/user
//        String provider = "GITHUB";
//        String providerUserId = String.valueOf(attributes.get("id"));
//        String email = (String) attributes.get("email");
//        String name = (String) attributes.getOrDefault("name", null);
//        String avatarUrl = (String) attributes.getOrDefault("avatar_url", null);
//
//        // 一些 GitHub 账号不公开 email，可以再去 /user/emails 拉，但现在先不折腾
//        if (StringUtils.isBlank(name)) {
//            name = (String) attributes.getOrDefault("login", "GitHubUser-" + providerUserId);
//        }
//
//        // 1. 先查是否已经有这个 social account
//        Optional<UserSocialAccount> existingSocialOpt =
//                userSocialAccountRepository.findByProviderAndProviderUserId(provider,
// providerUserId);
//
//        UserEntity userEntity;
//        if (existingSocialOpt.isPresent()) {
//            userEntity = existingSocialOpt.get().getUserEntity();
//            // 这里可以顺便更新一下头像 / 显示名
//            boolean changed = false;
//            if (avatarUrl != null && !avatarUrl.equals(userEntity.getAvatarUrl())) {
//                userEntity.setAvatarUrl(avatarUrl);
//                changed = true;
//            }
//            if (name != null && !name.equals(userEntity.getDisplayName())) {
//                userEntity.setDisplayName(name);
//                changed = true;
//            }
//            if (changed) {
//                userRepository.save(userEntity);
//            }
//        } else {
//            // 2. 没绑定过这个 GitHub 账号 -> 视为新用户
//            userEntity = new UserEntity();
//            userEntity.setUuid(UUID.randomUUID().toString());
//            userEntity.setEmail(email); // email 可能为 null
//            userEntity.setDisplayName(name);
//            userEntity.setAvatarUrl(avatarUrl);
//            userEntity.setStatus("ACTIVE");
//            userEntity = userRepository.save(userEntity);
//
//            UserSocialAccount social = new UserSocialAccount();
//            social.setUserEntity(userEntity);
//            social.setProvider(provider);
//            social.setProviderUserId(providerUserId);
//            social.setEmail(email);
//            try {
//                social.setRawProfile(objectMapper.writeValueAsString(attributes));
//            } catch (JsonProcessingException e) {
//                social.setRawProfile(null);
//            }
//            userSocialAccountRepository.save(social);
//        }
//
//        return new UserPrincipal(
//                userEntity.getId(),
//                userEntity.getUuid(),
//                attributes);
//    }
// }
