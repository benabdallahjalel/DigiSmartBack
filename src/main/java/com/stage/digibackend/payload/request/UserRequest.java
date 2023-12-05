package com.stage.digibackend.payload.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

public class UserRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;


    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    private String Telephone;
    @NotBlank
    private String Genre;
    private String Adresse;

    public String getAdresse(){return this.Adresse;}

    public void setAdresse(String Adresse){this.Adresse = Adresse;}

    public String getTelephone(){
        return this.Telephone;
    }
    public void setTelephone(String Telephone){this.Telephone = Telephone;}
    public String getGenre(){return this.Genre;}
    public  void setGenre(String Genre){this.Genre=Genre;}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
