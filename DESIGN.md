# Minimal TV — Direção visual

## Modo e contexto

Interface Android TV de operação por D-pad, vista à distância em 1280×720 lógico e 1920×1080 físico. Prioridade: foco, legibilidade e baixo custo de renderização.

## Paleta aprovada

| Papel | HEX |
|---|---|
| Fundo | `#050A19` |
| Superfície de cartões | `#182B47` com transparência |
| Texto principal | `#F4F7FF` |
| Principal e foco | `#79C8FF` |
| Destaque secundário | `#B9C8E6` |

Paleta azul-roxa com ondas desenhadas nativamente, escolhida a partir da referência enviada pelo usuário; não foi extraída de referências externas. Texto principal/fundo: 18,42:1; foco/fundo: 10,83:1 (cálculo WCAG). Estados de foco serão validados no dispositivo.

## Estrutura

- Margens seguras para overscan.
- Topo: nome e subtítulo à esquerda; relógio e configurações à direita.
- Centro: título “Aplicativos” e grade de 6 colunas na resolução lógica 1280×720. Cada cartão compacto mostra ícone real de 48dp, nome, superfície azul translúcida e foco azul luminoso por contorno, sem blur.
- Rodapé: ações de favoritos e configurações.
- Movimentos discretos ou imediatos; nenhuma animação decorativa e nenhuma atualização por segundo do relógio.

## Referências verificadas em 20/10/2026

- [Apple TV](https://www.apple.com/apple-tv-app/): página clara, tipografia grande e contenção visual.
- [Roku](https://www.roku.com/pt-br/): base violeta intensa e contraste alto.
- [Google TV](https://tv.google/): base escura, ênfase azul e elementos de apps em destaque.

Esses sites orientam comparação de linguagem; a Home do Minimal TV não incorpora seus feeds ou conteúdo remoto.
