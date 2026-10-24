package com.lessly.lessly_web.repository;

import com.lessly.lessly_web.model.Clothing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * ClothingRepository provides database access for Clothing items.
 * By extending JpaRepository, Spring will automatically generate database operations (save, find, delete, count) without the need for SQL to be written manually.
 * This allows the code to be more clean and minimal.
 */
@Repository
public interface ClothingRepository extends JpaRepository<Clothing, Long> {
}