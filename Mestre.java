import java.net.*;
import java.io.*;

public class Mestre {

    public static void main(String[] args) throws Exception {

        int operacao = Integer.parseInt(args[1]);
        int voltas = Integer.parseInt(args[2]);

        int token = 2;

        int minhaPorta = 6000;
        int portaProximo = 6000 + 1;

        DatagramSocket socket = new DatagramSocket(minhaPorta);

        for (int i = 0; i < voltas; i++){

            byte[] dados = String.valueOf(token).getBytes();
            InetAddress host = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(dados, dados.length, host, portaProximo);
            System.out.println("Enviando o valor '" + token + "' para o processo: " + (portaProximo - 6000));
            socket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
            socket.receive(pacote);
            String msg = new String(pacote.getData(), 0, pacote.getLength());
            token = Integer.parseInt(msg);

            if (operacao == 0) {
                token = token + 1;
            } else {
                token = token * 2;
            }
        }
        System.out.println("Resultado = " + token);
        socket.close();
    }
}
