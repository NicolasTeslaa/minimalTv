# Minimal TV — Especificação do MVP

## Regras confirmadas

1. A Home lista dinamicamente os aplicativos iniciáveis. Apps com atividade LEANBACK_LAUNCHER têm prioridade; atividades LAUNCHER servem de fallback. Uma entrada por package name, sem lista fixa. O próprio Minimal TV não entra na grade.
2. Favoritos aparecem antes dos demais. Entre favoritos, maior contagem de aberturas pelo Minimal TV vem primeiro; empate usa nome em ordem alfabética. Não favoritos usam nome em ordem alfabética. Favorito e contador persistem entre reinícios.
3. Segurar OK em um app abre menu contextual para marcar ou desmarcar favorito. OK curto abre o app. O rodapé contém gerenciamento de favoritos e configurações, sem atalho “Todos os aplicativos”.
4. Instalação, atualização e remoção de pacotes atualizam a lista sem polling. Uma alteração preserva o foco pelo identificador do app quando ele continua presente; se ele desapareceu, o foco vai ao item vizinho ou primeira ação útil.
   Na remoção, favorito e contador desse package são apagados; reinstalação começa do zero.
5. Falha de abertura mostra mensagem recuperável e mantém a Home utilizável. Voltar fecha menus/telas secundárias e retorna à Home; foco é restaurado ao voltar de apps externos.
6. Settings do Minimal TV oferece acesso direto às configurações do Android TV. Nenhuma troca de HOME ocorre automaticamente.

## Arquitetura

Um módulo `app`, uma Activity principal, Views e RecyclerView. `AppRepository` consulta PackageManager fora da thread principal e resolve componentes. `PreferencesRepository` mantém favoritos/contadores em SharedPreferences. Regras puras de ordenação e foco são separadas para testes JVM. `MainActivity` apresenta estados de Home, gerenciamento e configurações, com o mínimo de objetos ativos.

## Compatibilidade e segurança

Manifest com Leanback e HOME/DEFAULT em filtros separados, touchscreen não obrigatório, banner/ícone, `android:exported`, e `<queries>` para ambas categorias de lançamento. Sem `QUERY_ALL_PACKAGES`, rede, serviços persistentes, root ou alteração do launcher de fábrica. Android TV 14 é o alvo de validação física; o código deve compilar com SDK estável compatível.

## Aceite observável

- APK debug compila e instala; Activity inicia como app comum.
- Netflix e Play Store, quando instalados e visíveis por PackageManager, abrem por OK. HBO Max só pode ser validado se estiver instalado.
- Favorito persiste e muda de posição com sua contagem; app removido sai da lista; erro de launch não derruba a Activity.
- OK longo não lança o app quando a tecla é solta. Uma tentativa de abertura que falhe não aumenta o contador.
- D-pad alcança todos os controles; foco não salta sem causa após atualização e retorno.
- Métricas são registradas no hardware real, sem alegar atingir metas sem medição.
- O launcher original permanece instalado e ativo; há procedimento de recuperação.
