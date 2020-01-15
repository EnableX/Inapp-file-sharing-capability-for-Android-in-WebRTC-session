package com.enablex.demoenablex.utilities;

public class UserListModels {

    private String clientId;
    private String name;
    private boolean isAudioMuted;
    private boolean isVideoMuted;
    private String role;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAudioMuted() {
        return isAudioMuted;
    }

    public void setAudioMuted(boolean audioMuted) {
        isAudioMuted = audioMuted;
    }

    public boolean isVideoMuted() {
        return isVideoMuted;
    }

    public void setVideoMuted(boolean videoMuted) {
        isVideoMuted = videoMuted;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
