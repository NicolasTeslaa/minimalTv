# Changelog

## 0.1.0 — em desenvolvimento

- Projeto Android TV criado com Kotlin, RecyclerView e Gradle Wrapper.
- Regras de ordenação e persistência implementadas por TDD.
- Fluxo da Home conectado: OK abre o app, pressão longa gerencia favoritos e falhas
  de abertura exibem feedback recuperável.
- Home atualiza a lista em mudanças de pacotes e mantém o relógio atualizado por minuto.

### Verificação desta rodada

- `git diff --check`: passou.
- Build e testes Gradle: não executados porque o ambiente atual não possui JDK instalado.
