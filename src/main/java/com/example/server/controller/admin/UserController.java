package com.example.server.controller.admin;

import com.example.server.dto.SignUpRequest;
import com.example.server.model.ERole;
import com.example.server.model.Role;
import com.example.server.model.User;
import com.example.server.repository.RoleRepository;
import com.example.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    public UserController(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }
    @GetMapping()
    public ResponseEntity<List<User>> getAllUser(){
        List<User> listUser = userRepository.findAll();
        if(listUser.isEmpty())
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(listUser, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<User> addNewUser(@RequestBody SignUpRequest signUpRequest){
        Set<Role> roles = new HashSet<>();
        Optional<Role> userRole = roleRepository.findByName(ERole.ROLE_MODERATOR);
        if (userRole.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        roles.add(userRole.get());
        String hashedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        User user = new User();
        user.setRoles(roles);
        user.setPassword(hashedPassword);
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
    }

    @PutMapping("{id_user}")
    public ResponseEntity<User> editUser(@RequestBody User user, @PathVariable Long id_user){
        Optional<User> edit_user = userRepository.findById(id_user);
        if(edit_user.isPresent()){
            User edit = edit_user.get();
            edit.setEmail(user.getEmail());
            edit.setPassword(passwordEncoder.encode(user.getPassword()));
            return new ResponseEntity<>(userRepository.save(edit), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @DeleteMapping("{id_user}")
    public ResponseEntity<User> deleteUser(@PathVariable Long id_user){
        Optional<User> user = userRepository.findById(id_user);
        Optional<Role> userRole = roleRepository.findByName(ERole.ROLE_ADMIN);
        if (userRole.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(user.isPresent()){
            if(user.get().getRoles().contains(userRole))
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            userRepository.deleteById(id_user);
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
