package com.login_reistration.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.login_reistration.models.ERole;
import com.login_reistration.models.Role;
import com.login_reistration.models.User;
import com.login_reistration.payload.request.LoginRequest;
import com.login_reistration.payload.request.SignupRequest;
import com.login_reistration.payload.response.JwtResponse;
import com.login_reistration.payload.response.MessageResponse;
import com.login_reistration.repository.RoleRepository;
import com.login_reistration.repository.UserRepository;
import com.login_reistration.security.jwt.JwtUtils;
import com.login_reistration.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
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

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(jwt, 
                         userDetails.getId(), 
                         userDetails.getUsername(), 
                         userDetails.getEmail(), 
                         roles));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
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

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.System_Admin)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
        case "Inventory_Admin":
          Role inventoryadminRole = roleRepository.findByName(ERole.Inventory_Admin)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(inventoryadminRole);

          break;
        case "Store_Manager":
          Role storemanagerRole = roleRepository.findByName(ERole.Store_Manager)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(storemanagerRole);

          break;
        case "Purchase_Coordinator":
          Role purchasecoordinatorRole = roleRepository.findByName(ERole.Purchase_Coordinator)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(purchasecoordinatorRole);

          break;
        case "Storekeeper":
          Role storekeeperRole = roleRepository.findByName(ERole.Storekeeper)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(storekeeperRole);

          break;
        case "Designer":
          Role designerRole = roleRepository.findByName(ERole.Designer)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(designerRole);

          break;

        case "ShowRoom_Manager":
          Role showroommanagerRole = roleRepository.findByName(ERole.ShowRoom_Manager)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(showroommanagerRole);

          break;
        default:
          Role systemadminRole = roleRepository.findByName(ERole.System_Admin)
                  .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(systemadminRole);
        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }
}
