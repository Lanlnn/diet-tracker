 package com.diettracker.repository;
 
 import com.diettracker.entity.User;
 import org.springframework.data.jpa.repository.JpaRepository;
 
 public interface UserRepository extends JpaRepository<User, String> {
 }
