package com.mkvsk.warehousewizard.ui.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mkvsk.warehousewizard.core.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM product")
    List<Product> getAllProducts();

    @Query("SELECT * FROM product WHERE id = :id")
    Product getById(long id);

    @Query("SELECT * FROM product WHERE code = :code")
    Product getByCode(String code);

    @Query("SELECT * FROM product WHERE title = :title")
    Product getByTitle(String title);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);
}

