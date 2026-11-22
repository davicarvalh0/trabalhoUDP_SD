# Token Ring com UDP

Trabalho da disciplina de Sistemas Distribuídos. O programa simula uma topologia token ring com um processo mestre e três escravos. O token é um número inteiro que começa em 2 e passa de processo em processo no sentido horário, partindo do mestre:

```text
Mestre (0) → Escravo 1 → Escravo 2 → Escravo 3 → Mestre (0)
```

A comunicação entre os processos usa UDP (`DatagramSocket` e `DatagramPacket`). Cada processo pode rodar em uma máquina diferente da mesma rede local.

## Arquivos

| Arquivo | Função |
|---|---|
| `Mestre.java` | Cria o token com valor 2, envia para o Escravo 1, recebe do Escravo 3 e mostra o resultado final |
| `Escravo.java` | Recebe o token do processo anterior, aplica sua operação e envia para o próximo |

## Funcionamento

Cada processo escuta em uma porta UDP calculada a partir do seu id (6000 + id) e envia para a porta do próximo processo do anel:

| Processo | Escuta na porta | Envia para a porta |
|---|---|---|
| Mestre | 6000 | 6001 (Escravo 1) |
| Escravo 1 | 6001 | 6002 (Escravo 2) |
| Escravo 2 | 6002 | 6003 (Escravo 3) |
| Escravo 3 | 6003 | 6000 (Mestre) |

A cada passagem, o processo aplica a operação recebida por parâmetro: 0 soma 1 ao token e 1 multiplica o token por 2. O mestre não altera o token antes de enviar; ele aplica a sua operação quando o token volta. Isso se repete pelo número de voltas informado.

## Parâmetros

Os quatro parâmetros são obrigatórios:

```bash
java Escravo <id> <operacao> <voltas> <ip do proximo>
java Mestre 0 <operacao> <voltas> <ip do escravo 1>
```

| Parâmetro | Valores |
|---|---|
| `id` | 0 para o mestre; 1, 2 ou 3 para os escravos |
| `operacao` | 0 = soma 1, 1 = multiplica por 2 |
| `voltas` | 1 ou mais |
| `ip do proximo` | IP da máquina onde roda o próximo processo do anel |

O IP informado é sempre o da máquina seguinte, nunca o da própria máquina. Para rodar tudo em um computador só, use `localhost` como quarto parâmetro em todos os processos.

Se algum parâmetro faltar ou tiver valor inválido, o programa mostra uma mensagem de uso e encerra.

## Configuração de IP fixo no Ubuntu

Com IP fixo, cada máquina mantém o mesmo endereço entre um teste e outro e os comandos de execução não precisam mudar. A configuração é feita com o NetworkManager (`nmcli`) e pode ser desfeita no final (seção "Voltando para DHCP").

### Identificar a rede

Liste as conexões:

```bash
nmcli connection show
```

```text
NAME       UUID                                  TYPE  DEVICE
Davi 5G    80ca9c2f-ecfd-4458-bb99-dc6108ffb553  wifi  wlp1s0
```

Veja o gateway e a faixa da rede:

```bash
ip route
```

```text
default via 192.168.1.254 dev wlp1s0
192.168.1.0/24 dev wlp1s0
```

E o IP atual:

```bash
ip -4 addr show wlp1s0
```

Os exemplos deste guia usam a conexão `Davi 5G`, a interface `wlp1s0`, o gateway `192.168.1.254` e a rede `192.168.1.0/24`. Troque pelos valores da sua rede.

### Distribuir os endereços

Cada máquina recebe um IP diferente dentro da mesma faixa:

| Máquina | Processo | IP |
|---|---|---|
| 1 | Mestre | 192.168.1.100 |
| 2 | Escravo 1 | 192.168.1.101 |
| 3 | Escravo 2 | 192.168.1.102 |
| 4 | Escravo 3 | 192.168.1.103 |

Antes de usar esses endereços, confira a faixa DHCP do roteador e escolha IPs fora dela. Um `ping` sem resposta não garante que o IP esteja livre.

### Aplicar o IP fixo

Em cada máquina, troque o endereço pelo IP correspondente:

