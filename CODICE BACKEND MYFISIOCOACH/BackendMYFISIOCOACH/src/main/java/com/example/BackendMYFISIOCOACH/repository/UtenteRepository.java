package com.example.BackendMYFISIOCOACH.repository;

import com.example.BackendMYFISIOCOACH.entità.Utente;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, String> {

    // Metodo per il login
    Optional<Utente> findByEmailAndPassword(String email, String password);

    // Metodo per l'accesso alla modifica della password
    @Query("SELECT u FROM Utente u WHERE u.email = :email AND u.nome = :nome AND u.cognome = :cognome")
    Optional<Utente> findByDatiDiVerifica(String email, String nome, String cognome);

    // Metodo per verificare l'email dell'utente
    @Query("SELECT u FROM Utente u WHERE u.email = :email")
    Optional<Utente> findByEmail(String email);

    // Metodo per modificare la password
    @Modifying
    @Transactional
    @Query("UPDATE Utente u SET u.password = :nuovapassword WHERE u.email = :email")
    void modificaPassword(@Param("email") String email, @Param("nuovapassword") String nuovapassword);

    // Metodo per ottenere il nome dell'utente
    @Query("SELECT u.nome FROM Utente u WHERE u.email = :email")
    String findNomeByEmail(@Param("email") String email);

    // Metodo per ottnere lo stage dell'utente
    @Query("SELECT u.stage FROM Utente u WHERE u.email = :email")
    Integer findStageByEmail(@Param("email") String email);

    // Metodo per modificare lo stage
    @Modifying
    @Transactional
    @Query("UPDATE Utente u SET u.stage = :stage WHERE u.email = :email")
    void modificaStage(@Param("email") String email, @Param("stage") int stage);

    // Metodo per ottenere il cognome dell'utente
    @Query("SELECT u.cognome FROM Utente u WHERE u.email = :email")
    String findCognomeByEmail(@Param("email") String email);

    // Metodo per ottenere il peso dell'utente
    @Query("SELECT u.peso FROM Utente u WHERE u.email = :email")
    Integer findPesoByEmail(@Param("email") String email);

    // Metodo per ottenere l'altezza dell'utente
    @Query("SELECT u.altezza FROM Utente u WHERE u.email = :email")
    Integer findAltezzaByEmail(@Param("email") String email);

    // Metodo per ottenere l'età dell'utente
    @Query("SELECT u.eta FROM Utente u WHERE u.email = :email")
    Integer findEtaByEmail(@Param("email") String email);

    // Metodo per ottenere il sesso dell'utente
    @Query("SELECT u.sesso FROM Utente u WHERE u.email = :email")
    String findSessoByEmail(@Param("email") String email);

    // Metodo per ottenere la password dell'utente
    @Query("SELECT u.password FROM Utente u WHERE u.email = :email")
    String findPasswordByEmail(@Param("email") String email);

    // Metodo per modificare email e password dell'utente
    @Modifying
    @Transactional
    @Query("UPDATE Utente u SET u.email = :nuovaemail, u.password = :nuovapassword WHERE u.email = :email")
    void modificaEmailPassword(@Param("email") String email, @Param("nuovaemail") String nuovaemail,
            @Param("nuovapassword") String nuovapassword);


    // Metodo per registrare un nuovo utente con stage 1
    @Modifying
    @Transactional
    @Query("INSERT INTO Utente u (u.email, u.password, u.nome, u.cognome, u.peso, u.altezza, u.eta, u.sesso, u.stage) VALUES (:email, :password, :nome, :cognome, :peso, :altezza, :eta, :sesso, 1)")
    void registraUtente(@Param("email") String email, @Param("password") String password, @Param("nome") String nome,
            @Param("cognome") String cognome, @Param("peso") int peso, @Param("altezza") int altezza, @Param("eta") int eta,
            @Param("sesso") String sesso);

            
}
