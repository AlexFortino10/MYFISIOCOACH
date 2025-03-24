package com.example.BackendMYFISIOCOACH.servizi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.example.BackendMYFISIOCOACH.repository.AllenamentoGiornalieroRepository;

import jakarta.transaction.Transactional;

@Service
public class AllenamentoGiornalieroService {

    private final AllenamentoGiornalieroRepository repository;

    public AllenamentoGiornalieroService(AllenamentoGiornalieroRepository repository) {
        this.repository = repository;
    }

    public Optional<String> getAllenamentoGiornaliero(int giorno, int numeroAllenamento) {
        return repository.findUrlVideoByGiornoAndNumero(giorno, numeroAllenamento);
    }

    public Optional<String> getIstruzioniAllenamentoGiornaliero(int giorno, int numeroAllenamento) {
        return repository.findIstruzioniByGiornoAndNumero(giorno, numeroAllenamento);
    }

    public Optional<String> getBeneficiAllenamentoGiornaliero(int giorno, int numeroAllenamento) {
        return repository.findBeneficiByGiornoAndNumero(giorno, numeroAllenamento);
    }

    public Integer getOraAllenamento(int giorno, int numeroAllenamento) {

        return repository.findOraByGiornoAndNumero(giorno, numeroAllenamento);
    }

    public Integer getMinutiAllenamento(int giorno, int numeroAllenamento) {
        return repository.findMinutiByGiornoAndNumero(giorno, numeroAllenamento);
    }

    public Integer getOraPrimoAllenamentoGiornoSuccessivo() {
        int giorno = java.time.LocalDate.now().plusDays(1).getDayOfMonth();
        return repository.findOraByGiornoAndNumero(giorno, 1);
    }

    public Integer getMinutiPrimoAllenamentoGiornoSuccessivo() {
        int giorno = java.time.LocalDate.now().plusDays(1).getDayOfMonth();
        return repository.findMinutiByGiornoAndNumero(giorno, 1);
    }

    // Metodo per inserire un'allenamento giornaliero
    @Transactional
    public CompletableFuture<Boolean> insertAllenamentoGiornaliero(int giorno, int numero, String urlvideo,
            String istruzioni, String benefici, int ora, int minuti) {
        return CompletableFuture.supplyAsync(() -> {
            repository.insertAllenamentoGiornaliero(giorno, numero, urlvideo, istruzioni, benefici, ora, minuti);
            return true;
        });
    }

    // Metodo per eliminare un'allenamento giornaliero in base al numero
    @Transactional
    public CompletableFuture<Boolean> deleteAllenamentoGiornalieroByNumero(int numero) {
        return CompletableFuture.supplyAsync(() -> {
            repository.deleteAllenamentoGiornalieroByNumero(numero);
            return true;
        });
    }

    // Metodo per modificare le istruzioni di un allenamento in base al numero
    @Transactional
    public CompletableFuture<Boolean> modificaIstruzioniByNumero(int numero, String nuoveistruzioni) {
        return CompletableFuture.supplyAsync(() -> {
            repository.modificaIstruzioniByNumero(numero, nuoveistruzioni);
            return true;
        });
    }

    // Metodo per modificare l'url del video di un allenamento in base al numero
    @Transactional
    public CompletableFuture<Boolean> modificaUrlVideoByNumero(int numero, String nuovourlvideo) {
        return CompletableFuture.supplyAsync(() -> {
            repository.modificaUrlVideoByNumero(numero, nuovourlvideo);
            return true;
        });
    }

    // Metodo per modificare i benefici di un allenamento in base al numero
    @Transactional
    public CompletableFuture<Boolean> modificaBeneficiByNumero(int numero, String nuovibenefici) {
        return CompletableFuture.supplyAsync(() -> {
            repository.modificaBeneficiByNumero(numero, nuovibenefici);
            return true;
        });
    }

}
