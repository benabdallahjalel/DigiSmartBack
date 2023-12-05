package com.stage.digibackend.controllers;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.stage.digibackend.Collections.ERole;
import com.stage.digibackend.Collections.Role;
import com.stage.digibackend.Collections.User;
import com.stage.digibackend.payload.request.LoginRequest;
import com.stage.digibackend.payload.request.SignupRequest;
import com.stage.digibackend.payload.response.JwtResponse;
import com.stage.digibackend.payload.response.MessageResponse;
import com.stage.digibackend.repository.RoleRepository;
import com.stage.digibackend.repository.UserRepository;
import com.stage.digibackend.security.jwt.JwtUtils;
import com.stage.digibackend.security.services.TokenService;
import com.stage.digibackend.security.services.UserDetailsImpl;
import com.stage.digibackend.services.Userservice;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import java.io.UnsupportedEncodingException;

import java.net.InetAddress;

import java.net.UnknownHostException;
import java.util.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController implements InitializingBean {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	SessionRegistry sessionRegistry;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private Userservice userservice;



	private  TokenService tokenService;

	private Map<String, Integer> sessionCountMap = new HashMap<>();



	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {

		System.out.println(loginRequest.getEmail());
		Optional<User> userOptional = userRepository.findByEmailorEmail2(loginRequest.getEmail());

		System.out.println(userOptional);
		if (!userOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid email");
		}

		User u = userOptional.get();

		if (u.isEnabled() == false) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Verify your account");
		}
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(u.getEmail(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		String ipAddress = getIpAddress();
		System.out.println(ipAddress);
		//String email = u.getEmail();
		Integer numSessions = sessionCountMap.get(u.getEmail());
		if (numSessions == null) {
			numSessions = 1;
		} else if (numSessions >= 2) {
			// Maximum of three sessions reached
			System.out.println("Maximum number of sessions reached for this user");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Maximum number of sessions reached for this user." );
		} else {

			numSessions++;
		}

		sessionCountMap.put(u.getEmail(), numSessions);
		session.setAttribute("userEmail", u.getEmail());

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());
		//String xForwardedForHeader = request.getHeader("X-FORWARDED-FOR");
		//System.out.println(xForwardedForHeader);
		Map<String, String> result = new HashMap<>();


		System.out.println("user TO connect"+userDetails.getUsername()+
				userDetails.getEmail());
		return ResponseEntity.ok(new JwtResponse(jwt,
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				roles));

	}
	private String getIpAddress() {
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			System.out.println("******"+inetAddress.getHostName());
			return inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}

	}
	@PostMapping("/signup/{id}")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest,String siteURL,@PathVariable("id") String idUser) throws MessagingException, UnsupportedEncodingException {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(),
							 signUpRequest.getEmail(),
							 encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRoles();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.SUPER_ADMIN)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "client":
					Role modRole = roleRepository.findByName(ERole.CLIENT)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
				default:
					Role userRole = roleRepository.findByName(ERole.SUPER_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}
		User admin = userRepository.findById(idUser).get();
		user.setRoles(roles);
		//userRepository.save(user);


		String randomCode = RandomStringUtils.random(64, true, true);
		user.setVerificationCode(randomCode);
		user.setEnabled(false);
		user.setAdmin(admin);
		userRepository.save(user);
		System.out.println("registre");
		userservice.sendVerificationEmail(user, siteURL);
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
	public ResponseEntity<?> logoutUser(HttpServletRequest request) {
		// Get the user's email from the SecurityContext
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userEmail = ((UserDetailsImpl) authentication.getPrincipal()).getEmail();

		// Invalidate the session and decrement the number of sessions
		HttpSession session = request.getSession(false);
		if (session != null) {
			Integer numSessions = (Integer) session.getAttribute(userEmail);
			if (numSessions != null && numSessions > 0) {
				numSessions--;
				session.setAttribute(userEmail, numSessions);
				System.out.println("Number of sessions for " + userEmail + ": " + numSessions);
			}
			session.invalidate();
		}

		return ResponseEntity.ok(new MessageResponse("Logout successful!"));
	}


	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpSession session,@RequestBody String email) {
		Optional<User> user = userRepository.findByEmailorEmail2(email);
		if (user.isPresent()) {
			User u = user.get();
			Integer numSessions = sessionCountMap.get(u.getEmail());


			if (numSessions != null && numSessions > 0) {
				numSessions--;
				sessionCountMap.put(u.getEmail(), numSessions);
			}
			session.invalidate(); // Clear the user's session
		}
		return ResponseEntity.ok("Logged out successfully.");
	}
	@Override
	public void afterPropertiesSet() throws Exception {

	}
}
