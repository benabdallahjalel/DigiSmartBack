package com.stage.digibackend.repository;


import com.stage.digibackend.Collections.ERole;
import com.stage.digibackend.Collections.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
  Optional<Role> findByName(ERole name);
}
