package com.example.BackendMYFISIOCOACH.repository;

import com.example.BackendMYFISIOCOACH.entità.ResocontoUtente;

import jakarta.transaction.Transactional;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResocontoRepository extends JpaRepository<ResocontoUtente, String> {
    // Metodo per trovare il reseconto di un utente tramite l'email
    @Query("SELECT r FROM ResocontoUtente r WHERE r.email = :email")
    Optional<ResocontoUtente> findByEmail(@Param("email") String email);

    // Metodo per creare un resoconto
    @Modifying
    @Transactional
    @Query("INSERT INTO ResocontoUtente (email,minuti,secondi,numallenamenti,serie,recordpersonale) VALUES (:email,0,0,0,0,0)")
    void creaResoconto(@Param("email") String email);

    // Metodo per ottenere i minuti di un utente
    @Query("SELECT r.minuti FROM ResocontoUtente r WHERE r.email = :email")
    int findMinutiByEmail(@Param("email") String email);

    // Metodo per ottenere i secondi di un utente
    @Query("SELECT r.secondi FROM ResocontoUtente r WHERE r.email = :email")
    int findSecondiByEmail(@Param("email") String email);

    // Metodo per ottenere il numero di allenamenti di un utente
    @Query("SELECT r.numallenamenti FROM ResocontoUtente r WHERE r.email = :email")
    int findNumallenamentiByEmail(@Param("email") String email);

    // Metodo per ottenere il numero di serie di un utente
    @Query("SELECT r.serie FROM ResocontoUtente r WHERE r.email = :email")
    int findSerieByEmail(@Param("email") String email);

    // Metodo per ottenere il record personale di un utente
    @Query("SELECT r.recordpersonale FROM ResocontoUtente r WHERE r.email = :email")
    int findRecordpersonaleByEmail(@Param("email") String email);

    // Metodo per aggiornare il resoconto di un utente
    @Modifying
    @Transactional
    @Query("UPDATE ResocontoUtente r SET r.minuti = :minuti, r.secondi = :secondi, r.numallenamenti = :numallenamenti, r.serie = :serie, r.recordpersonale = :recordpersonale WHERE r.email = :email")
    void updateResoconto(@Param("email") String email, @Param("minuti") int minuti, @Param("secondi") int secondi,
            @Param("numallenamenti") int numallenamenti, @Param("serie") int serie,
            @Param("recordpersonale") int recordpersonale);
}
