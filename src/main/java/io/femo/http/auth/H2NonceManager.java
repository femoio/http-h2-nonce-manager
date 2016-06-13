package io.femo.http.auth;

import io.femo.http.HttpException;
import io.femo.http.handlers.Authentication;
import io.femo.http.handlers.auth.NonceManager;
import io.femo.http.handlers.auth.SimpleNonceManager;

import java.sql.*;
import java.util.UUID;

/**
 * Created by felix on 6/13/16.
 */
public class H2NonceManager implements NonceManager {

    private Connection connection;

    public H2NonceManager(String databaseName) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:h2:./" + databaseName);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS nonces (nonce CHAR(36) PRIMARY KEY, nc INTEGER NOT NULL, opaque CHAR(36) NOT NULL)");
            connection.commit();
        }
    }

    public String generateNew() {
        try (Statement statement = connection.createStatement()) {
            String nonce = UUID.randomUUID().toString();
            statement.execute("INSERT INTO NONCES (NONCE, NC, OPAQUE) VALUES ('" + nonce + "', 0, '" +
                    UUID.randomUUID().toString() + "')");
            connection.commit();
            return nonce;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getOpaque(String nonce) {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT OPAQUE FROM NONCES WHERE NONCE = '" + nonce + "'");
            if(resultSet.first()) {
                return resultSet.getString(1);
            }
            return "";
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean verifyAndUpdate(String nonce, String nc) {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT NC FROM NONCES WHERE NONCE = '" + nonce + "'");
            if(resultSet.first()) {
                int counter = resultSet.getInt(1);
                int newCounter = Integer.parseInt(nc);
                if(newCounter > counter) {
                    statement.execute("UPDATE NONCES SET NC = " + newCounter + " WHERE NONCE = '" + nonce + "'");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return true;
    }

    public static NonceManager h2OrDefault(String databaseName) {
        try {
            return new H2NonceManager(databaseName);
        } catch (SQLException e) {
            e.printStackTrace();
            return new SimpleNonceManager();
        }
    }
}
