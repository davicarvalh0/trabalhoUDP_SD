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

        DatagramSocket socket = new DatagramSocket(minhaPorta);

        for (int i = 0; i < voltas; i++) {
            byte[] buffer = new byte[1000];
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
            socket.receive(pacote);
            String texto = new String(pacote.getData(), 0, pacote.getLength());
            int valor = Integer.parseInt(texto);

            if (operacao == 0) {
                valor = valor + 1;
            } else {
                valor = valor * 2;
            }

            byte[] dados = String.valueOf(valor).getBytes();
            InetAddress host = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(dados, dados.length, host, portaProximo);
            System.out.println("Enviando valor '" + valor + "' para o processo: " + (portaProximo - 6000));
            socket.send(request);
        }
        socket.close();
    }
}