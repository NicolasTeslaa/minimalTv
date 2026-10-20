# Minimal TV — Design aprovado

O desenho e os exemplos de aceite foram aprovados pelo usuário em 20/10/2026. O escopo é um launcher Android TV funcional, offline na Home, sem feeds e com controle remoto como único meio de interação.

## Fluxo

Home com todos os apps: favoritos primeiro, ordenados por aberturas feitas no Minimal TV; demais em ordem alfabética. OK abre, OK longo oferece favorito, Voltar fecha a tela secundária ou menu. Há acesso a gerenciamento de favoritos e configurações no rodapé, e às configurações do Android TV na tela de configurações.

## Componentes

Uma Activity mantém estados de tela simples; RecyclerView reutiliza cartões. Repositório de apps consulta PackageManager em executor, cache limitado de ícones e receptor de mudanças de pacote. Preferências persistem favoritos e contagens. Regras de ordenação e restauração de foco são funções testáveis.

## Resiliência

Uma Activity inexistente ou que falha ao iniciar não causa crash. Mudança de pacotes atualiza a lista sem polling e preserva foco quando possível. O app é candidato HOME mas nunca desativa o original nem solicita o papel automaticamente.
Ao remover um app, seus dados de favorito e uso são excluídos; reinstalação começa do zero, conforme decisão posterior do usuário.

## Validação

TDD para ordenação, favoritos, contadores, descoberta e falhas de abertura. Build debug, testes JVM e de instrumentação onde disponível; instalação e navegação física na Aiwa conectada; PSS, CPU e tempos registrados com condições de medição. Meta de PSS não equivale a garantia.

Ver [SPEC.md](../../../SPEC.md), [DESIGN.md](../../../DESIGN.md) e [PRODUCT.md](../../../PRODUCT.md) para regras e direção visual.
