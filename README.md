# Camisa 10 – A Marca do Boleiro

Aplicativo mobile de e-commerce de camisas de futebol, com dois perfis de acesso:

- **Vendedor:** cadastra, edita e exclui camisas, com fotos e estoque por tamanho, e acompanha as vendas.
- **Comprador:** navega pela vitrine com filtros, escolhe tamanho, monta o carrinho, finaliza o pedido e consulta o histórico de compras.

Projeto da disciplina **Projeto, Design e Engenharia de Processos** (Atitus).

| Parte | Responsável |
|---|---|
| `backend/` – API REST em Java | João |
| `mobile/` – app React Native | Eduardo |

---

## Sumário

1. [Tecnologias](#tecnologias)
2. [Estrutura do repositório](#estrutura-do-repositório)
3. [Como executar](#como-executar)
4. [Usuários de teste](#usuários-de-teste)
5. [Testes](#testes)
6. [Funcionalidades por perfil](#funcionalidades-por-perfil)
7. [Requisitos da disciplina](#requisitos-da-disciplina)
8. [Requisitos funcionais (RF) e onde estão](#requisitos-funcionais-rf-e-onde-estão)
9. [Endpoints da API](#endpoints-da-api)
10. [Decisões técnicas e observações](#decisões-técnicas-e-observações)

---

## Tecnologias

| Camada | Tecnologias |
|---|---|
| **Mobile** | React Native 0.88 + **Expo SDK 58**, **JavaScript** (sem TypeScript), React Navigation 7, Axios, Context API (login e carrinho), expo-secure-store (token), expo-image-picker (fotos), react-native-svg (ícones e desenho da camisa), expo-font (Manrope e DM Mono) |
| **Backend** | **Java 17**, **Spring Boot 3.5** (Web, Data JPA, Validation, Security com OAuth2 Resource Server para JWT), Maven (wrapper incluso) |
| **Banco** | **H2 em memória** no perfil `dev` (não precisa instalar nada) ou **PostgreSQL** no perfil padrão |
| **Testes** | JUnit 5 + MockMvc (backend), `node:test` (regras do app), coleção do Postman |

---

## Estrutura do repositório

```
projeto-camisa10-atitus/
├── backend/                         API Spring Boot
│   ├── src/main/java/br/edu/atitus/camisa10/
│   │   ├── controller/              rotas REST (entrada HTTP)
│   │   ├── service/                 regras de negócio
│   │   ├── repository/              acesso ao banco (Spring Data JPA)
│   │   ├── model/                   entidades JPA (Produto, Pedido, ...)
│   │   ├── dto/                     objetos de entrada/saída da API
│   │   ├── security/                JWT e permissões por perfil
│   │   ├── exception/               erros padronizados em português
│   │   ├── config/                  dados de exemplo, migrações e arquivos estáticos
│   │   └── util/                    utilitários (busca sem acento, ano, cores, marcas)
│   ├── src/main/resources/
│   │   ├── application.properties       configuração (PostgreSQL)
│   │   ├── application-dev.properties   perfil dev (H2)
│   │   └── seed/camisas/                fotos das 41 camisas de exemplo
│   ├── catalogo-fotos/              catálogo com 1.294 fotos para o vendedor escolher
│   ├── postman/                     coleção do Postman com testes
│   └── src/test/                    testes automatizados
└── mobile/                          app Expo
    ├── App.js                       raiz: fontes, providers e navegação
    ├── src/
    │   ├── config.js                ÚNICO lugar com o endereço da API
    │   ├── theme.js                 cores, fontes e espaçamentos
    │   ├── navigation/              rotas: login, abas do vendedor e do comprador
    │   ├── screens/auth|comprador|vendedor/   telas de cada área
    │   ├── components/              componentes reutilizáveis
    │   ├── contexts/                AuthContext (login) e CarrinhoContext
    │   ├── services/                chamadas à API (Axios)
    │   └── utils/                   regras sem tela (preço, tamanhos, filtros, cores)
    ├── tests/                       testes das regras do app
    └── .env.example                 modelo para configurar o endereço da API
```

---

## Como executar

### Pré-requisitos

- **JDK 17 ou superior.** Não precisa instalar o Maven: o projeto traz o `mvnw`.
- **Node.js 20 ou superior.**
- No celular, o app **Expo Go** (Play Store ou App Store), ou um emulador Android.

### 1. Backend (API)

Na pasta `backend`, com o **perfil `dev`** (banco H2 em memória, sem instalar nada):

```powershell
# Windows (PowerShell)
cd backend
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

```bash
# Linux / macOS
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

A API sobe em `http://localhost:8080` quando aparecer `Started Camisa10Application`.

- **Dados do perfil dev:** a cada início, o banco é recriado com 2 usuários, 10 categorias e **41 camisas com foto**. As compras e os cadastros feitos durante o uso somem quando a API reinicia.
- **Console do banco:** `http://localhost:8080/h2-console`, com JDBC URL `jdbc:h2:mem:camisa10`, usuário `sa` e senha em branco.

> **Sem o perfil `dev`, a API tenta conectar num PostgreSQL** em `localhost:5432/camisa10`. Para usar o PostgreSQL, crie o banco `camisa10` e, se precisar, defina as variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD`. Em produção, defina também `JWT_SECRET` com pelo menos 32 caracteres.
>
> **Banco já existente:** quem já tem dados de uma versão anterior do projeto não precisa recriar nada. A classe `MigracoesBanco` atualiza as tabelas automaticamente.

### 2. App mobile

```powershell
cd mobile
npm install
```

**Configure o endereço da API.** Copie `mobile/.env.example` para `mobile/.env.local` e ajuste a linha:

```
EXPO_PUBLIC_API_URL=http://SEU_IP:8080
```

| Onde o app vai rodar | Valor |
|---|---|
| Celular físico com Expo Go | IP do computador na rede Wi-Fi. No Windows, use `ipconfig` e o campo "Endereço IPv4". **Celular e computador na mesma rede.** |
| Emulador Android | `http://10.0.2.2:8080`. É o padrão quando o `.env.local` não existe. |
| Simulador iOS | `http://localhost:8080` |

Depois inicie o Expo e leia o QR code com o Expo Go (Android) ou com a câmera (iPhone):

```powershell
npx expo start -c
```

**Se aparecer "Não foi possível conectar ao servidor":**
- confira se a API está rodando;
- confira o IP no `.env.local`, e reinicie o Expo depois de trocar;
- confira se o **Firewall do Windows** liberou o Java na rede.

---

## Usuários de teste

Criados automaticamente, com a senha **`123456`** para os dois:

| Perfil | E-mail |
|---|---|
| Vendedor | `vendedor@camisa10.com` |
| Comprador | `comprador@camisa10.com` |

Também é possível criar contas novas pela tela de cadastro, escolhendo o perfil.

---

## Testes

| O quê | Como rodar | Resultado esperado |
|---|---|---|
| **Backend** (JUnit + MockMvc) | `cd backend` e `.\mvnw test` | **87 testes** passando |
| **Regras do app** (`node:test`) | `cd mobile` e `npm test` | **11 testes** passando |
| **API pelo Postman** | importe `backend/postman/Camisa10.postman_collection.json`, suba a API em `dev` e rode a coleção | todos os testes da coleção passando |

Os testes do backend cobrem:
- cadastro e login;
- **permissões por perfil**: o comprador recebe 403 ao tentar criar, editar ou excluir produto;
- CRUD e upload de imagem;
- filtros e ordenação;
- compra com estoque por tamanho e **rollback** quando um item falha;
- históricos de compras e vendas;
- migrações do banco e utilitários.

---

## Funcionalidades por perfil

### Vendedor
- **Painel:** faturamento, número de vendas, produtos ativos e as últimas vendas.
- **Meus produtos:** lista com foto, preço e estoque. Tocar edita; a lixeira exclui, com exclusão lógica: o produto sai da vitrine e os pedidos antigos continuam.
- **Cadastro e edição:**
  - foto pelo **catálogo de fotos**, pela **galeria** ou pela **câmera** (com aviso quando a foto não é do catálogo);
  - nome, ano, marca, preço, **estoque por tamanho** (P, M, G, GG, XGG), descrição, **uma ou mais categorias** e as cores da camisa;
  - **tela de revisão** antes de gravar: mostra tudo, avisa quando o preço está abaixo de R$ 100 e, na edição, marca o que mudou.
- **Vendas:** histórico com comprador, data, itens (com tamanho) e valor.

### Comprador
- **Vitrine:**
  - busca por nome, sem diferenciar acentos;
  - **filtro por categoria**;
  - painel **"Filtrar e ordenar"**: preço, intervalo de anos, cor, marca e tamanho disponível; ordenação por nome, preço, ano, mais vendidas e lançamentos;
  - selo de "Últimas N" e de **"Esgotada"**.
- **Detalhe da camisa:** escolha do **tamanho** (tamanhos esgotados ficam riscados), quantidade limitada ao estoque e **guia de tamanhos**.
- **Carrinho:** uma linha por camisa e tamanho, −/+, remover, total e selo com a quantidade na aba.
- **Pedidos:** histórico de compras com número, data, status, itens e total.
- **Sem acesso à manutenção de produtos:** o app não mostra essas telas ao comprador, e a API recusa as tentativas com 403.

---

## Requisitos da disciplina

| Requisito | Como foi atendido |
|---|---|
| React Native, Expo, JavaScript | `mobile/`: Expo SDK 58, apenas arquivos `.js` |
| API em Java | `backend/`: Spring Boot 3.5, Java 17 |
| E-commerce com dois perfis | Loja de camisas; VENDEDOR e COMPRADOR escolhidos no cadastro, cada um com suas abas |
| CRUD completo de produtos | `POST`, `GET`, `PUT` e `DELETE` em `/api/produtos`. Telas `MeusProdutosScreen` e `ProdutoFormScreen` |
| Filtro por categorias | `GET /api/produtos?categoriaId=` e chips de categoria na `VitrineScreen` |
| Upload de imagem | `POST /api/produtos/{id}/imagem` (multipart). Galeria e câmera no `ProdutoFormScreen` |
| Integração com a API Java | `mobile/src/services/` (Axios + token JWT) |
| Vendedor: manutenção, CRUD, upload e histórico de vendas | Abas Painel, Produtos e Vendas (`screens/vendedor/`) |
| Comprador: comprar, carrinho e histórico | `ProdutoScreen`, `CarrinhoScreen` + `CarrinhoContext` e `PedidosScreen` |
| Comprador não faz manutenção | `SecurityConfig` (403) + `AutorizacaoPorPerfilTest`. A navegação por perfil (`RootNavigator`) não expõe as telas ao comprador |
| Módulos distintos e boas práticas | Backend em camadas, app separado por responsabilidade (ver [Estrutura](#estrutura-do-repositório)) e decisões abaixo |

---

## Requisitos funcionais (RF) e onde estão

| RF | Descrição | Backend | Mobile | Situação |
|---|---|---|---|---|
| RF01 | Cadastro com nome, e-mail, senha e perfil | `AuthController`, `AuthService`, `RegistroRequest` | `CadastroScreen` | ✅ |
| RF02 | Login e redirecionamento conforme o perfil | `AuthService`, `TokenService` | `LoginScreen`, `AuthContext`, `RootNavigator` | ✅ |
| RF03 | Logout | (token sem estado) | `PerfilScreen`, `AuthContext` | ✅ |
| RF04 | CRUD de produtos pelo vendedor | `ProdutoController`, `ProdutoService` | `MeusProdutosScreen`, `ProdutoFormScreen` | ✅ |
| RF05 | Upload de imagem do produto | `ImagemService`, `POST /api/produtos/{id}/imagem` | `ProdutoFormScreen`, `vendedorService` | ✅ |
| RF06 | Painel do vendedor (produtos, vendas, faturamento) | `VendaService`, `VendedorController` | `PainelScreen` | ✅ |
| RF07 | Histórico de vendas do vendedor | `VendaService`, `GET /api/vendedor/vendas` | `VendasScreen` | ✅ |
| RF08 | Vendedor só altera os próprios produtos | `ProdutoService.buscarDoVendedor` (403) | — | ✅ |
| RF09 | Catálogo com filtro por categoria | `CatalogoService`, `ProdutoRepository` | `VitrineScreen` | ✅ (e mais filtros) |
| RF10 | Busca por nome | `CatalogoService`, `TextoBusca` | `VitrineScreen` | ✅ |
| RF11 | Personalização com nome (até 12 letras) e número (0 a 99), por +R$ 25 | `PersonalizacaoService`, `Personalizacao`, `GET /api/config/personalizacao` | — | ⚠️ **Parcial:** a API aceita e valida, mas a tela foi adiada pelo grupo |
| RF12 | Carrinho de compras | — | `CarrinhoContext`, `CarrinhoScreen` | ✅ |
| RF13 | Finalizar compra (estoque e total no servidor) | `PedidoService`, `POST /api/pedidos` | `CarrinhoScreen`, `pedidoService` | ✅ |
| RF14 | Histórico de compras | `GET /api/pedidos/meus` | `PedidosScreen` | ✅ |
| RF15 | Comprador não pode manter produtos | `SecurityConfig`, `RespostasSeguranca` | navegação por perfil | ✅ |

---

## Endpoints da API

As rotas protegidas exigem o cabeçalho `Authorization: Bearer <token>`, recebido no login.

| Método | Rota | Acesso | Para quê |
|---|---|---|---|
| POST | `/api/auth/registro` | público | criar conta |
| POST | `/api/auth/login` | público | entrar (devolve o token) |
| GET | `/api/categorias` | público | categorias |
| GET | `/api/produtos` | público | vitrine. Parâmetros opcionais: `categoriaId`, `busca`, `precoMin`, `precoMax`, `anoMin`, `anoMax`, `cores`, `marcas`, `tamanho`, `ordenar` |
| GET | `/api/produtos/filtros` | público | opções do painel de filtros |
| GET | `/api/produtos/{id}` | público | detalhe |
| POST | `/api/produtos` | vendedor | criar produto |
| PUT | `/api/produtos/{id}` | vendedor dono | editar |
| DELETE | `/api/produtos/{id}` | vendedor dono | excluir (lógico) |
| POST | `/api/produtos/{id}/imagem` | vendedor dono | enviar foto (multipart, campo `imagem`, até 5 MB) |
| POST | `/api/produtos/{id}/imagem-catalogo` | vendedor dono | usar uma foto do catálogo |
| GET | `/api/vendedor/produtos` | vendedor | meus produtos |
| GET | `/api/vendedor/vendas` | vendedor | histórico de vendas |
| GET | `/api/vendedor/resumo` | vendedor | números do painel |
| GET | `/api/vendedor/fotos-catalogo` | vendedor | catálogo de fotos (`busca`, `categoriaId`) |
| POST | `/api/pedidos` | comprador | finalizar compra (itens com `produtoId`, `tamanho`, `quantidade`) |
| GET | `/api/pedidos/meus` | comprador | histórico de compras |
| GET | `/api/config/personalizacao` | público | valor e limites da personalização |

Erros sempre voltam no mesmo formato, com mensagem em português:

```json
{ "status": 400, "mensagem": "Dados inválidos. Verifique os campos.", "campos": { "ano": "Informe o ano da camisa." }, "timestamp": "..." }
```

---

## Decisões técnicas e observações

**Segurança**
- Senhas com **BCrypt**.
- Login com **JWT**: o perfil vai no token, e as permissões são conferidas **no servidor**, não só no app.
- O segredo do JWT fica fora do código (variável `JWT_SECRET`).

**Compra e estoque**
- O preço e o total do pedido são calculados pelo servidor.
- A compra roda numa **transação com trava da linha do produto**, para duas compras simultâneas não venderem a mesma última unidade.
- Se um item falha, nada é gravado.

**Produtos**
- **Exclusão lógica:** o produto sai da vitrine, mas os pedidos antigos continuam íntegros.
- **Estoque por tamanho:** grade única P, M, G, GG e XGG, com medidas no guia de tamanhos do app (GG = XL e XGG = 2XL).
- **Várias categorias por camisa:** ex.: "Retrô Brasil 2002" é Retrô e também Seleções.
- **Catálogo de fotos (`backend/catalogo-fotos`, ~93 MB):**
  - 1.294 fotos em 640 px, com miniaturas e nome, ano e categorias sugeridos;
  - ao escolher uma foto, o cadastro já vem preenchido;
  - o backend só aceita arquivos que estão no catálogo.

**Banco e configuração**
- **Migrações automáticas** (`MigracoesBanco`): bancos de versões anteriores são atualizados sem perder dados, como a categoria única virando várias e o estoque único virando estoque por tamanho.
- **Configuração do app num lugar só:** o endereço da API sai do `mobile/.env.local` ou, sem ele, do padrão em `src/config.js`.

**Visual e experiência**
- Tema escuro "Arquibancada", com fontes Manrope e DM Mono, centralizado em `src/theme.js`.
- Os botões principais ficam fixos embaixo da tela, na zona do polegar.
- Os componentes têm rótulos de acessibilidade.

**Fora do escopo:** pagamento real, frete, login social e avaliações.

**Histórico do git:** a base do app mobile entrou no commit `148851c`, junto com o `.gitignore`. Depois disso, os commits do mobile estão separados.
