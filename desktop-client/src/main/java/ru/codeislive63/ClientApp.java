package ru.codeislive63;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientApp extends Application {

    private static final String DEFAULT_URL = "http://localhost:8080/routes/search";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);
    private static final long WAIT_DEADLINE_MS = 30_000;
    private static final long RETRY_DELAY_MS = 500;

    private static final String REPORT_PATH = "/admin/dashboard/report.xlsx";

    private final HttpClient http = HttpClient.newBuilder()
            .cookieHandler(CookieHandler.getDefault()) // важно для сессий
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private String homeUrl;

    private final AtomicBoolean waitingNow = new AtomicBoolean(false);

    @Override
    public void start(Stage stage) {
        homeUrl = resolveServerUrl();
        homeUrl = normalizeUrlNoTrailingSlash(homeUrl);

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

        refreshBtn.setOnAction(e -> {
            loadWhenServerReady(engine, homeUrl, true);
        });

        homeBtn.setOnAction(e -> loadWhenServerReady(engine, homeUrl, true));

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

        engine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl == null) return;

            if (newUrl.contains(REPORT_PATH)) {
                Platform.runLater(() -> {
                    engine.getLoadWorker().cancel();
                    if (oldUrl != null && !oldUrl.contains(REPORT_PATH)) {
                        engine.load(oldUrl);
                    } else {
                        engine.load(homeUrl);
                    }
                    downloadXlsx(stage, newUrl);
                });
            }
        });


        engine.loadContent(loadingHtml(homeUrl));

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.FAILED) {
                engine.loadContent(serverDownHtml(homeUrl));
            }
        });

        loadWhenServerReady(engine, homeUrl, false);

        updateNavButtons.run();

        BorderPane root = new BorderPane(webView);
        root.setTop(toolbar);

        stage.setTitle("Railway Booker");
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }

    private void downloadXlsx(Stage stage, String url) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Сохранить отчёт");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        fc.setInitialFileName("analytics-report.xlsx");

        var file = fc.showSaveDialog(stage);
        if (file == null) return;

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .build();

        CompletableFuture.runAsync(() -> {
            try {
                byte[] bytes = http.send(req, HttpResponse.BodyHandlers.ofByteArray()).body();
                Files.write(file.toPath(), bytes);

                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Отчёт сохранён:\n" + file.getAbsolutePath(), ButtonType.OK);
                    a.setHeaderText(null);
                    a.showAndWait();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Не удалось скачать отчёт:\n" + ex.getMessage(), ButtonType.OK);
                    a.setHeaderText(null);
                    a.showAndWait();
                });
            }
        });
    }


    private WebEngine getWebEngine(WebView webView) {
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
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    engine.executeScript("document.documentElement.classList.add('is-javafx');");
                } catch (Exception ignored) { }
            }
        });

        return engine;
    }

    private void loadWhenServerReady(WebEngine engine, String url, boolean force) {
        if (!force && waitingNow.get()) return;
        if (force) waitingNow.set(false);

        if (!waitingNow.compareAndSet(false, true)) return;

        Platform.runLater(() -> engine.loadContent(loadingHtml(url)));

        Thread t = new Thread(() -> {
            long deadline = System.currentTimeMillis() + WAIT_DEADLINE_MS;

            while (System.currentTimeMillis() < deadline) {
                if (isServerUp(url)) {
                    Platform.runLater(() -> engine.load(url));
                    waitingNow.set(false);
                    return;
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ignored) {
                    break;
                }
            }

            waitingNow.set(false);

            Platform.runLater(() -> {
                engine.loadContent(serverDownHtml(url));
                Alert a = new Alert(Alert.AlertType.ERROR,
                        "Сервер не отвечает: " + url + "\n\nЗапусти сервер и нажми Reload.",
                        ButtonType.OK);
                a.setHeaderText("Railway Booker");
                a.showAndWait();
            });
        });

        t.setDaemon(true);
        t.start();
    }

    private boolean isServerUp(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<Void> resp = http.send(req, HttpResponse.BodyHandlers.discarding());
            int code = resp.statusCode();

            return code >= 200 && code < 500;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String resolveServerUrl() {
        String fromArgs = getParameters().getNamed().get("serverUrl");
        if (fromArgs != null && !fromArgs.isBlank()) {
            return fromArgs.trim();
        }

        Properties props = new Properties();

        try (InputStream is = ClientApp.class.getClassLoader().getResourceAsStream("client.properties")) {
            if (is != null) {
                props.load(is);

                String fromProps = props.getProperty("server.url");

                if (fromProps != null && !fromProps.isBlank()) {
                    return fromProps.trim();
                }
            }
        } catch (IOException ignored) { }

        return DEFAULT_URL;
    }

    private String normalizeUrlNoTrailingSlash(String url) {
        String u = url.trim();
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "http://" + u;
        }
        return u;
    }

    private String loadingHtml(String url) {
        return """
        <html>
          <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          </head>
          <body style="font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif; padding: 24px;">
            <h2>Railway Booker</h2>
            <p>Жду пока сервер поднимется…</p>
            <p style="opacity: 0.7;">%s</p>
            <div style="margin-top:16px; width: 260px; height: 10px; background: #e5e7eb; border-radius: 999px; overflow:hidden;">
              <div style="width: 40%%; height: 100%%; background: #3b82f6; border-radius: 999px; animation: move 1s infinite ease-in-out;"></div>
            </div>
            <style>
              @keyframes move { 0%% { transform: translateX(-50%%);} 50%% { transform: translateX(150%%);} 100%% { transform: translateX(-50%%);} }
            </style>
          </body>
        </html>
        """.formatted(escape(url));
    }

    private String serverDownHtml(String url) {
        return """
        <html>
          <head><meta charset="UTF-8" /></head>
          <body style="font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif; padding: 24px;">
            <h2>Сервер недоступен</h2>
            <p>Не удалось подключиться к:</p>
            <p style="opacity: 0.7;">%s</p>
            <p>Запусти сервер и нажми <b>Reload</b>.</p>
          </body>
        </html>
        """.formatted(escape(url));
    }

    private String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static void main(String[] args) {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        launch(args);
    }
}
