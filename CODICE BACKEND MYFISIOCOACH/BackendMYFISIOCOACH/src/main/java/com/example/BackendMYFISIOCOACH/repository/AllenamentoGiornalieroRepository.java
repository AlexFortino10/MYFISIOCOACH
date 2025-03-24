package com.example.BackendMYFISIOCOACH.repository;

import com.example.BackendMYFISIOCOACH.entità.AllenamentoGiornaliero;

import jakarta.transaction.Transactional;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AllenamentoGiornalieroRepository extends JpaRepository<AllenamentoGiornaliero, Integer> {

    @Query("SELECT a.urlvideo FROM AllenamentoGiornaliero a WHERE a.giorno = :giorno AND a.numero = :numero")
    Optional<String> findUrlVideoByGiornoAndNumero(@Param("giorno") int giorno, @Param("numero") int numero);

    @Query("SELECT a.istruzioni FROM AllenamentoGiornaliero a WHERE a.giorno = :giorno AND a.numero = :numero")
    Optional<String> findIstruzioniByGiornoAndNumero(@Param("giorno") int giorno, @Param("numero") int numero);

    @Query("SELECT a.benefici FROM AllenamentoGiornaliero a WHERE a.giorno = :giorno AND a.numero = :numero")
    Optional<String> findBeneficiByGiornoAndNumero(@Param("giorno") int giorno, @Param("numero") int numero);

    @Query("SELECT a.ora FROM AllenamentoGiornaliero a WHERE a.giorno = :giorno AND a.numero = :numero")
    Integer findOraByGiornoAndNumero(@Param("giorno") int giorno, @Param("numero") int numero);

    @Query("SELECT a.minuti FROM AllenamentoGiornaliero a WHERE a.giorno = :giorno AND a.numero = :numero")
    Integer findMinutiByGiornoAndNumero(@Param("giorno") int giorno, @Param("numero") int numero);

    // Metodo per inserire un'allenamento giornaliero
    @Modifying
    @Transactional
    @Query("INSERT INTO AllenamentoGiornaliero (giorno, numero, urlvideo, istruzioni, benefici, ora, minuti) VALUES (:giorno, :numero, :urlvideo, :istruzioni, :benefici, :ora, :minuti)")
    void insertAllenamentoGiornaliero(@Param("giorno") int giorno, @Param("numero") int numero,
            @Param("urlvideo") String urlvideo, @Param("istruzioni") String istruzioni,
            @Param("benefici") String benefici, @Param("ora") int ora, @Param("minuti") int minuti);

    // Metodo per eliminare un'allenamento giornaliero in base al numero
    @Modifying
    @Transactional
    @Query("DELETE FROM AllenamentoGiornaliero a WHERE a.numero = :numero")
    void deleteAllenamentoGiornalieroByNumero(@Param("numero") int numero);

    // Metodo per modificare le istruzioni di un allenamento in base al numero
    @Modifying
    @Transactional
    @Query("UPDATE AllenamentoGiornaliero a SET a.istruzioni = :nuoveistruzioni WHERE a.numero = :numero")
    void modificaIstruzioniByNumero(@Param("numero") int numero, @Param("nuoveistruzioni") String nuoveistruzioni);

    // Metodo per modificare l'url del video di un allenamento in base al numero
    @Modifying
    @Transactional
    @Query("UPDATE AllenamentoGiornaliero a SET a.urlvideo = :nuovourlvideo WHERE a.numero = :numero")
    void modificaUrlVideoByNumero(@Param("numero") int numero, @Param("nuovourlvideo") String nuovourlvideo);

    // Metodo per modificare i benefici di un allenamento in base al numero
    @Modifying
    @Transactional
    @Query("UPDATE AllenamentoGiornaliero a SET a.benefici = :nuovibenefici WHERE a.numero = :numero")
    void modificaBeneficiByNumero(@Param("numero") int numero, @Param("nuovibenefici") String nuovibenefici);
}
