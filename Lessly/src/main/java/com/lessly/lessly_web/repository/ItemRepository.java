package com.lessly.lessly_web.repository;

import com.lessly.lessly_web.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
/**
 * ItemsRepository provides database access for Item types.
 * By extending JpaRepository, Spring will automatically generate database operations (save, find, delete, count) without the need for SQL to be written manually.
 * This allows the code to be more clean and minimal. In addition, it's the primary repository that DeclutterService uses to handle the logic on all items.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByCategory(String category);
}