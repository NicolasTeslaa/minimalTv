# Changelog

## 0.1.0 — em desenvolvimento

- Projeto Android TV criado com Kotlin, RecyclerView e Gradle Wrapper.
- Regras de ordenação e persistência implementadas por TDD.
- Fluxo da Home conectado: OK abre o app, pressão longa gerencia favoritos e falhas
  de abertura exibem feedback recuperável.
- Home atualiza a lista em mudanças de pacotes e mantém o relógio atualizado por minuto.
- Launch explícito inclui a categoria Leanback, compatível com atividades como
  `com.wbd.stream/com.wbd.beam.BeamActivity`.
- Home compactada para seis colunas em 1280×720, ícones de 48dp e paleta violeta silenciosa.

### Verificação desta rodada

- `git diff --check`: passou.
- Build e testes Gradle: não executados porque o ambiente atual não possui JDK instalado.
