# Minimal TV — Direção visual

## Modo e contexto

Interface Android TV de operação por D-pad, vista à distância em 1280×720 lógico e 1920×1080 físico. Prioridade: foco, legibilidade e baixo custo de renderização.

## Paleta aprovada

| Papel | HEX |
|---|---|
| Fundo | `#10151C` |
| Superfície de cartões | `#272039` |
| Texto principal | `#F5F7FA` |
| Principal e foco | `#C7A6FF` |
| Destaque secundário | `#E0CCFF` |

Paleta violeta proposta e escolhida pelo usuário; não foi extraída de referências. Texto principal/fundo: 17,07:1; texto principal/superfície: 14,47:1; fundo/botão principal: 9,01:1 (cálculo WCAG). Estados de foco serão validados no dispositivo.

## Estrutura

- Margens seguras para overscan.
- Topo: nome e subtítulo à esquerda; relógio e configurações à direita.
- Centro: título “Aplicativos” e grade de 4 colunas na resolução lógica 1280×720. Cada cartão mostra ícone real, nome e foco visível por contorno e cor, sem blur.
- Rodapé: ações de favoritos e configurações.
- Movimentos discretos ou imediatos; nenhuma animação decorativa e nenhuma atualização por segundo do relógio.

## Referências verificadas em 20/10/2026

- [Apple TV](https://www.apple.com/apple-tv-app/): página clara, tipografia grande e contenção visual.
- [Roku](https://www.roku.com/pt-br/): base violeta intensa e contraste alto.
- [Google TV](https://tv.google/): base escura, ênfase azul e elementos de apps em destaque.

Esses sites orientam comparação de linguagem; a Home do Minimal TV não incorpora seus feeds ou conteúdo remoto.
