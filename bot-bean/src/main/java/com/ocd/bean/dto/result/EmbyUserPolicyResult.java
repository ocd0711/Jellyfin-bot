package com.ocd.bean.dto.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ocd.bean.mysql.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ch.hu
 * @date 2024/11/13 10:21
 * Description:
 */
@Data
@NoArgsConstructor
public class EmbyUserPolicyResult {

    @JsonProperty("IsAdministrator")
    private Boolean isAdministrator;
    @JsonProperty("IsHidden")
    private Boolean isHidden;
    @JsonProperty("EnableCollectionManagement")
    private Boolean enableCollectionManagement;
    @JsonProperty("EnableSubtitleManagement")
    private Boolean enableSubtitleManagement;
    @JsonProperty("EnableLyricManagement")
    private Boolean enableLyricManagement;
    @JsonProperty("IsDisabled")
    private Boolean isDisabled;
    @JsonProperty("BlockedTags")
    private List<String> blockedTags;
    @JsonProperty("AllowedTags")
    private List<String> allowedTags;
    @JsonProperty("EnableUserPreferenceAccess")
    private Boolean enableUserPreferenceAccess;
    @JsonProperty("AccessSchedules")
    private List<String> accessSchedules;
    @JsonProperty("BlockUnratedItems")
    private List<String> blockUnratedItems;
    @JsonProperty("EnableRemoteControlOfOtherUsers")
    private Boolean enableRemoteControlOfOtherUsers;
    @JsonProperty("EnableSharedDeviceControl")
    private Boolean enableSharedDeviceControl;
    @JsonProperty("EnableRemoteAccess")
    private Boolean enableRemoteAccess;
    @JsonProperty("EnableLiveTvManagement")
    private Boolean enableLiveTvManagement;
    @JsonProperty("EnableLiveTvAccess")
    private Boolean enableLiveTvAccess;
    @JsonProperty("EnableMediaPlayback")
    private Boolean enableMediaPlayback;
    @JsonProperty("EnableAudioPlaybackTranscoding")
    private Boolean enableAudioPlaybackTranscoding;
    @JsonProperty("EnableVideoPlaybackTranscoding")
    private Boolean enableVideoPlaybackTranscoding;
    @JsonProperty("EnablePlaybackRemuxing")
    private Boolean enablePlaybackRemuxing;
    @JsonProperty("ForceRemoteSourceTranscoding")
    private Boolean forceRemoteSourceTranscoding;
    @JsonProperty("EnableContentDeletion")
    private Boolean enableContentDeletion;
    @JsonProperty("EnableContentDeletionFromFolders")
    private List<String> enableContentDeletionFromFolders;
    @JsonProperty("EnableContentDownloading")
    private Boolean enableContentDownloading;
    @JsonProperty("EnableSyncTranscoding")
    private Boolean enableSyncTranscoding;
    @JsonProperty("EnableMediaConversion")
    private Boolean enableMediaConversion;
    @JsonProperty("EnabledDevices")
    private List<String> enabledDevices;
    @JsonProperty("EnableAllDevices")
    private Boolean enableAllDevices;
    @JsonProperty("EnabledChannels")
    private List<String> enabledChannels;
    @JsonProperty("EnableAllChannels")
    private Boolean enableAllChannels;
    @JsonProperty("EnabledFolders")
    private List<String> enabledFolders;
    @JsonProperty("EnableAllFolders")
    private Boolean enableAllFolders;
    @JsonProperty("InvalidLoginAttemptCount")
    private Integer invalidLoginAttemptCount;
    @JsonProperty("LoginAttemptsBeforeLockout")
    private Integer loginAttemptsBeforeLockout;
    @JsonProperty("MaxActiveSessions")
    private Integer maxActiveSessions;
    @JsonProperty("EnablePublicSharing")
    private Boolean enablePublicSharing;
    @JsonProperty("BlockedChannels")
    private List<String> blockedChannels;
    @JsonProperty("RemoteClientBitrateLimit")
    private Integer remoteClientBitrateLimit;
    @JsonProperty("AuthenticationProviderId")
    private String authenticationProviderId;
    @JsonProperty("PasswordResetProviderId")
    private String passwordResetProviderId;
    @JsonProperty("SyncPlayAccess")
    private String syncPlayAccess;

    // ------ emby ⬇️ -------
    @JsonProperty("IsHiddenRemotely")
    private Boolean isHiddenRemotely;
    @JsonProperty("IsHiddenFromUnusedDevices")
    private Boolean isHiddenFromUnusedDevices;
    @JsonProperty("LockedOutDate")
    private Integer lockedOutDate;
    @JsonProperty("AllowTagOrRating")
    private Boolean allowTagOrRating;
    @JsonProperty("IsTagBlockingModeInclusive")
    private Boolean isTagBlockingModeInclusive;
    @JsonProperty("IncludeTags")
    private List<String> includeTags;
    @JsonProperty("RestrictedFeatures")
    private List<?> restrictedFeatures;
    @JsonProperty("EnableSubtitleDownloading")
    private Boolean enableSubtitleDownloading;
    @JsonProperty("ExcludedSubFolders")
    private List<String> excludedSubFolders;
    @JsonProperty("SimultaneousStreamLimit")
    private Integer simultaneousStreamLimit;
    @JsonProperty("AllowCameraUpload")
    private Boolean allowCameraUpload;
    @JsonProperty("AllowSharingPersonalItems")
    private Boolean allowSharingPersonalItems;
    // ------ emby ⬆️ -------

    public void sByUser(User user) {
        this.isAdministrator = user.getSuperAdmin();
    }

    public void sHideFolder(User user, boolean isHide, List<String> EnabledFolders, List<String> ExFolders) {
        this.isAdministrator = user.getSuperAdmin();
        this.enableAllFolders = !isHide;
        if (!isHide) {
            EnabledFolders.clear();
            ExFolders.clear();
        }
        this.enabledFolders = EnabledFolders;
        this.excludedSubFolders = ExFolders;
    }
}
