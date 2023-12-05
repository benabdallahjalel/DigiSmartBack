package com.stage.digibackend.services;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.stage.digibackend.Collections.User;
import com.stage.digibackend.dto.PasswordResetResponse;
import org.springframework.http.ResponseEntity;

import javax.mail.MessagingException;

public interface IUserservice {
    String addUser(User user);
    List<User> getAllUsers();
    User getUserById(String userId);
    User getUserBytelephone(String telephone);
    User updateUser(String userId,User userRequest);
    String deleteUser(String userId);
    List<User> ListAdmin();

    List<User> ListAllClient() ;
    List<User> ListClient(String admin);


    ResponseEntity<String> verifiePwd(String code, String pwd);

    PasswordResetResponse resetPassword(String email) throws MessagingException, UnsupportedEncodingException;


    //reser password with phone number
    PasswordResetResponse sendOTPForPasswordResest(String phone);
}

