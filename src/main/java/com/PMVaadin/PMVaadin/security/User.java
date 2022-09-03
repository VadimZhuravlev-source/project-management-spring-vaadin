package com.PMVaadin.PMVaadin.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
@Transactional
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String address;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "password")
    private byte[] password;

    private User(UserBuilder userBuilder) {
        this.id = userBuilder.id;
        this.firstName = userBuilder.firstName;
        this.lastName = userBuilder.lastName;
        this.role = userBuilder.role;
        this.phoneNumber = userBuilder.phoneNumber;
        this.address = userBuilder.address;
        this.isActive = userBuilder.isActive;
        this.password = userBuilder.password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return id == user.id && Objects.equals(phoneNumber, user.phoneNumber)
                && isActive == user.isActive && Objects.equals(firstName, user.firstName)
                && Objects.equals(lastName, user.lastName)
                && role == user.role
                && Objects.equals(address, user.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName,
                role,
                phoneNumber, address, isActive);
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", firstName=" + firstName + ", lastName=" + lastName +
                ", role=" + role +
                ", phoneNumber=" + phoneNumber + ", address=" + address + ", isActive=" + isActive + '}';
    }

    public static class UserBuilder {
        private Integer id;
        private String firstName;
        private String lastName;
        private Role role;
        private String phoneNumber;
        private String address;
        private boolean isActive;
        private byte[] password;

        public UserBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserBuilder address(String address) {
            this.address = address;
            return this;
        }

        public UserBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public UserBuilder password(byte[] password) {
            this.password = password;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}