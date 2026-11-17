import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

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

        InetAddress host = InetAddress.getByName(args[3]);

        for (int i = 0; i < voltas; i++) {
            byte[] buffer = new byte[1000];
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
            socket.receive(pacote);
            if (pacote.getLength() != Integer.BYTES) {
                throw new IllegalArgumentException("Token recebido com tamanho inválido");
            }
            int token = ByteBuffer.wrap(pacote.getData(), pacote.getOffset(), pacote.getLength()).getInt();

            if (operacao == 0) {
                token = token + 1;
            } else {
                token = token * 2;
            }

            byte[] msg = ByteBuffer.allocate(Integer.BYTES).putInt(token).array();
            DatagramPacket request = new DatagramPacket(msg, msg.length, host, portaProximo);
            System.out.println("Enviando token '" + token + "' para o processo: " + (portaProximo - 6000));
            socket.send(request);
        }
        socket.close();
    }
}