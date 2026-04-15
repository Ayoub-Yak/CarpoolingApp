package com.covoiturage.dao;

import com.covoiturage.model.User;
import java.util.List;

/**
 * Interface DAO pour les opérations sur les utilisateurs.
 */
public interface UserDao {

    void save(User user);

    User findById(int id);

    User findByEmail(String email);

    List<User> findAll();

    List<User> findByType(String type);

    void update(User user);

    void delete(int id);
}
