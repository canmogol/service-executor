package com.fererlab.content;

import com.fererlab.log.FLogger;
import com.fererlab.util.Maybe;

import java.net.URI;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCContentLoader implements ContentLoader {

    private static final Logger log = FLogger.getLogger(JDBCContentLoader.class.getSimpleName());

    private Map<String, String> dbTypeDriver = new HashMap<String, String>() {{
        // TODO also add other db drivers from configuration
        put("postgresql", "org.postgresql.Driver");
    }};


    @Override
    public Maybe<String> load(URI uri) {
        String content = null;
        Map<String, String> connectionParams = new HashMap<>();
        String connectionString = uri.getScheme() + ":" + uri.getSchemeSpecificPart();
        String[] connectionAndParams = connectionString.split("\\?");
        String connectionURL = connectionAndParams[0];
        String dbType = uri.getSchemeSpecificPart().split(":")[0];
        if (connectionAndParams.length > 1) {
            String paramString = connectionAndParams[1];
            String[] paramPairs = paramString.split("&");
            for (String pair : paramPairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length > 1) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    connectionParams.put(key, value);
                }
            }
        }

        String fileContentFieldName = connectionParams.getOrDefault("content", "file_content");
        String tableName = connectionParams.getOrDefault("table", "services");
        String programmingLanguage = connectionParams.getOrDefault("programming_language", "java");
        String service = connectionParams.getOrDefault("service_name", "Service");

        try {
            Class.forName(dbTypeDriver.get(dbType)).asSubclass(Driver.class);
            String username = connectionParams.get("username");
            String password = connectionParams.get("password");
            Connection connection = DriverManager.getConnection(connectionURL, username, password);
            String statementQuery = "select t." + fileContentFieldName + " from " + tableName + " t where t.programming_language = ? and t.service_name = ?";
            PreparedStatement statement = connection.prepareStatement(statementQuery);
            statement.setString(1, programmingLanguage);
            statement.setString(2, service);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                content = resultSet.getString(fileContentFieldName);
                log.info("got content from from table: " + tableName + " at field: " + fileContentFieldName + " language: " + programmingLanguage + " service: " + service);
            }
        } catch (Exception e) {
            String error = "got exception while getting content from database, URI: " + uri + " exception: " + e;
            log.log(Level.SEVERE, error, e);
        }
        return Maybe.create(content);
    }
}
