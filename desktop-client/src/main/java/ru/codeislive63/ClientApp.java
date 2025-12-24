package ru.codeislive63;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class ClientApp extends Application {

    private static final String DEFAULT_URL = "http://localhost:8080/";
    private String homeUrl;

    @Override
    public void start(Stage stage) {
        homeUrl = resolveServerUrl();

        WebView webView = new WebView();
        WebEngine engine = getWebEngine(webView);

        webView.setZoom(0.9);
        webView.setContextMenuEnabled(false);

        WebHistory history = engine.getHistory();

        Button backBtn = new Button("<");
        Button forwardBtn = new Button(">");
        Button refreshBtn = new Button("Reload");
        Button homeBtn = new Button("Home");

        backBtn.setOnAction(e -> {
            if (history.getCurrentIndex() > 0) {
                history.go(-1);
            }
        });

        forwardBtn.setOnAction(e -> {
            if (history.getCurrentIndex() < history.getEntries().size() - 1) {
                history.go(1);
            }
        });

        refreshBtn.setOnAction(e -> engine.reload());
        homeBtn.setOnAction(e -> engine.load(homeUrl));

        ToolBar toolbar = new ToolBar(backBtn, forwardBtn, refreshBtn, homeBtn);
        toolbar.setPadding(new Insets(6));

        Runnable updateNavButtons = () -> {
            int idx = history.getCurrentIndex();
            int size = history.getEntries().size();
            backBtn.setDisable(idx <= 0);
            forwardBtn.setDisable(idx >= size - 1 || size == 0);
        };

        history.currentIndexProperty().addListener((obs, oldV, newV) -> updateNavButtons.run());
        history.getEntries().addListener((javafx.collections.ListChangeListener<WebHistory.Entry>) c -> updateNavButtons.run());

        engine.load(homeUrl);
        updateNavButtons.run();

        BorderPane root = new BorderPane(webView);
        root.setTop(toolbar);

        stage.setTitle("Railway Booker");
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }

    private static WebEngine getWebEngine(WebView webView) {
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        engine.setOnAlert(event -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Railway Booker");
            a.setHeaderText(null);
            a.setContentText(event.getData());
            a.showAndWait();
        });

        engine.setConfirmHandler(message -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Confirmation");
            a.setHeaderText(null);
            a.setContentText(message);

            Optional<ButtonType> res = a.showAndWait();
            return res.isPresent() && res.get() == ButtonType.OK;
        });

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (Objects.requireNonNull(newState) == Worker.State.SUCCEEDED) {
                try {
                    engine.executeScript("document.documentElement.classList.add('is-javafx');");
                } catch (Exception ignored) { }
            }
        });

        return engine;
    }

    private String resolveServerUrl() {
        String fromArgs = getParameters().getNamed().get("serverUrl");

        if (fromArgs != null && !fromArgs.isBlank()) {
            return normalizeUrl(fromArgs);
        }

        Properties props = new Properties();
        try (InputStream is = ClientApp.class.getClassLoader().getResourceAsStream("client.properties")) {
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

        if (!u.endsWith("/")) {
            u += "/";
        }

        return u;
    }

    public static void main(String[] args) {
        System.setProperty("prism.order", "sw");
        launch(args);
    }
}
