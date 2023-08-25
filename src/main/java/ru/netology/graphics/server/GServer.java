package ru.netology.graphics.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.netology.graphics.image.TextGraphicsConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class GServer {
    public static final int PORT = 8888;

    private final HttpServer server;
    private final TextGraphicsConverter converter;

    public GServer(TextGraphicsConverter converter) throws Exception {
        if (converter == null) {
            throw new IllegalArgumentException("Серверу нужно передать в конструктор объект-конвертер, а было передано null.");
        }
        this.converter = converter;
        this.converter.setMaxHeight(300);
        this.converter.setMaxWidth(300);
        this.converter.setMaxRatio(4);

        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/", this::serverHtml);
        server.createContext("/convert", this::serverConvertion);
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Открой в браузере http://localhost:8888/");
        server.start();
    }

    protected void serverHtml(HttpExchange h) throws IOException {
        System.out.println("Serving html..");
        var htmlPath = Path.of("assets/index.html");
        var htmlContent = Files.readString(htmlPath);
        var jsPath = Path.of("assets/my.js");
        var jsContent = Files.readString(jsPath);
        htmlContent = htmlContent.replace("{{{JS}}}", jsContent);
        var htmlBytes = htmlContent.getBytes();
        h.sendResponseHeaders(200, htmlBytes.length);
        h.getResponseBody().write(htmlBytes);
        h.close();
    }

    protected void serverConvertion(HttpExchange h) throws IOException {
        System.out.println("Convert request..");
        var url = new BufferedReader(new InputStreamReader(h.getRequestBody())).readLine();
        try {
            System.out.println("Converting image: " + url);
            var img = converter.convert(url).getBytes();
            Files.write(Path.of("assets/img.txt"), img);
            System.out.println("...converted!");
            h.sendResponseHeaders(200, img.length);
            h.getResponseBody().write(img);
        } catch (Exception e) {
            e.printStackTrace();
            var msg = e.getMessage();
            if (msg.isEmpty()) {
                msg = "Произошла ошибка конвертации :'(";
            }
            var msgBytes = msg.getBytes();
            h.sendResponseHeaders(500, msgBytes.length);
            h.getResponseBody().write(msgBytes);
        } finally {
            h.close();
        }
    }
}
