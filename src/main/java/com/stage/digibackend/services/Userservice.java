package com.stage.digibackend.services;

import com.stage.digibackend.Collections.ERole;
import com.stage.digibackend.Collections.Role;
import com.stage.digibackend.Collections.User;
import com.stage.digibackend.Configuration.TunisieSmsConfig;
import com.stage.digibackend.dto.OtpStatus;
import com.stage.digibackend.dto.PasswordResetResponse;
import com.stage.digibackend.repository.RoleRepository;
import com.stage.digibackend.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Userservice implements IUserservice {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TunisieSmsConfig tunisiesmsConfig;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private RoleRepository roleRepository;

    public void register(User user, String siteURL) throws UnsupportedEncodingException, MessagingException {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        String randomCode = RandomStringUtils.random(64, true, true);
        user.setVerificationCode(randomCode);
        user.setEnabled(false);

        userRepository.save(user);
        System.out.println("registre");
        sendVerificationEmail(user, siteURL);
    }

    public void sendVerificationEmail(User user, String siteURL) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "alert@dig2s.com";
        String senderName = "Digi-Smart-Solution";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "Digi-Smart-Solution.";
        System.out.println("send");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]", user.getUsername());
        String verifyURL =   "http://localhost:1112/users/verify/" + user.getVerificationCode();
        content = content.replace("[[URL]]", verifyURL);
        helper.setText(content, true);
        mailSender.send(message);
    }

    public boolean verify(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);

        if (user == null || user.isEnabled()) {
            return false;
        } else {
            user.setVerificationCode(null);
            user.setEnabled(true);
            userRepository.save(user);

            return true;
        }

    }


    //CRUD on user Crete , Read, Update, Delete
    @Override
    public String addUser(User user) {
        return userRepository.save(user).getId();
    }
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Override
    public User getUserById(String userId) {
        System.out.println("User ID"+userId);
        System.out.println(userRepository.findById(userId).get());
        return userRepository.findById(userId).get() ;
    }

    @Override
    public User getUserBytelephone(String telephone) {
        return userRepository.findByTelephone(telephone);
    }

    @Override
    public User updateUser(String userId,User userRequest) {
            User existingUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            if(!userRequest.getEmail2().equals(existingUser.getEmail2()) ){
                System.out.println(userRequest.getEmail2());
                System.out.println(existingUser.getEmail2());
                System.out.println("different mail");
                Optional<User> u = userRepository.findByEmailorEmail2(userRequest.getEmail2());
                if(u.isPresent()){
                    throw new RuntimeException("email use");
                }
            }
            if (userRequest.getTelephone() != null && !userRequest.getTelephone().isEmpty()) {
                existingUser.setTelephone(userRequest.getTelephone());
            }

            if (userRequest.getGenre() != null && !userRequest.getGenre().isEmpty()) {
                existingUser.setGenre(userRequest.getGenre());
            }

            if (userRequest.getUsername() != null && !userRequest.getUsername().isEmpty()) {
                existingUser.setUsername(userRequest.getUsername());
            }

            if (userRequest.getAdresse() != null && !userRequest.getAdresse().isEmpty()) {
                existingUser.setAdresse(userRequest.getAdresse());
            }

            if (userRequest.getPassword() == null || userRequest.getPassword().isEmpty()) {
                userRequest.setPassword(existingUser.getPassword());
            }
            existingUser.setPassword(userRequest.getPassword());

            if (userRequest.getEmail() == null || userRequest.getEmail().isEmpty()) {
                userRequest.setEmail(existingUser.getEmail());
            }
            existingUser.setEmail(userRequest.getEmail());

            if (userRequest.getVerificationCode() == null || userRequest.getVerificationCode().isEmpty()) {
                userRequest.setVerificationCode(existingUser.getVerificationCode());
            }
            existingUser.setVerificationCode(userRequest.getVerificationCode());

            if (userRequest.getVerify() == null || userRequest.getVerify().isEmpty()) {
                userRequest.setVerify(existingUser.getVerify());
            }
            existingUser.setVerify(userRequest.getVerify());
            existingUser.setEmail2(userRequest.getEmail2());
            return userRepository.save(existingUser);
        }


    @Override
    public String deleteUser(String userId) {
        userRepository.deleteById(userId);
        return userId+"User deleted succesully";
    }

    @Override
    public List<User> ListAdmin() {
        Optional<Role> r = roleRepository.findByName(ERole.ADMIN);
        Role r1 = r.get();
        return   userRepository.findByRoleNot(r1.getId());
    }


    @Override
    public List<User> ListAllClient() {
        Optional<Role> r = roleRepository.findByName(ERole.CLIENT);
        Role r1 = r.get();
        return userRepository.findByRoleNot(r1.getId());
    }

    @Override
    public List<User> ListClient(String admin) {
        return userRepository.findByAdmin(admin);
    }


    @Override
    public PasswordResetResponse resetPassword(String email) throws MessagingException, UnsupportedEncodingException {
        User user = userRepository.getUserByUsername(email);
        String randomCode = RandomStringUtils.random(6, true, true);
        user.setVerify(randomCode);
        userRepository.save(user);
        String toAddress = email;
        String fromAddress = "alert@dig2s.com";
        String senderName = "Digi-Smart-Solution";
        String subject = "Your verify code:";
        String content = " <!DOCTYPE html><html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\"><head>\n" +
                "  <title> Welcome to [Coded Mails] </title>\n" +
                "  <!--[if !mso]><!-- -->\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                "  <!--<![endif]-->\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
                "  <style type=\"text/css\">\n" +
                "    #outlook a {\n" +
                "      padding: 0;\n" +
                "    }\n" +
                "\n" +
                "    body {\n" +
                "      margin: 0;\n" +
                "      padding: 0;\n" +
                "      -webkit-text-size-adjust: 100%;\n" +
                "      -ms-text-size-adjust: 100%;\n" +
                "    }\n" +
                "\n" +
                "    table,\n" +
                "    td {\n" +
                "      border-collapse: collapse;\n" +
                "      mso-table-lspace: 0pt;\n" +
                "      mso-table-rspace: 0pt;\n" +
                "    }\n" +
                "\n" +
                "    img {\n" +
                "      border: 0;\n" +
                "      height: auto;\n" +
                "      line-height: 100%;\n" +
                "      outline: none;\n" +
                "      text-decoration: none;\n" +
                "      -ms-interpolation-mode: bicubic;\n" +
                "    }\n" +
                "\n" +
                "    p {\n" +
                "      display: block;\n" +
                "      margin: 13px 0;\n" +
                "    }\n" +
                "  </style>\n" +
                "  <!--[if mso]>\n" +
                "        <xml>\n" +
                "        <o:OfficeDocumentSettings>\n" +
                "          <o:AllowPNG/>\n" +
                "          <o:PixelsPerInch>96</o:PixelsPerInch>\n" +
                "        </o:OfficeDocumentSettings>\n" +
                "        </xml>\n" +
                "        <![endif]-->\n" +
                "  <!--[if lte mso 11]>\n" +
                "        <style type=\"text/css\">\n" +
                "          .mj-outlook-group-fix { width:100% !important; }\n" +
                "        </style>\n" +
                "        <![endif]-->\n" +
                "  <!--[if !mso]><!-->\n" +
                "  <link href=\"https://fonts.googleapis.com/css?family=Nunito:100,400,700\" rel=\"stylesheet\" type=\"text/css\" />\n" +
                "  <style type=\"text/css\">\n" +
                "    @import url(https://fonts.googleapis.com/css?family=Nunito:100,400,700);\n" +
                "  </style>\n" +
                "  <!--<![endif]-->\n" +
                "  <style type=\"text/css\">\n" +
                "    @media only screen and (min-width:480px) {\n" +
                "      .mj-column-per-100 {\n" +
                "        width: 100% !important;\n" +
                "        max-width: 100%;\n" +
                "      }\n" +
                "    }\n" +
                "  </style>\n" +
                "  <style type=\"text/css\">\n" +
                "    @media only screen and (max-width:480px) {\n" +
                "      table.mj-full-width-mobile {\n" +
                "        width: 100% !important;\n" +
                "      }\n" +
                "\n" +
                "      td.mj-full-width-mobile {\n" +
                "        width: auto !important;\n" +
                "      }\n" +
                "    }\n" +
                "  </style>\n" +
                "  <style type=\"text/css\">\n" +
                "    a,\n" +
                "    span,\n" +
                "    td,\n" +
                "    th {\n" +
                "      -webkit-font-smoothing: antialiased !important;\n" +
                "      -moz-osx-font-smoothing: grayscale !important;\n" +
                "    }\n" +
                "  </style>\n" +
                "<style>@font-face {\n" +
                "            font-family: 'Open Sans Regular';\n" +
                "            font-style: normal;\n" +
                "            font-weight: 400;\n" +
                "            src: url('chrome-extension://gkkdmjjodidppndkbkhhknakbeflbomf/fonts/open_sans/open-sans-v18-latin-regular.woff');\n" +
                "        }</style><style>@font-face {\n" +
                "            font-family: 'Open Sans Bold';\n" +
                "            font-style: normal;\n" +
                "            font-weight: 800;\n" +
                "            src: url('chrome-extension://gkkdmjjodidppndkbkhhknakbeflbomf/fonts/open_sans/open-sans-v18-latin-800.woff');\n" +
                "        }</style></head>\n" +
                "\n" +
                "<body style=\"background-color:#eaeaea;\">\n" +
                " \n" +
                "              <!--[if mso | IE]>\n" +
                "                  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                \n" +
                "        <tr>\n" +
                "      \n" +
                "            <td\n" +
                "               class=\"\" style=\"vertical-align:top;width:600px;\"\n" +
                "            >\n" +
                "          <![endif]-->\n" +
                "              <div class=\"mj-column-per-100 mj-outlook-group-fix\" style=\"font-size:0px;text-align:left;direction:ltr;display:inline-block;vertical-align:top;width:100%;\">\n" +
                "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"vertical-align:top;\" width=\"100%\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td align=\"left\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                "                      <div style=\"font-family:Nunito, Helvetica, Arial, sans-serif;font-size:16px;font-weight:400;line-height:20px;text-align:left;color:#54595f;\">\n" +
                "                        <h1 style=\"margin: 0; font-size: 24px; line-height: 32px; line-height: normal; font-weight: bold;\"> Reset your Password</h1>\n" +
                "                      </div>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                  <tr>\n" +
                "                    <td align=\"left\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                "                      <div style=\"font-family:Nunito, Helvetica, Arial, sans-serif;font-size:16px;font-weight:400;line-height:20px;text-align:left;color:#54595f;\">\n" +
                "                        <p style=\"margin: 5px 0;\">Hi [[name]], </p>\n" +
                "                        <p style=\"margin: 5px 0;\">We’ve received a request to reset the password for the Coded Mails account. No changes have been made to your account yet. You can reset your password by clicking the link below:</p>\n" +
                "                      </div>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                  <tr>\n" +
                "                    <td align=\"center\" vertical-align=\"middle\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                "                      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"border-collapse:separate;line-height:100%;\">\n" +
                "                        <tbody><tr>\n" +
                "                          <td align=\"center\" bgcolor=\"#54595f\" role=\"presentation\" style=\"border:none;border-radius:30px;cursor:auto;mso-padding-alt:10px 25px;background:#54595f;\" valign=\"middle\">\n" +
                "                            <div  \"" +
                "\" style=\"display: inline-block;  background: #54595f; color: white; font-family: Nunito, Helvetica, Arial, sans-serif; font-size: 16px; font-weight: normal; line-height: 30px; margin: 0; text-decoration: none; text-transform: none; padding: 10px 25px; mso-padding-alt: 0px; border-radius: 30px;\" target=\"_blank\"> " +randomCode+
                "</div>\n" +
                "                          </td>\n" +
                "                        </tr>\n" +
                "                      </tbody></table>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                  <tr>\n" +
                "                    <td align=\"left\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                "                      <div style=\"font-family:Nunito, Helvetica, Arial, sans-serif;font-size:16px;font-weight:400;line-height:20px;text-align:left;color:#54595f;\">\n" +
                "                        <p style=\"margin: 5px 0;\">If you did not request a new password, please let us know immediately by replying to this email. </p>\n" +
                "                      </div>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                  <tr>\n" +
                "                    <td align=\"left\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                "                      <div style=\"font-family:Nunito, Helvetica, Arial, sans-serif;font-size:16px;font-weight:400;line-height:20px;text-align:left;color:#54595f;\">\n" +
                "                        <p style=\"margin: 5px 0;\">Thanks, <br /> Coded Mails</p>\n" +
                "                      </div>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </div>\n" +
                "              <!--[if mso | IE]>\n" +
                "            </td>\n" +
                "          \n" +
                "        </tr>\n" +
                "      \n" +
                "                  </table>\n" +
                "                <![endif]-->\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody>\n" +
                "      </table>\n" +
                "    </div>\n" +
                "    <!--[if mso | IE]>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "      \n" +
                "      <table\n" +
                "         align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"\" style=\"width:600px;\" width=\"600\"\n" +
                "      >\n" +
                "        <tr>\n" +
                "          <td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\">\n" +
                "      <![endif]-->\n" +
                "  \n" +
                "    <div style=\"margin:0px auto;max-width:600px;\">\n" +
                "      <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n" +
                "        <tbody>\n" +
                "          <tr>\n" +
                "            <td style=\"direction:ltr;font-size:0px;padding:20px 0;text-align:center;\">\n" +
                "              <!--[if mso | IE]>\n" +
                "                  <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                \n" +
                "        <tr>\n" +
                "      \n" +
                "            <td\n" +
                "               class=\"\" style=\"vertical-align:top;width:600px;\"\n" +
                "            >\n" +
                "          <![endif]-->\n" +
                "              <div class=\"mj-column-per-100 mj-outlook-group-fix\" style=\"font-size:0px;text-align:left;direction:ltr;display:inline-block;vertical-align:top;width:100%;\">\n" +
                "                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"vertical-align:top;\" width=\"100%\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"font-size:0px;word-break:break-word;\">\n" +
                "                      <!--[if mso | IE]>\n" +
                "    \n" +
                "        <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td height=\"1\" style=\"vertical-align:top;height:1px;\">\n" +
                "      \n" +
                "    <![endif]-->\n" +
                "                      <div style=\"height:1px;\">   </div>\n" +
                "                      <!--[if mso | IE]>\n" +
                "    \n" +
                "        </td></tr></table>\n" +
                "      \n" +
                "    <![endif]-->\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </div>\n" +
                "              <!--[if mso | IE]>\n" +
                "            </td>\n" +
                "          \n" +
                "        </tr>\n" +
                "      \n" +
                "                  </table>\n" +
                "                <![endif]-->\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody>\n" +
                "      </table>\n" +
                "    </div>\n" +
                "    <!--[if mso | IE]>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "      <![endif]-->\n" +
                "  </div>\n" +
                "\n" +
                "\n" +
                "</body></html>";
        System.out.println("send");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

      //  contentt = content.replace("[[name]]", user.getUsername());
       content = content.replace("[[randomCode]]", randomCode).replace("[[name]]", user.getUsername());
        System.out.println(randomCode);
      //  String verifyURL = siteURL + "/verify?code=" + user.getVerificationCode();

      //  content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);
      //  helper.setText(contentt, true);
        mailSender.send(message);
        //sendSms(randomCode);
    return new PasswordResetResponse(OtpStatus.DELIVERED , randomCode);
    }
    private boolean isPasswordValid(String pwd) {
        // Vérifier si le mot de passe contient au moins une lettre minuscule et une lettre majuscule
        return pwd.matches("(?=.*[a-z])(?=.*[A-Z]).*");
    }
    @Override
    public ResponseEntity<String> verifiePwd(String code, String pwd) {
        User user = userRepository.getUserCD(code);
        System.out.println(user.getEmail());
        if (user != null) {
            if (isPasswordValid(pwd)) {
                user.setPassword(passwordEncoder.encode(pwd));
                user.setVerify(null);
                userRepository.save(user);
                System.out.println("PASSWORD CHANGED");
                return ResponseEntity.ok("Mot de passe changé");
            } else {
                return ResponseEntity.ok("Le mot de passe doit contenir au moins une lettre minuscule et une lettre majuscule");
            }
        }

        return ResponseEntity.ok("Vérifiez votre code");
    }

    @Override
    public PasswordResetResponse sendOTPForPasswordResest(String telephone) {
        PasswordResetResponse response=null;
        try {

            String mySender = tunisiesmsConfig.getSender();
            String myKey= tunisiesmsConfig.getKey();
            String randomCode = RandomStringUtils.random(6, true, true);
            User user = getUserBytelephone(telephone);
            System.out.println(user);
            if(user!=null)
            {
                user.setVerify(randomCode);
                userRepository.save(user);
                String otpMessage="Cher_utilisateur_utilisez_ce_code_pour_réinitialiser_votre_mot_de_passe:"+randomCode;
                String Url_str = "https://www.tunisiesms.tn/client/Api/Api.aspx?fct=sms&key=MYKEY&mobile=216XXXXXXXX&sms=Hello+World&sender=YYYYYYYY";
                Url_str = Url_str.replace("MYKEY", myKey);
                Url_str = Url_str.replace("216XXXXXXXX", "216"+telephone);
                Url_str = Url_str.replace("Hello+World", otpMessage);
                Url_str = Url_str.replace("YYYYYYYY", mySender);
                URL myURL = new URL(Url_str);
                URLConnection myURLConnection = myURL.openConnection();
              myURLConnection.connect();
                System.out.println("here we go");
                response=new PasswordResetResponse(OtpStatus.DELIVERED,randomCode);

            }
            else{
                return new PasswordResetResponse(OtpStatus.FAILED,"Check your phone number");
            }

        } catch (Exception exp) {
            response=new PasswordResetResponse(OtpStatus.FAILED,exp.getMessage());}

        return response;

    }

}
