package com.swp.migrate;


import com.swp.entity.RoleEntity;
import com.swp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
public class RoleInit implements CommandLineRunner {

    private final RoleRepository roleRepository;
    @Override
    public void run(String... args) throws Exception {
        if(roleRepository.count() == 0) {
            RoleEntity roleAdmin = new RoleEntity();
            roleAdmin.setName("ADMIN");
            RoleEntity roleStaff = new RoleEntity();
            roleStaff.setName("STAFF");
            RoleEntity roleUser = new RoleEntity();
            roleUser.setName("USER");

            roleRepository.save(roleAdmin);
            roleRepository.save(roleStaff);
            roleRepository.save(roleUser);
        }
    }
}
