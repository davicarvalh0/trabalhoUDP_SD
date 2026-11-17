import java.net.*;
import java.io.*;

public class Escravo {
    public static void main(String[] args) throws Exception {

        int id = Integer.parseInt(args[0]);
        int operacao = Integer.parseInt(args[1]);
        int voltas = Integer.parseInt(args[2]);

        int minhaPorta = 6000 + id;
        int portaProximo = minhaPorta + 1;
        if (portaProximo == 6004) {
            portaProximo = 6000;
        }

        String[] ips = {
                "192.168.1.100",  // 0 → Mestre
                "192.168.1.101",  // 1 → Escravo 1
                "192.168.1.121",  // 2 → Escravo 2
                "192.168.1.85"   // 3 → Escravo 3
        };

        DatagramSocket socket = new DatagramSocket(minhaPorta);

        InetAddress host = InetAddress.getByName(ips[portaProximo - 6000]);

        for (int i = 0; i < voltas; i++) {
            byte[] buffer = new byte[1000];
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
            socket.receive(pacote);
            String texto = new String(pacote.getData(), 0, pacote.getLength());
            int token = Integer.parseInt(texto);

            if (operacao == 0) {
                token = token + 1;
            } else {
                token = token * 2;
            }

            byte[] msg = String.valueOf(token).getBytes();
            DatagramPacket request = new DatagramPacket(msg, msg.length, host, portaProximo);
            System.out.println("Enviando token '" + token + "' para o processo: " + (portaProximo - 6000));
            socket.send(request);
        }
        socket.close();
    }
}