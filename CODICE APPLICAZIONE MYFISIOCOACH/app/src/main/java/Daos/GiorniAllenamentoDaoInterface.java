package Daos;

import java.util.ArrayList;
import java.util.concurrent.Future;

public interface GiorniAllenamentoDaoInterface {
    void InserisciGiorniAllenamento(String email, int giorno, int mese);
    Future<Integer> getUltimoGiornoAllenamento(String email,int mese);
    int getNumeroMese(String mese);
    Future<ArrayList<Integer>> getGiorniAllenamento(String email, int mese);
}
