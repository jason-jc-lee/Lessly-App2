package com.lessly.lessly_web.repository;

import com.lessly.lessly_web.model.Perishables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * PerishablesRepository provides database access for Perishables items.
 * By extending JpaRepository, Spring will automatically generate database operations (save, find, delete, count) without the need for SQL to be written manually.
 * This allows the code to be more clean and minimal.
 */
@Repository
public interface PerishablesRepository extends JpaRepository<Perishables, Long> {
}