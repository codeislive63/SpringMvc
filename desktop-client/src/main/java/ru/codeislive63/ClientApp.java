package ru.codeislive63;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientApp extends Application {

    private static final String DEFAULT_URL = "http://localhost:8080/";

    @Override
    public void start(Stage stage) {
        String url = resolveServerUrl();

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.load(url);

        BorderPane root = new BorderPane(webView);
        stage.setTitle("Railway Booker");
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }

    private String resolveServerUrl() {
        String fromArgs = getParameters().getNamed().get("serverUrl");

        if (fromArgs != null && !fromArgs.isBlank()) {
            return normalizeUrl(fromArgs);
        }

        Properties props = new Properties();

        try (InputStream is = ClientApp.class
                .getClassLoader()
                .getResourceAsStream("client.properties")) {
            if (is != null) {
                props.load(is);
                String fromProps = props.getProperty("server.url");

                if (fromProps != null && !fromProps.isBlank()) {
                    return normalizeUrl(fromProps);
                }
            }
        } catch (IOException ignored) { }

        return DEFAULT_URL;
    }

    private String normalizeUrl(String url) {
        String u = url.trim();
        if (!u.endsWith("/")) u += "/";
        return u;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
