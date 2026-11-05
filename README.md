# Triagem de e-mails com LLM

Projeto pequeno em Java para praticar integração com LLM. Ele lê e-mails de um arquivo e, para cada um, pede à API do Claude a categoria (reclamação, dúvida ou pedido), um resumo e uma sugestão de resposta. Se o MySQL estiver configurado, salva o resultado no banco.

## Como rodar

Precisa de Java 17+, Maven e uma chave da API da Anthropic.

```bash
export ANTHROPIC_API_KEY="sua-chave"
mvn compile exec:java
```

Para salvar no MySQL, crie a tabela com `sql/schema.sql` e defina `DB_URL` (ex.: `jdbc:mysql://localhost:3306/triagem`), `DB_USER` e `DB_PASSWORD`. Sem `DB_URL`, só imprime na tela.

O modelo padrão é `claude-haiku-4-5`; dá para trocar com `ANTHROPIC_MODEL`.

## O que aprendi / próximos passos

- A LLM às vezes devolve o JSON dentro de um bloco de código, então o programa limpa isso antes de ler.
- Quero transformar em agente, para a LLM consultar o banco antes de responder.
- Quero ler e-mails de verdade em vez do arquivo de exemplo.
