# [Nome do Projeto - ex: DailyDecide ou AIBriefing]

![Status: Em Desenvolvimento](https://img.shields.io/badge/status-em__desenvolvimento-yellow)
![Licença: MIT](https://img.shields.io/badge/licen%C3%A7a-MIT-blue.svg)

Um SaaS multi-tenant focado em gerar insights diários com IA a partir de dados de ERPs, entregues por e-mail para facilitar a tomada de decisão.

---

## 📖 Índice

* [O Problema](#-o-problema)
* [Funcionalidades Principais](#-funcionalidades-principais)
* [Stack Tecnológica](#-stack-tecnológica)
* [Arquitetura do Sistema](#-arquitetura-do-sistema)
* [Como Executar Localmente](#-como-executar-localmente)
* [Licença](#-licença)

---

## 🎯 O Problema

Empresas que utilizam ERPs possuem um volume massivo de dados transacionais (vendas, estoque, produtos). No entanto, extrair *valor* e *insights acionáveis* desses dados é um processo manual e lento.

Este projeto resolve isso ao:
1.  **Automatizar** a coleta de dados de vendas, estoque e produtos.
2.  **Enriquecer** os dados brutos (ex: `loja_id: 3` vira `Loja: "Shopping Iguatemi"`).
3.  **Usar IA** para analisar os dados, gerar resumos e destacar insights.
4.  **Entregar** um "briefing" de negócios pronto por e-mail, todos os dias às 06:00, permitindo que gestores comecem o dia já informados.

---

## ✨ Funcionalidades Principais

* **Arquitetura Multi-Tenant:** Um único deploy da aplicação atende múltiplos clientes (tenants) de forma segura e isolada.
* **Gestão de Assinaturas:** Integração com gateway de pagamento (ex: Stripe) para controle de planos e pagamentos.
* **Gestão Segura de Credenciais:** Armazenamento criptografado das chaves de API dos ERPs dos clientes.
* **Cache Dimensional (L2):** Sincronização e armazenamento local de metadados do ERP (como Lojas, Categorias, Vendedores) para evitar o problema N+1 e reduzir drasticamente as chamadas de API.
* **Processamento Assíncrono com Filas:** Uso de Jobs (Redis/RabbitMQ) para todas as tarefas pesadas (Sincronização de Metadados, Geração de Relatórios).
* **Geração de Relatórios com IA:** Orquestração de chamadas à API do ERP, enriquecimento de dados e envio para um modelo de IA (OpenAI/Gemini) para sumarização.
* **Entrega por E-mail:** Envio dos relatórios via serviços transacionais (Amazon SES, SendGrid).

---

## 🛠️ Stack Tecnológica

| Componente | Tecnologia |
| :--- | :--- |
| **Backend (API de Gestão)** | `[Sua Linguagem - ex: Kotlin, Java, Node.js]` + `[Seu Framework - ex: Spring Boot, NestJS]` |
| **Frontend** | `[Seu Framework - ex: React, Angular, Vue]` |
| **Banco de Dados (Cache L2)** | `PostgreSQL` (ou `MySQL`) |
| **Fila & Cache (L1)** | `Redis` (para Jobs e Cache de performance) |
| **Workers (Executor)** | `[Mesma stack do Backend]` |
| **Pagamentos** | `[ex: Stripe, Pagar.me]` |
| **E-mail Transacional** | `[ex: Amazon SES, SendGrid]` |
| **IA** | `[ex: OpenAI API, Gemini API]` |
| **Hospedagem** | `[ex: AWS, DigitalOcean, Render]` |
| **Containerização** | `Docker` e `Docker Compose` |

---

## 🏛️ Arquitetura do Sistema

A arquitetura é baseada em **dois serviços principais** para garantir escalabilidade independente.

1.  **Serviço 1: API de Gestão (Monolito Modular)**
    * **Responsabilidades:** Lida com todo o "estado" do cliente: CRUD de usuários, autenticação, gerenciamento de assinaturas (webhooks de pagamento) e armazenamento seguro de credenciais.
    * **Agendador (Scheduler):** Este serviço também contém o Agendador (Scheduler) que roda em horários definidos (ex: 03:00 para Sincronização Dimensional, 05:55 para Geração de Relatórios) e **cria os jobs** na fila.

2.  **Serviço 2: Executor de Relatórios (Workers)**
    * **Responsabilidades:** É um serviço *stateless* (sem estado) e escalável, desenhado para ser um "operário". Ele não tem API; ele apenas **ouve a Fila**.
    * **Escalabilidade:** Você pode ter 1 instância deste serviço ou 50, dependendo da sua carga de trabalho (ex: às 06:00).
    * **Lógica:** Executa a lógica pesada (buscar no Cache L1, buscar no Cache L2, chamar a API do ERP, chamar a IA, enviar e-mail).

### Fluxograma do Job Principal (Relatório das 06:00)

Este diagrama (feito em Mermaid) mostra o fluxo completo de um job de geração de relatório, incluindo a estratégia de cache em dois níveis (L1 - Redis, L2 - PostgreSQL).

```mermaid
%% Fluxo Principal: Geração do Relatório (Job das 06:00)
flowchart TD
    %% Bloco: Agendamento API de Gestão
    SCH["⏰ Agendador - 05:55"]
    API["👩‍💼 API de Gestão"]
    SCH_DB[("🗄️ saas_db")]
    SCH -- 1. Inicia --> API
    API -- 2. Busca Clientes Ativos --> SCH_DB
    API -- 3. [Loop] Cria Jobs 'gerar_relatorio' --> Q

    %% Bloco: Fila
    Q(("mq Fila - Redis"))

    %% Bloco: Workers Serviço 2: Executor de Relatórios
    W1["⚙️ Worker 1"]
    W2["⚙️ Worker 2"]
    WN["⚙️ Worker N..."]

    %% Bloco: Armazenamento de Dados
    CACHE[("💾 Cache L1 - Redis")]
    DB[("🗄️ Cache L2 - saas_db")]

    %% Bloco: Serviços Externos
    ERP["🏭 API Externa do ERP"]
    IA["🤖 Serviço de IA"]
    MAIL["📤 Serviço de E-mail"]
    CLIENTE["👤 Cliente Final"]
    
    %% Definição de Estilos
    classDef servico fill:#ececff,stroke:#9370db,stroke-width:2px;
    classDef db fill:#fff0f5,stroke:#db7093,stroke-width:2px;
    classDef fila fill:#e0ffff,stroke:#008b8b,stroke-width:2px;
    classDef ext fill:#f5f5dc,stroke:#8b4513,stroke-width:2px;
    classDef cache fill:#e6ffe6,stroke:#006400,stroke-width:2px;
    
    class API,SCH,W1,W2,WN servico
    class DB,SCH_DB db
    class Q fila
    class ERP,IA,MAIL,CLIENTE ext
    class CACHE cache

    %% Distribuição dos Jobs
    Q -- 4. Entrega Job --> W1
    Q -- 4. Entrega Job --> W2
    Q -- 4. Entrega Job --> WN

    %% Lógica Detalhada do Worker (Ex: W1)
    W1 -- 5. Pega Job {tenant_id: 123} --> Q
    W1 -- 6. Tenta buscar `dimensions:123` --> CACHE
    
    CACHE -- 7. Verifica --> C{"Cache L1?"}
    C -->|HIT ✅| W1_DADOS_OK["Worker tem dados Lojas, Cat."]
    
    %% Caminho do Cache Miss (L1)
    C -->|MISS ❌| W1_BUSCA_DB["Worker busca no Cache L2"]
    W1_BUSCA_DB -- 8. Busca Lojas, Cat., etc. --> DB
    DB -- 9. Retorna dados --> W1_BUSCA_DB
    W1_BUSCA_DB -- 10. Salva em L1 para futuro --> CACHE
    W1_BUSCA_DB --> W1_DADOS_OK

    %% Continuação (Worker com dados dimensionais em mãos)
    W1_DADOS_OK -- 11. Busca Vendas (lista) --> ERP
    W1_DADOS_OK -- 12. Busca Detalhes Vendas (N+1) --> ERP
    ERP -- 13. Retorna dados brutos --> W1_DADOS_OK
    
    W1_DADOS_OK -- 14. Enriquece Dados (Join em memória) --> W1_DADOS_ENRIQUECIDOS["Worker Dados Enriquecidos"]
    
    W1_DADOS_ENRIQUECIDOS -- 15. Envia p/ Sumarização --> IA
    IA -- 16. Retorna Insights --> W1_DADOS_ENRIQUECIDOS
    
    W1_DADOS_ENRIQUECIDOS -- 17. Monta e Envia E-mail --> MAIL
    MAIL -- 18. Entrega p/ Cliente --> CLIENTE
    
    W1_DADOS_ENRIQUECIDOS -- 19. Marca Job 'Concluído' --> Q
    W1_DADOS_ENRIQUECIDOS -- 20. Busca próximo Job --> Q