# Trabalho final - Programação Orientada a Objetos  
## Aluno: Mauro Gutemberg Magalhães Barros  
## Análise e Desenvolvimento de sistemas; Módulo 2  

## Funcionalidades

### Menus
- Tela inicial com nome do jogo.  
- Seleção entre **Tutorial** e **Iniciar**.  

### Mecânicas Básicas
- **Iniciar** → leva o jogador à primeira fase.  
- Cada **rodada** = 3 apostas.  
- O jogador vence a partir da **primeira rodada vencida**.  
- O jogador pode escolher **sair** e ficar com o dinheiro ou seguir para o **modo infinito**.  
- **Modo infinito**:  
  - O jogador só pode sair após **2 rodadas vencidas** em diante.  
  - Sempre possível sair apenas **na primeira aposta** da rodada.  

### Mecânicas de Gameplay
- Cada rodada entrega **52 cartas novas**.  
- Dentro da rodada:  
  - São feitas **3 apostas**.  
  - Cada aposta = **nova mão de pôquer** aleatória.  
  - O jogador joga com uma **mão de 10 cartas**.  
  - O jogador joga uma **mão de até 5 cartas**.  
- Cada mão de pôquer soma ao **multiplicador de dinheiro** do jogador.  
  - O multiplicador é aplicado ao dinheiro **ao final da 3ª aposta**.  
- O jogador inciará cada aposta com 5 descartes, podendo descartar até 5 cartas.  
  - O jogo poderá acabar caso o deck de 52 cartas se esvazie durante uma rodada.  
- Para avançar, o jogador **não pode** encerrar um round com o multiplicador ≤ **1.5x**.  
- O jogador pode escolher **rodar um dado de 6 lados**:  
  - O número sorteado = quantidade de balas do revólver (**b/6**).  
  - O revólver pode **aumentar** o multiplicador de acordo com a probabilidade.  
- Fórmula do revólver:  
  - `MULT * ((n + 1) ^ 2)`  
  - Onde **n** = número de balas no tambor.  

---

## Pontuação por Mão de Pôquer
Cada mão de pôquer adiciona um multiplicador ao saldo do jogador:

- **High Card** → +0.1x  
- **Dupla** → +0.4x  
- **Dois Pares** → +0.8x  
- **Trinca** → +1.5x  
- **Sequência** → +3.0x  
- **Flush** → +6.0x  
- **Full House** → +10.0x  
- **Quadra** → +25.0x  
- **Straight Flush** → +50.0x  
- **Royal Flush** → +100.0x  

---

## Estrutura das Rodadas

### Round 1
- Blind 1: Hand == **Par**  
- Blind 2: Hand <= **Par** (random)  
- Blind 3: Hand <= **Par** (random)  

### Round 2
- Blind 1: Hand == **Dupla**  
- Blind 2: Hand <= **Dupla** (random)  
- Blind 3: Hand <= **Dupla** (random)  

### Round 3
- Blind 1: Hand == **Dois Pares**  
- Blind 2: Hand <= **Dois Pares** (random)  
- Blind 3: Hand <= **Dois Pares** (random)  

### Round 4
- Blind 1: Hand == **Trinca**  
- Blind 2: Hand <= **Trinca** (random)  
- Blind 3: Hand <= **Trinca** (random)  

### Round 5
- Blind 1: Hand == **Flush**  
- Blind 2: Hand <= **Flush** (random)  
- Blind 3: Hand <= **Flush** (random)  

### Round 6
- Blind 1: Hand == **Full House**  
- Blind 2: Hand <= **Full House** (random)  
- Blind 3: Hand <= **Full House** (random)  