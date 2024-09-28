package com.example.JDDB.lib.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class UrlReader {
    private final HttpClient client;
    private final String CDN_SERVER_URL = "https://cdn.discordapp.com";


    public UrlReader(){
        this.client = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        establishConnection();
    }

    private void establishConnection(){
        HttpRequest httpRequest =HttpRequest
                .newBuilder()
                .uri(URI.create(CDN_SERVER_URL))
                .build();

        try {
            client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String fetch(String urlAddress){
        return fetch(urlAddress, 0);
    }

    private String fetch(String urlAddress, int rounds) {
        HttpRequest httpRequest = HttpRequest
                .newBuilder()
                .uri(URI.create(urlAddress))
                .GET()
                .header("Accept-Encoding", "gzip")
                .build();


        HttpResponse<byte[]> response;

        try {
            response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode()==404){
            if (rounds==0){
                throw new RuntimeException("Couldnt execute operation");
            }

            return fetch(urlAddress, rounds-1);
        }


        HttpHeaders headers = response.headers();
        List<String> encoding = headers.allValues("Content-Encoding");

        byte[] responseBody = response.body();
        if (encoding.contains("gzip")){
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(responseBody))) {
                responseBody = gzipInputStream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new String(responseBody, StandardCharsets.UTF_8);
    }
}
