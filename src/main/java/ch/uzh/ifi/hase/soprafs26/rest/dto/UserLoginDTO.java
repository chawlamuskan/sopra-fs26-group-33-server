package ch.uzh.ifi.hase.soprafs26.rest.dto;

// ## created this file cause when i login , i only need username n password
// ## cannot pass it directly to the DB , so created this obj that expects to receive the data 
// ##  

public class UserLoginDTO {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
