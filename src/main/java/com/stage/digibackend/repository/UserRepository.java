package com.stage.digibackend.repository;


import com.stage.digibackend.Collections.ERole;
import com.stage.digibackend.Collections.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

  List<User> findAll();
  Optional<User> findByUsername(String username);
  @Query(value = "{$or: [{'email': ?0}, {'email2': ?0}]}")
  Optional<User> findByEmailorEmail2(String email);
  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

User findByTelephone(String telephone);
  @Query(value="{'roles.id' : ?0}")
  List<User> findByRoleNot(String role);


  @Query(value="{'admin' : ?0}")
  List<User> findByAdmin(String admin);
  @Query(value = "{'email' : ?0}")
  User getUserByUsername(String username);

  @Query(value = "{'verify' : ?0}")
  User getUserCD(String code);

  @Query(value = "{'verificationCode' : ?0}")
  User findByVerificationCode(String verificationCode);

}
