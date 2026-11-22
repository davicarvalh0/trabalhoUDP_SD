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

## Preparação da rede

Todas as máquinas precisam estar conectadas na mesma rede.

### Descobrir o IP de cada máquina

Em cada máquina, rode:

```bash
hostname -I
```

Se aparecer mais de um endereço, use o que estiver na mesma faixa das outras máquinas (por exemplo, `192.168.1.x`). O roteador pode trocar o IP de uma máquina de um dia para o outro, então confira antes de cada execução.

### Testar a comunicação

De cada máquina, faça `ping` para as outras:

```bash
ping <ip de outra maquina>
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

Faça isso só em uma rede confiável e religue o firewall no final (seção "Depois do trabalho").

## Compilação

Em cada máquina, na pasta com os arquivos `.java`:

```bash
javac Escravo.java Mestre.java
```

## Execução em quatro máquinas

Os escravos devem ser iniciados antes do mestre. Com UDP, se o mestre enviar o token antes de o Escravo 1 estar escutando, o pacote se perde sem gerar erro.

Exemplo com todos somando e uma volta, supondo estes IPs:

| Processo | IP da máquina | Comando |
|---|---|---|
| Escravo 1 | 192.168.1.101 | `java Escravo 1 0 1 192.168.1.102` |
| Escravo 2 | 192.168.1.102 | `java Escravo 2 0 1 192.168.1.103` |
| Escravo 3 | 192.168.1.103 | `java Escravo 3 0 1 192.168.1.100` |
| Mestre | 192.168.1.100 | `java Mestre 0 0 1 192.168.1.101` |

Troque os IPs pelos que apareceram no `hostname -I` de cada máquina.

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
| `IP invalido ou nao encontrado` | O quarto parâmetro não é um IP válido | Conferir o IP com `hostname -I` na máquina de destino |

Se o mestre der timeout, os escravos continuam esperando o token. Encerre todos com Ctrl+C antes de rodar de novo.

Para testar o UDP sem o programa Java entre duas máquinas:

```bash
# máquina que recebe
nc -ul 6001

# máquina que envia
echo 5 | nc -u <ip da maquina que recebe> 6001
```

Se o 5 não aparecer na máquina que recebe, o problema é de rede ou firewall.

## Depois do trabalho

Religue o firewall em cada máquina:

```bash
sudo ufw enable
```
