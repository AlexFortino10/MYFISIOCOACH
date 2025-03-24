package com.example.BackendMYFISIOCOACH.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BackendMYFISIOCOACH.entità.GiorniAllenamento;

import jakarta.transaction.Transactional;

@Repository
public interface GiorniAllenamentoRepository extends JpaRepository<GiorniAllenamento, Long> {

    @Query("SELECT MAX(g.giorno) FROM GiorniAllenamento g WHERE g.email = :email AND g.mese = :mese")
    Integer findUltimoGiornoAllenamento(@Param("email") String email, @Param("mese") int mese);

    @Query("SELECT g.giorno FROM GiorniAllenamento g WHERE g.email = :email AND g.mese = :mese")
    List<Integer> findGiorniAllenamento(@Param("email") String email, @Param("mese") int mese);

    // Metodo per inserire un giorno di allenamento
    @Modifying
    @Transactional
    @Query("INSERT INTO GiorniAllenamento g (g.email, g.giorno, g.mese) VALUES (:email, :giorno, :mese)")
    void insertGiornoAllenamento(@Param("email") String email, @Param("giorno") int giorno, @Param("mese") int mese);

    // Metodo per ottener i giorni di allenamento di un utente di uno specifico mese
    @Query("SELECT g.giorno FROM GiorniAllenamento g WHERE g.email = :email AND g.mese = :mese")
    ArrayList<Integer> getGiorniAllenamento(@Param("email") String email, @Param("mese") int mese);

}
