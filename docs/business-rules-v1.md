# Order Management Benchmark API — Regras de Negócio v1

Este documento define a fonte oficial das regras de negócio do benchmark V1.
Os vínculos com operações OpenAPI e casos de teste são mantidos separadamente no arquivo `traceability-ground-truth-v1.csv`.

## Convenções

- **ID**: identificador estável da regra de negócio.
- **Descrição**: comportamento de domínio que deve ser respeitado.
- **Condições**: pré-condições ou restrições necessárias para a regra.
- **Resultado esperado**: comportamento esperado quando a regra é satisfeita.
- **Violação**: comportamento esperado quando a regra não é satisfeita.

## RN-001 — Cliente ativo para criação de pedido

**Descrição:** Um pedido somente pode ser criado para um cliente com status ACTIVE.

**Condições:**
- O cliente informado deve existir.
- O status do cliente deve ser ACTIVE no momento da criação do pedido.

**Resultado esperado:** O pedido é criado com status DRAFT.

**Violação:** A criação é rejeitada quando o cliente estiver BLOCKED.

## RN-002 — Somente produtos ativos podem ser adicionados

**Descrição:** Produtos com status INACTIVE não podem ser adicionados a um pedido.

**Condições:**
- O produto informado deve existir.
- O status do produto deve ser ACTIVE no momento da inclusão.

**Resultado esperado:** O item pode ser adicionado ao pedido.

**Violação:** A inclusão é rejeitada quando o produto estiver INACTIVE.

## RN-003 — Quantidade do item deve ser positiva

**Descrição:** A quantidade de um produto adicionada ou atualizada em um pedido deve ser maior que zero.

**Condições:**
- A quantidade informada deve ser um inteiro positivo.

**Resultado esperado:** A quantidade do item é aceita quando for maior que zero.

**Violação:** A operação é rejeitada quando a quantidade for zero ou negativa.

## RN-004 — Quantidade não pode superar o estoque disponível

**Descrição:** A quantidade de um item do pedido não pode ser superior ao estoque disponível do produto.

**Condições:**
- O produto deve possuir estoque suficiente para a quantidade solicitada.

**Resultado esperado:** A inclusão ou atualização do item é permitida quando houver estoque suficiente.

**Violação:** A operação é rejeitada quando a quantidade solicitada superar o estoque disponível.

## RN-005 — Itens só podem ser alterados em pedidos em rascunho

**Descrição:** Inclusão, alteração e remoção de itens somente são permitidas para pedidos com status DRAFT.

**Condições:**
- O pedido deve existir.
- O status atual do pedido deve ser DRAFT.

**Resultado esperado:** Itens podem ser incluídos, alterados ou removidos.

**Violação:** Qualquer alteração de item é rejeitada após o pedido deixar o estado DRAFT.

## RN-006 — Pedido precisa possuir ao menos um item para checkout

**Descrição:** Um pedido vazio não pode avançar de DRAFT para PENDING_PAYMENT.

**Condições:**
- O pedido deve possuir pelo menos um item.

**Resultado esperado:** O checkout pode prosseguir quando o pedido possuir ao menos um item.

**Violação:** O checkout é rejeitado quando o pedido estiver vazio.

## RN-007 — Estoque deve ser revalidado no checkout

**Descrição:** No momento do checkout, todos os itens devem possuir estoque suficiente.

**Condições:**
- O estoque atual de cada produto deve ser igual ou superior à quantidade presente no pedido.

**Resultado esperado:** O pedido pode avançar para PENDING_PAYMENT quando todos os itens ainda tiverem estoque suficiente.

**Violação:** O checkout é rejeitado quando qualquer item tiver se tornado indisponível ou insuficiente.

## RN-008 — Cupom deve estar ativo e dentro do período de validade

**Descrição:** Um cupom somente pode ser aplicado quando estiver ativo e a data atual estiver dentro do seu período de validade.

**Condições:**
- O cupom deve existir.
- O status do cupom deve ser ACTIVE.
- A data atual deve estar entre validFrom e validUntil, inclusive.

**Resultado esperado:** O cupom pode ser aplicado ao pedido.

**Violação:** A aplicação é rejeitada quando o cupom estiver inativo, ainda não for válido ou estiver expirado.

## RN-009 — Pedido deve atingir o valor mínimo do cupom

**Descrição:** Um cupom só pode ser aplicado quando o valor do pedido atingir o valor mínimo exigido pelo cupom.

**Condições:**
- O subtotal elegível do pedido deve ser maior ou igual a minimumOrderValue do cupom.

**Resultado esperado:** O cupom pode ser aplicado quando o valor mínimo for atingido.

**Violação:** A aplicação é rejeitada quando o pedido estiver abaixo do valor mínimo.

## RN-010 — Apenas um cupom por pedido

**Descrição:** Um pedido não pode possuir mais de um cupom aplicado simultaneamente.

**Condições:**
- O pedido não deve possuir outro cupom aplicado no momento da aplicação.

**Resultado esperado:** O primeiro cupom pode ser aplicado e pode ser posteriormente removido.

**Violação:** A aplicação de um segundo cupom é rejeitada enquanto já houver um cupom associado.

## RN-011 — Cupom só pode ser alterado em pedidos em rascunho

**Descrição:** Aplicação ou remoção de cupom só é permitida para pedidos com status DRAFT.

**Condições:**
- O status atual do pedido deve ser DRAFT.

**Resultado esperado:** O cupom pode ser aplicado ou removido enquanto o pedido estiver em DRAFT.

**Violação:** A alteração de cupom é rejeitada após o checkout.

## RN-012 — Apenas pedidos aguardando pagamento podem ser pagos

**Descrição:** A operação de pagamento só pode ser executada quando o pedido estiver em PENDING_PAYMENT.

**Condições:**
- O status atual do pedido deve ser PENDING_PAYMENT.

**Resultado esperado:** O pagamento pode ser processado.

**Violação:** O pagamento é rejeitado para pedidos em qualquer outro estado.

## RN-013 — Valor pago deve corresponder ao total do pedido

**Descrição:** O valor informado no pagamento deve corresponder exatamente ao total calculado do pedido.

**Condições:**
- O valor do pagamento deve ser igual ao total atual do pedido.

**Resultado esperado:** O pagamento é aceito quando o valor corresponder ao total.

**Violação:** O pagamento é rejeitado quando o valor for diferente do total do pedido.

## RN-014 — Cancelamento permitido apenas antes do pagamento

**Descrição:** Somente pedidos nos estados DRAFT ou PENDING_PAYMENT podem ser cancelados.

**Condições:**
- O status atual do pedido deve ser DRAFT ou PENDING_PAYMENT.

**Resultado esperado:** O pedido é movido para CANCELLED.

**Violação:** O cancelamento é rejeitado para pedidos PAID, SHIPPED ou CANCELLED.

## RN-015 — Apenas pedidos pagos podem ser enviados

**Descrição:** Um envio somente pode ser criado para pedidos com status PAID.

**Condições:**
- O status atual do pedido deve ser PAID.

**Resultado esperado:** O envio é criado e o pedido pode avançar para SHIPPED.

**Violação:** A criação de envio é rejeitada para pedidos que ainda não estejam pagos.
