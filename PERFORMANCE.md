# Desempenho

## Metas iniciais

- PSS em repouso do Minimal TV: até 40 MiB.
- Inicialização quente: até 1 s.
- CPU próxima de zero em repouso, sem serviço persistente nem rede na Home.

São metas, não medições nem garantias.

## Amostra de referência do launcher original

Em 20/10/2026, `adb shell dumpsys meminfo -s com.google.android.tvlauncher` retornou TOTAL PSS **90.486 KiB**, TOTAL RSS **57.172 KiB** e TOTAL SWAP PSS **63.540 KiB**. `cat /proc/meminfo` indicou MemTotal 935.352 KiB e MemAvailable 283.444 KiB. O uptime do aparelho era aproximadamente 899 s. Não houve controle de apps abertos ou repetição; esta é apenas uma amostra de referência.

## Procedimento comparativo

1. Fixar resolução, tempo após boot e conjunto de apps abertos.
2. Abrir cada launcher e aguardar a mesma janela de repouso.
3. Repetir `dumpsys meminfo -s`, `top -b -n 1 -m 15` e `/proc/meminfo` três vezes por launcher.
4. Medir início quente com `am start -W` após retorno de app externo, sem incluir instalação.
5. Registrar PSS, RSS, swap PSS, CPU, latência D-pad, ANRs e contexto da medição.

Não somar RSS de processos para estimar uso físico exclusivo. Não inferir economia de memória a partir de uma amostra isolada.
