import java.net.*;
import java.io.*;

public class Mestre {
    public static void main(String[] args) {

        // ===== VALIDAÇÃO DOS PARÂMETROS =====
        if (args.length < 4) {
            System.out.println("Uso: java Mestre 0 <operacao> <voltas> <ip do escravo 1>");
            System.out.println("Exemplo: java Mestre 0 0 1 192.168.1.101");
            return;
        }

        int operacao, voltas;
        try {
            operacao = Integer.parseInt(args[1]);
            voltas = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("ERRO: operacao e voltas precisam ser numeros inteiros.");
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

        int token = 2;
        int minhaPorta = 6000;
        int portaProximo = 6001;

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(minhaPorta);
            InetAddress host = InetAddress.getByName(args[3]);

            // Se o token não voltar em 10 segundos, o receive() desiste
            // e lança SocketTimeoutException, em vez de travar pra sempre.
            socket.setSoTimeout(10000);

            System.out.println("Mestre escutando na porta " + minhaPorta);
            System.out.println("Vai enviar para " + host.getHostAddress() + ":" + portaProximo);

            for (int i = 0; i < voltas; i++) {
                byte[] dados = String.valueOf(token).getBytes();
                DatagramPacket request = new DatagramPacket(dados, dados.length, host, portaProximo);
                socket.send(request);
                System.out.println("[volta " + (i + 1) + "] Enviou " + token + " para "
                        + host.getHostAddress() + ":" + portaProximo);

                byte[] buffer = new byte[1000];
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacote);
                String msg = new String(pacote.getData(), 0, pacote.getLength());
                token = Integer.parseInt(msg);
                System.out.println("[volta " + (i + 1) + "] Recebeu " + token + " de "
                        + pacote.getAddress().getHostAddress() + ":" + pacote.getPort());

                if (operacao == 0) {
                    token = token + 1;
                } else {
                    token = token * 2;
                }
            }
            System.out.println("Resultado = " + token);

        } catch (SocketTimeoutException e) {
            System.out.println("ERRO: o token nao voltou em 10 segundos.");
            System.out.println("Veja nos terminais dos escravos qual foi o ultimo que imprimiu 'Enviou'.");
            System.out.println("O problema esta entre esse escravo e o proximo (IP, firewall ou processo parado).");
        } catch (BindException e) {
            System.out.println("ERRO: a porta " + minhaPorta + " ja esta em uso. Tem outro Mestre rodando?");
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