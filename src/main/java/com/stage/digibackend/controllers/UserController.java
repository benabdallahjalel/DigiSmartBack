package com.stage.digibackend.controllers;

import java.io.UnsupportedEncodingException;
import com.stage.digibackend.Collections.User;
import com.stage.digibackend.dto.PasswordResetResponse;
import com.stage.digibackend.services.IUserservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.validation.Valid;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stage.digibackend.Collections.ERole;
import com.stage.digibackend.Collections.Role;
import com.stage.digibackend.Collections.User;
import com.stage.digibackend.payload.request.SignupRequest;
import com.stage.digibackend.payload.request.UserRequest;
import com.stage.digibackend.payload.response.MessageResponse;
import com.stage.digibackend.repository.RoleRepository;
import com.stage.digibackend.repository.UserRepository;
import com.stage.digibackend.services.IUserservice;
import com.stage.digibackend.services.Userservice;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/users")

@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    IUserservice iUserService;
    //Add a user
    @PostMapping("/adduser")
    public String addUser(@RequestBody User user)
    {
        return iUserService.addUser(user);
    }




    //get all users
  //@PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    @GetMapping("/get")
    public List<User> getUsers()
    {
        return iUserService.getAllUsers();
    }
    //get user by id
//    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public User getUser(@PathVariable String userId)
    {
        return iUserService.getUserById(userId);
    }
    //@PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    @PutMapping("/updateuser/{userId}")
    public User modifyUser(@PathVariable String userId,@RequestBody User user) {
        return iUserService.updateUser(userId,user);
    }
    //@PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    @DeleteMapping("/deleteuser/{userId}")
    public String deleteUser(@PathVariable String userId)
    {
        return iUserService.deleteUser(userId);    }
////Liste des admin
    @GetMapping("/ListAdmin")
    List<User> ListAdmin(){
        return iUserService.ListAdmin();
    }

    //  LISTE ALL CLIENT IN DATABASE
    @GetMapping("/listAllClient")
    List<User> ListAllClient(){
        return iUserService.ListAllClient();
    }
/////Liste de client pour chaque admin
    @GetMapping("/ListClientByAdmin/{user}")
    List<User> ListClient(@PathVariable String user){
        return iUserService.ListClient(user);
    }

    ///reset password
    @PutMapping("/resetPwd/{email}")
    PasswordResetResponse verifypwd(@PathVariable String email) throws MessagingException, UnsupportedEncodingException {

        System.out.println("sending mail : "+email);
        return iUserService.resetPassword(email);
    }

    @GetMapping("/verifiePwd/{code}/{pwd}")
    ResponseEntity verifiePwd(@PathVariable String code,@PathVariable String pwd){
        return iUserService.verifiePwd(code,pwd);
    }
//reset password with sms
@PutMapping("/sendOtp/{phone}")
PasswordResetResponse sendSms(@PathVariable String phone){
   return iUserService.sendOTPForPasswordResest(phone);
}

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    PasswordEncoder encoder;


    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private Userservice userservice;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping("/AddAdmin")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest userRequest, String siteURL) throws MessagingException, UnsupportedEncodingException {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(userRequest.getUsername(),
                userRequest.getEmail(),
                encoder.encode(userRequest.getPassword()));


        Set<Role> roles = new HashSet<>();
        Role r =  roleRepository.findByName(ERole.ADMIN).get();
                if(roles==null){
                    return ResponseEntity.badRequest().body("Role not found");
                }

                roles.add(r);
        user.setRoles( roles);


        String Telephone = "+216"+userRequest.getTelephone();
        System.out.println(userRequest.getTelephone());
        user.setGenre(userRequest.getGenre());
        user.setTelephone(Telephone);
        user.setAdresse(userRequest.getAdresse());
        String randomCode = RandomStringUtils.random(64, true, true);
        user.setVerificationCode(randomCode);
        user.setEnabled(false);

        userRepository.save(user);


        System.out.println("registre");
        //userservice.sendVerificationEmail(user, siteURL);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/AddClient/{id}")
    public ResponseEntity<?> registerClient(@Valid @RequestBody UserRequest userRequest, String siteURL, @PathVariable("id") String idUser) throws MessagingException, UnsupportedEncodingException {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(userRequest.getUsername(),
                userRequest.getEmail(),
                encoder.encode(userRequest.getPassword()));


        Set<Role> roles = new HashSet<>();
        Role r =  roleRepository.findByName(ERole.CLIENT).get();
        if(roles==null){
            return ResponseEntity.badRequest().body("Role not found");
        }
        User admin = userRepository.findById(idUser).get();
        roles.add(r);
        user.setRoles( roles);
        //userRepository.save(user);
        String Telephone = "+216"+userRequest.getTelephone();
        user.setGenre(userRequest.getGenre());
        user.setTelephone(Telephone);
        user.setAdresse(userRequest.getAdresse());
        String randomCode = RandomStringUtils.random(64, true, true);
        user.setVerificationCode(randomCode);
        user.setEnabled(false);
        user.setAdmin(admin);
        userRepository.save(user);


        System.out.println("registre");
        userservice.sendVerificationEmail(user, siteURL);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }



    @GetMapping("/verify/{code}")
    public RedirectView verifyUser(@PathVariable("code") String code) {
        if (userservice.verify(code)) {
            return new RedirectView("http://localhost:4200/login");
        } else {
            return new RedirectView("http://localhost:4200/forbidden");
        }
    }

    @PostConstruct
    public void createDefaultSuperAdmin() {
        // Check if the default user already exists
        Optional<User> u = userRepository.findByEmailorEmail2("your_default_email");
        if (!u.isPresent()) {
            if (!userRepository.existsByUsername("your_default_username_here")) {
                // Create a new user with the role "super-admin"
                User defaultUser = new User();
                defaultUser.setEmail("your_default_email");
                defaultUser.setUsername("your_default_username_here");
                defaultUser.setTelephone("your_default_telephone_here");
                defaultUser.setGenre("your_default_genre_here");
                defaultUser.setPassword(passwordEncoder.encode("dig2S"));
                defaultUser.setEnabled(true);
                // Find or create the "super-admin" role
                Role superAdminRole = roleRepository.findByName(ERole.SUPER_ADMIN)
                        .orElseGet(() -> {
                            Role role = new Role();
                            role.setName(ERole.SUPER_ADMIN);
                            return roleRepository.save(role);
                        });
                // Assign the "super-admin" role to the default user
                Set<Role> roles = new HashSet<>();
                roles.add(superAdminRole);
                defaultUser.setRoles(roles);
                // Save the default user to the database
                userRepository.save(defaultUser);
            }
        }
    }




}
