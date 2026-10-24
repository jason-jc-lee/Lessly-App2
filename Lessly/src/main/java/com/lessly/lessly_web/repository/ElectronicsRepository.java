package com.lessly.lessly_web.repository;

import com.lessly.lessly_web.model.Electronics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * ElectronicsRepository provides database access for Electronics items.
 * By extending JpaRepository, Spring will automatically generate database operations (save, find, delete, count) without the need for SQL to be written manually.
 * This allows the code to be more clean and minimal.
 */
@Repository
public interface ElectronicsRepository extends JpaRepository<Electronics, Long> {
}