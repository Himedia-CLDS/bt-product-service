package com.clds.bottletalk.model;

public class UserDTO {

    private String userId;
    private String gender;
    private String birthYear;

    public UserDTO() {
    }

    public UserDTO(String userId, String gender, String birthYear) {
        this.userId = userId;
        this.gender = gender;
        this.birthYear = birthYear;
    }

    public String getuserId() {
        return userId;
    }

    public void setuserId(String userId) {
        this.userId = userId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId='" + userId + '\'' +
                ", gender='" + gender + '\'' +
                ", birthYear='" + birthYear + '\'' +
                '}';
    }
}


