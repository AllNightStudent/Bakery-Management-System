package com.swp.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private Boolean status = Boolean.TRUE;

    private String phone;

    private String name;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity role;


    
}
