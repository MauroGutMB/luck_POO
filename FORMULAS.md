# Fórmulas Matemáticas do Jogo

## 1. Cálculo de Dinheiro Projetado
$$
\text{Dinheiro Projetado} = \text{Dinheiro Atual} \times \text{Multiplicador Total}
$$

## 2. Penalidade por Mão Inferior
$$
\text{Penalidade} = 0.2 \times \text{Rodada Atual}
$$

## 3. Atualização do Multiplicador (Sucesso)
$$
\text{Multiplicador}_{\text{novo}} = \text{Multiplicador}_{\text{atual}} + \text{Multiplicador da Mão}
$$

## 4. Atualização do Multiplicador (Falha)
$$
\text{Multiplicador}_{\text{novo}} = \max(0, \text{Multiplicador}_{\text{atual}} - \text{Penalidade})
$$

## 5. Multiplicadores das Mãos de Poker

| Mão | Multiplicador |
|-----|---------------|
| High Card | $0.1$ |
| Par | $0.4$ |
| Dois Pares | $0.6$ |
| Trinca | $1.0$ |
| Sequência | $1.5$ |
| Flush | $2.0$ |
| Full House | $5.0$ |
| Quadra | $10.0$ |
| Straight Flush | $50.0$ |
| Royal Flush | $100.0$ |

## 6. Metas de Dinheiro por Rodada

| Rodada | Meta ($) | Mão Mínima Exigida |
|--------|----------|--------------------|
| 1 | $20$ | Par |
| 2 | $50$ | Dois Pares OU Par (aleatório) |
| 3 | $150$ | Trinca, D.P, P (Aleatório) |
| 4 | $500$ | Flush, Trinca, D.P, P. (Aleatório) |
| 5 | $2000$ | Full House, Flush, Trinca, D.P, P (Aleatório) |
| 6 | $10000$ | Quadra, Full House, Flush, Trinca, D.P, P (Aleatório) |
| 7+ | $50000$ | Royal Flush Quadra, Full House, Flush, Trinca (Aleatório)|

## 7. Notação Científica
Para valores $\geq 1\,000\,000$:
$$
\text{Valor Formatado} = a.bc \times 10^n
$$
onde $1 \leq a < 10$ e $n$ é o expoente.

## 8. Condições de Vitória/Derrota

### Vitória da Blind
$$
\text{Dinheiro Projetado} \geq \text{Meta da Rodada}
$$

### Game Over
$$
\text{Mãos Jogadas} = 3 \land \text{Dinheiro Projetado} < \text{Meta da Rodada}
$$
ou
$$
\text{Deck Vazio} = \text{true}
$$
ou
$$
\text{Dinheiro ao final do round} < \text{Dinheiro necessário}
$$


## 9. Valores Iniciais

- **Dinheiro Inicial**: $10$
- **Multiplicador Inicial**: $1.0$
- **Máximo de Mãos por Blind**: $3$
- **Descartes Iniciais**: $5$
- **Tamanho da Mão**: $8$ cartas
