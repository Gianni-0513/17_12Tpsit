package com.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);

        while (true) {
            Socket s = ss.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            // Leggi la prima riga della richiesta HTTP
            String firstLine = in.readLine();
            System.out.println(firstLine);

            // Analizza la richiesta (metodo, url, versione)
            String[] request = firstLine.split(" ");
            String method = request[0];
            String requestedFile = request[1];
            String httpVersion = request[2];

            // Leggi gli header
            String header;
            while ((header = in.readLine()) != null && !header.isEmpty()) {
                System.out.println(header);
            }
            System.out.println("Request complete\n");

            File file = new File("htdocs" + requestedFile);
            // Verifica se il file esiste
            if (file.exists()) {
                out.writeBytes(httpVersion + " 200 OK\n");
                out.writeBytes("Content-Length: " + file.length() + "\n");
                out.writeBytes("Content-Type: " + getContentType(file) + "\n");
                out.writeBytes("\n");

                // Invia il contenuto del file
                try (InputStream input = new FileInputStream(file)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = input.read(buf)) != -1) {
                        out.write(buf, 0, n);
                    }
                }
            } 
            else if (requestedFile.equals("/")) {
                file = new File("htdocs/index.html");  
            }
            else if (!requestedFile.contains(".")) {
                file = new File("htdocs" + requestedFile + "/");
                if (!file.exists()) {
                    out.writeBytes(httpVersion + " 301 Moved Permanently\n");
                    out.writeBytes("Content-Length: 0\n");
                    out.writeBytes("\n");
                }
            }
            else if (!file.exists()) {
                out.writeBytes(httpVersion + " 301 Moved Permanently\n");
                    out.writeBytes("Content-Length: 0\n");
                    out.writeBytes("\n"); 
            }
            else {
                out.writeBytes(httpVersion + " 301 Moved Permanently\n");
                    out.writeBytes("Content-Length: 0\n");
                    out.writeBytes("\n");
            }

            // Chiudi le risorse
            in.close();
            out.close();
            s.close();
        }
    }

    // Restituisce il tipo di contenuto del file
    public static String getContentType(File f) {
        String[] s = f.getName().split("\\.");
        String ext = s[s.length - 1];

        switch (ext) {
            case "html":
            case "htm":
                return "text/html";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "css":
                return "text/css";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "js":
                return "application/javascript";
            case "json":
                return "application/json";
            default:
                return "application/octet-stream";
        }
    }
}
