package com.james.chat.entity;

import java.io.Serializable;

public class FriendRequest implements Serializable {
    private Long requestUser;

    private Long acceptUser;

    private String requestDescription;

    private static final long serialVersionUID = 1L;

    public Long getRequestUser() {
        return requestUser;
    }

    public void setRequestUser(Long requestUser) {
        this.requestUser = requestUser;
    }

    public Long getAcceptUser() {
        return acceptUser;
    }

    public void setAcceptUser(Long acceptUser) {
        this.acceptUser = acceptUser;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", requestUser=").append(requestUser);
        sb.append(", acceptUser=").append(acceptUser);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