```bash
sudo nmcli connection modify "Davi 5G" \
ipv4.method manual \
ipv4.addresses 192.168.1.100/24 \
ipv4.gateway 192.168.1.254 \
ipv4.dns "1.1.1.1 8.8.8.8"
```

- `ipv4.method manual`: desativa o DHCP e usa a configuração manual.
- `ipv4.addresses`: IP da máquina e máscara (`/24` = 255.255.255.0).
- `ipv4.gateway`: endereço do roteador.
- `ipv4.dns`: servidores DNS.

Reinicie a conexão para aplicar (o Wi-Fi cai por alguns segundos):

```bash
sudo nmcli connection down "Davi 5G"
sudo nmcli connection up "Davi 5G"
```

Confira o resultado:

```bash
ip -4 addr show wlp1s0
```

```text
inet 192.168.1.100/24
```

### Testar a comunicação

De cada máquina, faça `ping` para as outras e para o gateway:

```bash
ping 192.168.1.101
ping 192.168.1.254
```

### Desativar o firewall

O `ping` passar não garante que o UDP passe, porque o firewall pode bloquear as portas do programa. Para evitar esse problema durante o trabalho, desative o firewall em cada máquina:

```bash
sudo ufw disable
```

Confira se ficou desativado:

```bash
sudo ufw status
```

```text
Status: inactive
```

Faça isso só em uma rede confiável e religue o firewall no final (seção "Voltando para DHCP").

## Compilação

Em cada máquina, na pasta com os arquivos `.java`:

```bash
javac Escravo.java Mestre.java
```

## Execução em quatro máquinas

Os escravos devem ser iniciados antes do mestre. Com UDP, se o mestre enviar o token antes de o Escravo 1 estar escutando, o pacote se perde sem gerar erro.

Exemplo com todos somando e uma volta:

| Máquina | Comando |
|---|---|
| 192.168.1.101 | `java Escravo 1 0 1 192.168.1.102` |
| 192.168.1.102 | `java Escravo 2 0 1 192.168.1.103` |
| 192.168.1.103 | `java Escravo 3 0 1 192.168.1.100` |
| 192.168.1.100 | `java Mestre 0 0 1 192.168.1.101` |

Cada processo mostra a porta em que escuta, o destino dos envios e, a cada volta, de onde recebeu e para onde enviou o token. O mestre termina mostrando `Resultado = 6`.

## Resultados esperados

Com o mesmo valor de operação e de voltas em todos os processos:

| Operação | Voltas | Resultado |
|---|---|---|
| 0 (soma) | 1 | 6 |
| 1 (multiplica) | 1 | 32 |
| 0 (soma) | 2 | 10 |
| 1 (multiplica) | 2 | 512 |

## Erros comuns

| Mensagem | Causa | Solução |
|---|---|---|
| `a porta 600X ja esta em uso` | Um processo de um teste anterior ainda está rodando | Encerrar com Ctrl+C. Para achar o processo: `ss -ulpn \| grep 600` e depois `kill <pid>` |
| `o token nao voltou em 10 segundos` | O token parou em algum ponto do anel | Ver qual foi o último processo que mostrou "Enviou". O problema está entre ele e o próximo: IP errado, processo não iniciado ou firewall ainda ativo |
| `IP invalido ou nao encontrado` | O quarto parâmetro não é um IP válido | Conferir o IP com `ip -4 addr show` na máquina de destino |

Se o mestre der timeout, os escravos continuam esperando o token. Encerre todos com Ctrl+C antes de rodar de novo.

Para testar o UDP sem o programa Java entre duas máquinas:

```bash
# máquina que recebe
nc -ul 6001

# máquina que envia
echo 5 | nc -u 192.168.1.101 6001
```

Se o 5 não aparecer na máquina que recebe, o problema é de rede ou firewall.

## Voltando para DHCP

Depois do trabalho, para a máquina voltar a receber IP automático:

```bash
sudo nmcli connection modify "Davi 5G" ipv4.method auto
sudo nmcli connection up "Davi 5G"
```

Para religar o firewall:

```bash
sudo ufw enable
```
