# Desenvolvimento e avaliação de uma ferramenta web para identificação de lacunas de rastreabilidade entre regras de negócio, operações de APIs REST e casos de teste automatizados

## Como funcionaria?

A ideia seria uma ferramenta de engenharia de software que cria e mantém um mapa entre três artefatos que normalmente ficam separados:
Regra de negócio ↔ operação da API ↔ teste automatizado

Isso permitiria responder perguntas como:

“Essa regra de negócio está sendo testada?”
“Qual endpoint implementa essa regra?”
“Se eu alterar essa regra, quais endpoints e testes preciso revisar?”
“Existe endpoint sem nenhuma regra relacionada?”
“Existe regra de negócio que não possui teste?”
“Um endpoint foi removido, mas ainda existem vínculos apontando para ele?”

### Ideia do esqueleto geral:
- Spring Boot no backend; <br/>
- PostgreSQL; <br/>
- Angular no frontend; <br/>
- Docker; <br/>
- OpenAPI/Swagger; <br/>
- JUnit; <br/>



