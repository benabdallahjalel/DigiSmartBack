package com.stage.digibackend.services;
import com.stage.digibackend.Collections.DataSensor;
import com.stage.digibackend.Collections.Device;
import com.stage.digibackend.Collections.Sensor;
import com.stage.digibackend.Collections.User;
import com.stage.digibackend.dto.OtpStatus;
import com.stage.digibackend.dto.deviceResponse;
import com.stage.digibackend.repository.DataSensorRepository;
import com.stage.digibackend.repository.DeviceRepository;
import com.stage.digibackend.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class DeviceService implements IDeviceService {
    @Autowired
    DeviceRepository deviceRepository;
    private final SimpMessagingTemplate messagingTemplate;
@Autowired
    DataSensorRepository dataSensorRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    SensorService sensorService;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    IDataSensorService dataSensor;
    @Autowired
    public DeviceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    Boolean verifyMacAdd(String addMac)
    {
         final String MAC_ADDRESS_PATTERN = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        Pattern pattern = Pattern.compile(MAC_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(addMac);
        return matcher.matches();

    }
    @Override

    public String addDevice(Device device) {
        String macAdd = device.getMacAdress();

        if (deviceRepository.findBymacAdress(macAdd) != null) {
            return "Error: A device already exists with the MAC address";
        }

        if (!verifyMacAdd(macAdd)) {
            return "Error: Invalid MAC address";
        }
        Device savedDevice = deviceRepository.save(device);
       /* List <Sensor> sensorList= getSensorsList(savedDevice.getDeviceId());
        Map<String, Integer> sensorCountMap = new HashMap<>();
        for (Sensor sensor : sensorList) {
            int sensorCount = sensorCountMap.getOrDefault(sensor, 0) + 1;
            sensorCountMap.put(sensor.getSensorName(), sensorCount);
            String modifiedSensorName = sensor.getSensorName() + sensorCount;
            sensor.setSensorName(modifiedSensorName);

        }*/
        for (String sensor : savedDevice.getSensorList()) {
            dataSensor.affecteSensorDevice(sensor, savedDevice.getDeviceId());
        }

        return "ok";
    }
    public List<Device> getActiveDevices() {
        List<Device> devices = getAllDevices();
        List<Device> activeDevices = new ArrayList<>();
        for (Device device : devices) {
            if (device.getActive()) {
                activeDevices.add(device);
            }
        }
        return activeDevices;
    }
    @Override
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Override
    public Device getDeviceById(String deviceId) {
        Optional<Device> existingDeviceOptional = deviceRepository.findById(deviceId);
        if (!existingDeviceOptional.isPresent()) {
            System.out.println("Device not found!");
            return null;
        }
        Device device = existingDeviceOptional.get();

        System.out.println(device);
        return device;
    }


    @Override
    public Device getDeviceByMacAdd(String add_mac) {
        return deviceRepository.findBymacAdress(add_mac);
    }
    @Override
    public String setDeviceState(String deviceId) {
        Optional<Device> existingDeviceOptional = deviceRepository.findById(deviceId);
        if (!existingDeviceOptional.isPresent()) {
            System.out.println("Device not found!");
            return "No device was found with this specific id";
        }

        Device existingDevice = existingDeviceOptional.get();
        existingDevice.setActive(!existingDevice.getActive());
        deviceRepository.save(existingDevice);
        System.out.println("State set to " + existingDevice.getActive());
        return "State set to " + existingDevice.getActive();
    }

    @Override
    public deviceResponse updateDevice(String deviceId, Device deviceRequest) {
        deviceResponse response = null;
        Device existingDevice = deviceRepository.findById(deviceId).orElse(null);
        if (existingDevice != null) {
            if (deviceRequest.getDescription() != null) {
                existingDevice.setDescription(deviceRequest.getDescription());
            }
            if (deviceRequest.getSensorList() != null && !deviceRequest.getSensorList().isEmpty()) {
                existingDevice.setSensorList(deviceRequest.getSensorList());
            }


            if (deviceRequest.getLocation() != null) {
                existingDevice.setLocation(deviceRequest.getLocation());
            }

            if (deviceRequest.getMacAdress() != null) {
                existingDevice.setMacAdress(deviceRequest.getMacAdress());
            }
            if (deviceRequest.getIdClient() != null) {
                existingDevice.setIdClient(deviceRequest.getIdClient());
            }
            if (deviceRequest.getLat() != null) {
                existingDevice.setLat(deviceRequest.getLat());
            }
            if (deviceRequest.getNom() != null) {
                existingDevice.setLat(deviceRequest.getLat());
            }
            if (deviceRequest.getLng() != null) {
                existingDevice.setLng(deviceRequest.getLng());
            }
            if (deviceRequest.getIdAdmin() != null) {
                existingDevice.setIdAdmin(deviceRequest.getIdAdmin());
            }
            deviceRepository.save(existingDevice);
            response = new deviceResponse(OtpStatus.SUCCED, existingDevice);
        } else {
            response = new deviceResponse(OtpStatus.FAILED, existingDevice);
        }
        return response;
    }

    @Override
    public String deleteDevice(String deviceId) {
        deviceRepository.deleteById(deviceId);
        return deviceId +"   Device deleted succesully";
    }

    @Override
    public String affectDeviceToAdmin(String deviceId,String adminId) throws MessagingException, UnsupportedEncodingException {

        Device existingDevice= deviceRepository.findById(deviceId).get();
        if(existingDevice.getIdAdmin()==null) {
            String randomCode = RandomStringUtils.random(6, true, true);
            existingDevice.setIdAdmin(adminId);
            existingDevice.setDeviceCode(randomCode);
            existingDevice.setActive(true);
            deviceRepository.save(existingDevice);
            //get the user with the specific id to send him  mail with the mac address
            User currentAdmin = userRepository.findById(adminId).get();
            System.out.println(currentAdmin);
            //send mail to this user
            String toAddress = currentAdmin.getEmail();
            String fromAddress = "alert@dig2s.com";
            String senderName = "Digi-Smart-Solution";
            String subject = "Your code for your device:";
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
                    "                        <h1 style=\"margin: 0; font-size: 24px; line-height: 32px; line-height: normal; font-weight: bold;\">Your device mac address</h1>\n" +
                    "                      </div>\n" +
                    "                    </td>\n" +
                    "                  </tr>\n" +
                    "                  <tr>\n" +
                    "                    <td align=\"left\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                    "                      <div style=\"font-family:Nunito, Helvetica, Arial, sans-serif;font-size:16px;font-weight:400;line-height:20px;text-align:left;color:#54595f;\">\n" +
                    "                        <p style=\"margin: 5px 0;\">Hi ," +currentAdmin.getUsername()+"</p>\n" +
                    "                        <p style=\"margin: 5px 0;\">you will find attached the mac address of your device:</p>\n" +
                    "                      </div>\n" +
                    "                    </td>\n" +
                    "                  </tr>\n" +
                    "                  <tr>\n" +
                    "                    <td align=\"center\" vertical-align=\"middle\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                    "                      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"border-collapse:separate;line-height:100%;\">\n" +
                    "                        <tbody><tr>\n" +
                    "                          <td align=\"center\" bgcolor=\"#54595f\" role=\"presentation\" style=\"border:none;border-radius:30px;cursor:auto;mso-padding-alt:10px 25px;background:#54595f;\" valign=\"middle\">\n" +
                    "                            <button disabled \"" +
                    "\" style=\"display: inline-block;  background: #54595f; color: white; font-family: Nunito, Helvetica, Arial, sans-serif; font-size: 16px; font-weight: normal; line-height: 30px; margin: 0; text-decoration: none; text-transform: none; padding: 10px 25px; mso-padding-alt: 0px; border-radius: 30px;\" target=\"_blank\"> " +existingDevice.getDeviceCode()+
                    "</button>\n" +
                    "                          </td>\n" +
                    "                        </tr>\n" +
                    "                      </tbody></table>\n" +
                    "                    </td>\n" +
                    "                  </tr>\n" +
                    "                  <tr>\n" +
                    "                    <td align=\"left\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                    "                      <div style=\"font-family:Nunito, Helvetica, Arial, sans-serif;font-size:16px;font-weight:400;line-height:20px;text-align:left;color:#54595f;\">\n" +
                    "                        <p style=\"margin: 5px 0;\"></p>\n" +
                    "                      </div>\n" +
                    "                    </td>\n" +
                    "                  </tr>\n" +
                    "                  <tr>\n" +
                    "                    <td align=\"left\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                    "                      <div style=\"font-family:Nunito, Helvetica, Arial, sans-serif;font-size:16px;font-weight:400;line-height:20px;text-align:left;color:#54595f;\">\n" +
                    "                        <p style=\"margin: 5px 0;\">Thanks, <br /> </p>\n" +
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(fromAddress, senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);

            return "Device" + deviceId + "affected succesufully to admin " + adminId;
        }
        else {
            return "Device already affected";

        }

    }

    @Override
    public String affectDeviceToClient(String deviceId, String clientId) {
        Device existingDevice = deviceRepository.findById(deviceId).get();
        if (existingDevice == null) {
            System.out.println("Device not found!");
            return "no device exists with this id";
        }
        if(existingDevice.getIdClient()!=null) {
            return "device already affected to a client";

        }
        String randomCode = RandomStringUtils.random(6, true, true);
        existingDevice.setIdClient(clientId);
        existingDevice.setDeviceCode(randomCode);
        deviceRepository.save(existingDevice);
            return "device successufully  affected to a client" + existingDevice.getIdClient();

        }

    @Override
    public List<Sensor> getSensorsList(String deviceId) {
        Device existingDevice = deviceRepository.findById(deviceId).get();
        if (existingDevice == null) {
            System.out.println("Device not found!");
            return Collections.emptyList();
        }
        List<String> sensorIds = existingDevice.getSensorList();
        List<Sensor> sensors = new ArrayList<>();
        for (String sensorId : sensorIds) {
            Sensor sensor = sensorService.getSensor(sensorId);
            if (sensor != null) {
                sensors.add(sensor);
            }
        }
        return sensors;
    }

    @Override
    public List<Device> getAdminDevices(String adminId) {
        Optional<User> optionalUser = userRepository.findById(adminId);
        if (!optionalUser.isPresent()) {
            return Collections.emptyList();
        }
        User user = optionalUser.get();
        System.out.println(user);
        List<Device> devices = getAllDevices();
        List<Device> userDevices = new ArrayList<>();
        for (Device device : devices) {
            if (device.getIdAdmin() != null && device.getIdAdmin().equals(adminId)) {
                System.out.println(device);
                userDevices.add(device);
            }
        }
        return userDevices;
    }


    @Override
    public List<Device> getClientDevices(String clientId) {
        Optional<User> optionalUser = userRepository.findById(clientId);
        if (!optionalUser.isPresent()) {
            return Collections.emptyList();
        }
        User user = optionalUser.get();
        System.out.println(user);

        List<Device> devices = getAllDevices();
        List<Device> clientDevices = new ArrayList<>();
        for (Device device : devices) {
            if (device.getIdClient().equals(clientId)) {
                System.out.println(device);
                clientDevices.add(device);
            }
        }
        return clientDevices;
    }
/*Send notification */
private void sendNotification(String clientId, String message) {
    messagingTemplate.convertAndSendToUser(clientId, "/sensorNotification", message);
}
    @Override
    public void checkAndSendNotification(String deviceId){
        Device existingDevice = deviceRepository.findById(deviceId).get();
        if (existingDevice == null) {

            System.out.println("Device not found!");
            return;
        }
        List<Sensor> sensorList=getSensorsList(deviceId);
        for (Sensor s : sensorList) {
            DataSensor dataSensor = dataSensorRepository.findDataSensorByDeviceAndSensor(existingDevice,s);
            if(dataSensor.getData()<s.getRangeMin() || dataSensor.getData()>s.getRangeMax())
            {
                String notificationMessage = "Sensor value warning! The current value is: " + dataSensor.getData();
                sendNotification(existingDevice.getIdAdmin(), notificationMessage);
                sendNotification(existingDevice.getIdClient(),notificationMessage);
            }
        }

    }


}