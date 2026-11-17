# Guia de Configuração de IP Fixo no Ubuntu

## 1. Objetivo

Este guia apresenta os procedimentos necessários para configurar endereços IP fixos em máquinas com Ubuntu, permitindo que diferentes computadores se comuniquem dentro da mesma rede local.

A configuração será realizada utilizando o **NetworkManager (nmcli)**, sem necessidade de alterar arquivos de configuração do sistema.

O procedimento pode ser revertido posteriormente, retornando a configuração automática de IP (DHCP).

## 2. Informações da rede

Antes de iniciar, é necessário identificar as informações da rede à qual a máquina está conectada.

Neste exemplo, utilizaremos os seguintes dados:

| Informação | Valor |
|---|---|
| Conexão Wi-Fi | Davi 5G |
| Interface de rede | wlp1s0 |
| Gateway | 192.168.1.254 |
| Máscara de rede | 255.255.255.0 |
| Faixa da rede | 192.168.1.0/24 |
| IP da máquina | 192.168.1.100 |

**Observação:** os endereços apresentados são exemplos baseados na rede utilizada durante a configuração. Cada máquina deve utilizar um endereço IP exclusivo.

## 3. Identificando as informações da rede

Abra o terminal e execute:

### 3.1. Identificar as conexões disponíveis

```bash
nmcli connection show
```

O comando apresenta as conexões de rede configuradas no computador.

Exemplo:

```text
NAME       UUID                                  TYPE  DEVICE
Davi 5G    80ca9c2f-ecfd-4458-bb99-dc6108ffb553  wifi  wlp1s0
```

### 3.2. Identificar o gateway e a faixa de IP

```bash
ip route
```

Exemplo:

```text
default via 192.168.1.254 dev wlp1s0
192.168.1.0/24 dev wlp1s0
```

### 3.3. Identificar o IP atual

```bash
ip -4 addr show wlp1s0
```

Com essas informações, é possível identificar a configuração atual da rede.

## 4. Definindo os endereços IP

Cada computador deverá receber um endereço IP diferente, mantendo a mesma faixa de rede.

Exemplo de distribuição:

| Máquina | Endereço IP |
|---|---|
| Máquina 1 | 192.168.1.100 |
| Máquina 2 | 192.168.1.101 |
| Máquina 3 | 192.168.1.102 |
| Máquina 4 | 192.168.1.103 |
| Máquina 5 | 192.168.1.104 |

**Importante:** antes de utilizar os endereços, verifique a faixa DHCP do roteador e escolha endereços reservados ou fora dessa faixa. Um teste de `ping` sem resposta não garante que o IP esteja livre.

## 5. Configurando o IP fixo

A configuração será realizada através do comando `nmcli`.

### 5.1. Configurar o endereço IP

Execute o comando abaixo, substituindo o endereço IP pelo correspondente à máquina:

```bash
sudo nmcli connection modify "Davi 5G" \
ipv4.method manual \
ipv4.addresses 192.168.1.100/24 \
ipv4.gateway 192.168.1.254 \
ipv4.dns "1.1.1.1 8.8.8.8"
```

Descrição dos parâmetros:

- `ipv4.method manual`: define a configuração manual do IPv4.
- `ipv4.addresses`: determina o endereço IP e a máscara de rede.
- `ipv4.gateway`: define o gateway utilizado para acessar outras redes.
- `ipv4.dns`: configura os servidores DNS.

### 5.2. Aplicar as configurações

Após configurar o endereço, reinicie a conexão:

```bash
sudo nmcli connection down "Davi 5G"
sudo nmcli connection up "Davi 5G"
```

A conexão Wi-Fi poderá ficar indisponível por alguns instantes durante o procedimento.

### 5.3. Verificar o endereço configurado

Execute:

```bash
ip -4 addr show wlp1s0
```

O resultado esperado será semelhante a:

```text
inet 192.168.1.100/24
```

## 6. Testando a comunicação entre as máquinas

Depois de configurar todas as máquinas, é necessário verificar se elas conseguem se comunicar pela rede.

### 6.1. Testar a comunicação com outra máquina

Na primeira máquina, execute:

```bash
ping 192.168.1.101
```

Se a comunicação estiver funcionando, serão recebidas respostas semelhantes a:

```text
64 bytes from 192.168.1.101: icmp_seq=1 ttl=64 time=2.15 ms
```

### 6.2. Testar o gateway

```bash
ping 192.168.1.254
```

Esse teste verifica a comunicação com o roteador.

### 6.3. Verificar a configuração da rota

```bash
ip route
```

O endereço IP configurado deverá aparecer associado à interface de rede.

## 7. Restaurando a configuração automática (DHCP)

Caso seja necessário retornar à configuração original, execute:

```bash
sudo nmcli connection modify "Davi 5G" ipv4.method auto
```

Em seguida, reative a conexão:

```bash
sudo nmcli connection up "Davi 5G"
```

O computador voltará a receber automaticamente um endereço IP disponibilizado pelo servidor DHCP da rede.

## 8. Resumo dos comandos

| Finalidade | Comando |
|---|---|
| Listar conexões | `nmcli connection show` |
| Consultar rotas | `ip route` |
| Consultar IP | `ip -4 addr show wlp1s0` |
| Configurar IP fixo | `nmcli connection modify` |
| Reiniciar conexão | `nmcli connection down/up` |
| Testar comunicação | `ping IP_DESTINO` |
| Restaurar DHCP | `nmcli connection modify "Davi 5G" ipv4.method auto` |

## 9. Considerações finais

A utilização de endereços IP fixos facilita a identificação dos computadores e permite estabelecer uma comunicação previsível entre os dispositivos conectados à mesma rede local.

Essa configuração é especialmente útil em ambientes que utilizam múltiplas máquinas, como laboratórios de informática, sistemas distribuídos e clusters computacionais.

Ao finalizar o trabalho, recomenda-se restaurar a configuração DHCP caso os computadores voltem a utilizar a rede em condições normais.
