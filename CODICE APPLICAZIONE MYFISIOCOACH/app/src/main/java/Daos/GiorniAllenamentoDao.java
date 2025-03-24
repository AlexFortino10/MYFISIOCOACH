package Daos;

import com.example.myfisiocoach.ConnectionClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GiorniAllenamentoDao implements GiorniAllenamentoDaoInterface {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    @Override
    public void InserisciGiorniAllenamento(String email, int giorno, int mese){
        //Inserisco i giorni di allenamento nella tabella giorniallenamento
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                connection = ConnectionClass.connect();
                String query = "INSERT INTO giorniallenamento (email,giorno,mese) VALUES (?,?,?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, email);
                preparedStatement.setInt(2, giorno);
                preparedStatement.setInt(3, mese);
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                //Chiudo tutte le risorse
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public Future<Integer> getUltimoGiornoAllenamento(String email,int mese) {
        //Recupero l'ultimo giorno di allenamento dell'utente
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        return executorService.submit(() -> {
            try {
                connection = ConnectionClass.connect();
                String query = "SELECT MAX(giorno) FROM giorniallenamento WHERE email = ? AND mese = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, email);
                preparedStatement.setInt(2, mese);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                } else {
                    return 0;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                //Chiudo tutte le risorse
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public int getNumeroMese(String mese) {
        //Recupero il numero del mese
        switch (mese){
            case "gennaio":
                return 1;
            case "febbraio":
                return 2;
            case "marzo":
                return 3;
            case "aprile":
                return 4;
            case "maggio":
                return 5;
            case "giugno":
                return 6;
            case "luglio":
                return 7;
            case "agosto":
                return 8;
            case "settembre":
                return 9;
            case "ottobre":
                return 10;
            case "novembre":
                return 11;
            case "dicembre":
                return 12;
            default:
                return 0;
        }
    }

    @Override
    public Future<ArrayList<Integer>> getGiorniAllenamento(String email, int mese) {
        //Recupero i giorni di allenamento dell'utente
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        return executorService.submit(() -> {
            try {
                connection = ConnectionClass.connect();
                String query = "SELECT giorno FROM giorniallenamento WHERE email = ? AND mese = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, email);
                preparedStatement.setInt(2, mese);
                resultSet = preparedStatement.executeQuery();
                ArrayList<Integer> giorni = new ArrayList<>();
                while (resultSet.next()) {
                    giorni.add(resultSet.getInt(1));
                }
                return giorni;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                //Chiudo tutte le risorse
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
