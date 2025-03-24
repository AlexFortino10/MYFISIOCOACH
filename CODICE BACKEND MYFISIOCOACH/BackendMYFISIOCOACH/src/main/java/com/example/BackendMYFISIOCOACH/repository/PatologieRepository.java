package com.example.BackendMYFISIOCOACH.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BackendMYFISIOCOACH.entità.Patologie;

import jakarta.transaction.Transactional;

@Repository
public interface PatologieRepository extends JpaRepository<Patologie, Long> {

    @Query("SELECT p.nome FROM Patologie p")
    List<String> getNomePatologie();

    @Query("SELECT p.nome FROM Patologie p WHERE p.nome NOT IN (SELECT pu.patologia FROM PatologieUtente pu WHERE pu.email = :email)")
    List<Patologie> getPatologieMancantiUtente(@Param("email") String email);

    @Query("SELECT pu.patologia FROM PatologieUtente pu WHERE pu.email = :email")
    ArrayList<String> getPatologieUtente(@Param("email") String email);

    @Query("SELECT p.urlFoto FROM FotoAllenamentiPatologie p WHERE p.patologia = :nomePatologia")
    Optional<String> getImmaginePatologia(@Param("nomePatologia") String nomePatologia);

    // Metodo per modificare l'email
    @Modifying
    @Transactional
    @Query("UPDATE PatologieUtente p SET p.email = :newEmail WHERE p.email = :oldEmail")
    void updateEmail(@Param("oldEmail") String oldEmail, @Param("newEmail") String newEmail);

    // Metodo per aggiungere una patologia a un determinato utente
    @Modifying
    @Transactional
    @Query("INSERT INTO PatologieUtente (email, patologia) VALUES (:email, :patologia)")
    void addPatologiaUtente(@Param("email") String email, @Param("patologia") String patologia);

    // Metodo per eliminare una patologia da un determinato utente
    @Modifying
    @Transactional
    @Query("DELETE FROM PatologieUtente p WHERE p.email = :email AND p.patologia = :patologia")
    void deletePatologiaUtente(@Param("email") String email, @Param("patologia") String patologia);
}