# Minimal TV — Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Kotlin, Android SDK nativo, Android Views, RecyclerView, Gradle Kotlin DSL e SharedPreferences, conforme o briefing aprovado.

## Users

Pessoa que usa uma Smart TV Aiwa/KTC com Android TV 14 e controle remoto, em hardware de aproximadamente 913 MiB de RAM.

## Product Purpose

Abrir os aplicativos instalados sem feeds, anúncios, serviços próprios persistentes ou dependência de rede na Home. Ser elegível como launcher HOME sem alterar o firmware nem desativar o launcher original.

## Operating Context

TV de resolução física 1920×1080, resolução lógica atual 1280×720, densidade 213 dpi, `low_ram=true` e ABI de apps `armeabi-v7a,armeabi` confirmada via ADB em 20/10/2026. Navegação por D-pad e botão OK.

## Capabilities and Constraints

- Home mostra todos os apps iniciáveis, favoritos primeiro. Favoritos são ordenados por número de aberturas feitas pelo Minimal TV; os demais, por nome.
- Segurar OK abre menu para adicionar ou remover favorito. O rodapé oferece gerenciamento de favoritos e configurações. Não há atalho separado “Todos os aplicativos”.
- Ao desinstalar um app, seu favorito e contador são apagados. Uma reinstalação com o mesmo package name começa do zero.
- App descobre atividades LEANBACK_LAUNCHER e usa LAUNCHER como fallback quando apropriado, respeitando package visibility sem QUERY_ALL_PACKAGES.
- O launcher original permanece instalado e habilitado. Nenhum componente de firmware, DRM ou sistema crítico é modificado.
- Metas de engenharia: PSS em repouso até 40 MiB e inicialização quente até 1 segundo, sujeitas a medição real.

## Brand Commitments

Nome “Minimal TV”, subtítulo “Your apps. Nothing else.” e fundo `#10151C` definidos no briefing. Direção cromática violeta escolhida pelo usuário.

## Evidence on Hand

Em 20/10/2026, ADB mostrou Android API 34, dispositivo `SMART_TV`/`stanford`, conexão `192.168.1.9:36661`, `low_ram=true`, e launcher original `com.google.android.tvlauncher`. Uma amostra de `dumpsys meminfo -s` indicou PSS original de 90.486 KiB; não é benchmark comparativo.

## Product Principles

1. O controle remoto alcança todas as ações.
2. Foco e ordem da lista são previsíveis.
3. A interface fica offline e ociosa sem trabalho periódico desnecessário.
4. Recuperar o launcher original deve continuar simples.
