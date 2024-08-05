package com.clds.bottletalk.product.document;

public class UserDTO {

    private String userId;


    public UserDTO() {
    }

    public UserDTO(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId='" + userId + '\'' +
                '}';
    }
}
