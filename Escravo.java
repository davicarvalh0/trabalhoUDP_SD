import java.net.*;
import java.io.*;

public class Escravo {
    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Uso: java Escravo <id> <operacao> <voltas> <ip do proximo>");
            System.out.println("Exemplo: java Escravo 1 0 1 192.168.1.121");
            return;
        }

        int id, operacao, voltas;
        try {
            id = Integer.parseInt(args[0]);
            operacao = Integer.parseInt(args[1]);
            voltas = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("ERRO: id, operacao e voltas precisam ser numeros inteiros.");
            return;
        }

        if (id < 1 || id > 3) {
            System.out.println("ERRO: o id do escravo tem que ser 1, 2 ou 3.");
            return;
        }
        if (operacao != 0 && operacao != 1) {
            System.out.println("ERRO: a operacao tem que ser 0 (soma) ou 1 (multiplica).");
            return;
        }
        if (voltas < 1) {
            System.out.println("ERRO: o numero de voltas tem que ser 1 ou mais.");
            return;
        }

        int minhaPorta = 6000 + id;
        int portaProximo = minhaPorta + 1;
        if (portaProximo == 6004) {
            portaProximo = 6000;
        }

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(minhaPorta);
            InetAddress host = InetAddress.getByName(args[3]);

            // Mostra a configuração pra conferir se está tudo certo
            System.out.println("Escravo " + id + " escutando na porta " + minhaPorta);
            System.out.println("Vai enviar para " + host.getHostAddress() + ":" + portaProximo);
            System.out.println("Esperando o token...");

            for (int i = 0; i < voltas; i++) {
                byte[] buffer = new byte[1000];
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacote);
                String texto = new String(pacote.getData(), 0, pacote.getLength());
                int token = Integer.parseInt(texto);

                System.out.println("[volta " + (i + 1) + "] Recebeu " + token + " de "
                        + pacote.getAddress().getHostAddress() + ":" + pacote.getPort());

                if (operacao == 0) {
                    token = token + 1;
                } else {
                    token = token * 2;
                }

                byte[] msg = String.valueOf(token).getBytes();
                DatagramPacket request = new DatagramPacket(msg, msg.length, host, portaProximo);
                socket.send(request);
                System.out.println("[volta " + (i + 1) + "] Enviou " + token + " para "
                        + host.getHostAddress() + ":" + portaProximo);
            }
            System.out.println("Escravo " + id + " terminou.");

        } catch (BindException e) {
            System.out.println("ERRO: a porta " + minhaPorta + " ja esta em uso.");
            System.out.println("Tem outro Escravo " + id + " rodando? Feche-o (Ctrl+C) ou use: ss -ulpn | grep 600");
        } catch (UnknownHostException e) {
            System.out.println("ERRO: IP invalido ou nao encontrado: " + args[3]);
        } catch (NumberFormatException e) {
            System.out.println("ERRO: chegou um pacote que nao e um numero.");
        } catch (IOException e) {
            System.out.println("ERRO de rede: " + e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}