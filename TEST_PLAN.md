# Plano e evidências de teste

## Ambiente

Em 20/10/2026, a TV Aiwa/KTC conectada via ADB reportou Android API 34, `stanford`, `low_ram=true`, ABI `armeabi-v7a,armeabi`, resolução lógica 1280×720 e física 1920×1080. O serial observado foi `192.168.1.9:36661`; a porta pode mudar.

## Evidências coletadas

| Cenário | Método | Resultado em 20/10/2026 |
|---|---|---|
| Ordenação: favoritos, contagem e desempate | JUnit `AppOrderTest` | 2 testes passaram após RED com 2 falhas esperadas |
| Persistência de favorito e contagem | AndroidTest na TV | Passou na segunda rodada; primeira RED falhou como esperado |
| Remoção apaga favorito e contagem | AndroidTest na TV | RED confirmou falha; 2 testes de persistência passaram após implementação |
| Descoberta de Netflix e Play Store | AndroidTest na TV | RED falhou com repositório vazio; passou após consulta PackageManager com `<queries>` |
| Prioridade Leanback, fallback e deduplicação | JUnit `AppSelectionTest` | 2 testes passaram após RED com 2 falhas esperadas |

## Casos a executar no fechamento

### Estado desta rodada

O código da Home foi conectado aos fluxos de abertura, pressão longa, mudanças de
pacotes e atualização do relógio. A execução local dos testes e do build permanece
pendente porque este ambiente não possui JDK; não há nova evidência de execução
física nesta rodada.

Nesta rodada, o componente HBO Max foi confirmado no hardware como
`com.wbd.stream/com.wbd.beam.BeamActivity`; o comando ADB explícito retornou
`Status: ok`. O APK instalado durante a investigação era anterior ao commit que
conectou os callbacks da Home.

- Descoberta Leanback, fallback Launcher, deduplicação e app desabilitado.
- OK curto, OK longo sem abertura no KEY_UP, falha de abertura sem aumento da contagem.
- Atualização após instalação/remoção, foco após mudança, retorno de app externo e Back.
- Operação offline, reinício do processo, 1280×720 e 1920×1080.
- Verificação de elegibilidade HOME sem selecionar padrão.
- Instalação e abertura de Netflix e Play Store. HBO Max não estava instalado na TV consultada; não declarar teste físico de abertura até estar disponível.

Sem o Minimal TV como Home padrão, o botão Home do controle retorna ao launcher de fábrica. Para testar o retorno ao Minimal TV após outro app, use Back quando possível ou reabra a Activity explicitamente.
