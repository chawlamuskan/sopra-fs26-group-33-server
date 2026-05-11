package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class UserPutDTO {
    private String password;
    private String oldPassword;

    public String getPassword() {
        return password;
    }   

    public void setPassword(String password) {
        this.password = password;
    }   

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

}
