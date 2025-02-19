package com.simuladorDispositivos.simuladordispositivo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Controller
public class HomeController {

    private String ip;
    private int port;
    private Socket clientSocket;
    private ServerSocket serverSocket ;

    @GetMapping("/")
    public String showConfigPage(Model model) {
        try {
            closeSockets();
            ip = java.net.InetAddress.getLocalHost().getHostAddress();
            model.addAttribute("ip", ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "index";
    }

    @PostMapping("/start-server")
    public String startServer(@RequestParam("port") int port, @RequestParam("device") String device, Model model) {
        this.port = port;
        String deviceDescription = (device.equals("1")) ? "Determinador de umidade" : "Balança de Classificação";
        model.addAttribute("device", deviceDescription);
        model.addAttribute("message", "Servidor iniciado " + ip + ":" + port);

        new Thread(() -> startSocketServer(port, model)).start();

        return "sendData";
    }

    public void startSocketServer(int port, Model model) {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                System.out.println("Servidor já está rodando!");
                return;
            }

            serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado em " + ip + ":" + port);

            while (!serverSocket.isClosed()) {
                clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().toString();
                System.out.println("Conexão recebida de " + clientSocket.getInetAddress());
                model.addAttribute("clientMessage", "Conexão recebida de " + clientAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message", "Erro ao iniciar o servidor.");
        }
    }

    @PostMapping("/send-data")
    public String sendData(@RequestParam String data,
                           @RequestParam String device,
                           @RequestParam(required = false) String sojaMilho,
                           Model model) {
        // Exibe as mensagens iniciais
        model.addAttribute("message", "Servidor iniciado " + ip + ":" + port);
        model.addAttribute("messageSucesso", "Dados enviados com sucesso!");
        model.addAttribute("device", device);

        System.out.println("Dados: " + data);

        String json = data;
        if (device.equals("Determinador de umidade")) {
            if (sojaMilho != null && !sojaMilho.isEmpty()) {
                json = "{";
                json += "\"umidade\": \"" + data + "\",";
                json += "\"curva\": \"" + sojaMilho + "\"";
                json += "}";
            }

        }
        System.out.println("JSON: " + json);
        try {
            sendDataToSocket(json);
        } catch (Exception e) {
            model.addAttribute("messageSucesso", "Erro ao enviar dados:"+ e.getMessage());
            e.printStackTrace();
        }

        return "sendData";
    }

    private void sendDataToSocket(String data) throws IOException {
        try (OutputStream output = clientSocket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {
            while (true) {
                writer.println(data);
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Cliente desconectado");
        }
    }

    private void closeSockets() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Servidor fechado!");
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("Cliente desconectado.");
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }
}
