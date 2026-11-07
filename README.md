# 🍔 Burger House

Aplicativo Android completo para uma hamburgueria fictícia. Projeto funcional, com
persistência local (Room), UI moderna em Jetpack Compose (Material Design 3) e
arquitetura MVVM. Nenhum backend necessário: todos os dados são locais no dispositivo.

> **Nota:** este projeto é apenas um teste/portfólio. Fique à vontade para usá-lo
> como modelo ou ponto de partida para os seus próprios apps.

## Stack técnica

| Tecnologia | Uso |
|---|---|
| Kotlin | Linguagem |
| Jetpack Compose | UI declarativa |
| Navigation Compose | Navegação entre telas |
| Room | Persistência local (SQLite) |
| ViewModel + StateFlow | Gerenciamento de estado (MVVM) |
| Coil | Carregamento de imagens (Unsplash) |
| Material Design 3 | Tema e componentes |
| Coroutines | Assincronicidade |
| DataStore Preferences | Sessão e configurações |

## Requisitos

- Android 8.0 (API 26) ou superior
- Android Studio (Koala/Ladybug ou mais recente recomendado)
- JDK 17
- Conexão com a internet (apenas para carregar as imagens do cardápio)

## Como buildar e executar

```bash
# Opção 1 — Android Studio
# 1. Abra o Android Studio
# 2. File > Open... e selecione a pasta "burgerhouse"
# 3. Aguarde o Gradle Sync
# 4. Run ▶ (emulador ou dispositivo físico)

# Opção 2 — Terminal (com o Gradle Wrapper do Android Studio já gerado)
cd burgerhouse
./gradlew assembleDebug        # Gera o APK em app/build/outputs/apk/debug/
./gradlew installDebug         # Instala no dispositivo conectado
```

Cupons válidos para testar no carrinho/checkout:
`BURGER10` (10% off) • `PRIMEIRA15` (15% off) • `FRETEGRATIS` (frete grátis)

## Estrutura do projeto

```
burgerhouse/
├── settings.gradle.kts            # Repositórios e módulos
├── build.gradle.kts               # Plugins do projeto raiz
├── gradle.properties
├── gradle/libs.versions.toml      # Catálogo de versões
└── app/
    ├── build.gradle.kts           # Config do módulo app
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/burgerhouse/app/
        │   ├── BurgerHouseApp.kt          # ServiceLocator
        │   ├── MainActivity.kt            # Activity única
        │   ├── data/
        │   │   ├── local/
        │   │   │   ├── entity/            # Product, CartItem, Order, OrderItem, User, Coupon
        │   │   │   ├── dao/               # DAOs do Room
        │   │   │   └── database/          # BurgerHouseDatabase + DatabaseSeeder
        │   │   └── repository/            # BurgerRepository, UserPreferencesRepository
        │   ├── domain/model/              # Categorias, status, resumo do carrinho
        │   ├── viewmodel/                 # Auth, Menu, Cart, Checkout, Order, Loyalty, Profile
        │   └── ui/
        │       ├── theme/                 # Cores, tipografia, tema claro/escuro
        │       ├── components/            # ProductCard, ImageWithShimmer, OrderTimeline...
        │       ├── screens/               # splash, onboarding, login, home, productdetail,
        │       │                          # cart, checkout, tracking, history, loyalty, profile
        │       ├── navigation/            # Routes, BottomBar, AppNavigation, SnackbarController
        │       └── util/                  # Formatação em R$ e validações
        └── res/                           # strings, temas, cores, ícones
```

## Telas (11)

1. **Splash** — logo + fade-in, navegação automática após 2s.
2. **Onboarding** — 3 slides (cardápio, delivery, fidelidade) com "Pular" e indicador.
3. **Login/Cadastro** — nome, e-mail, telefone, endereço; validação e sessão (DataStore).
4. **Home** — header com usuário e carrinho (badge pulsante), busca com debounce,
   chips de categorias, carrossel de promoções e cards de produtos.
5. **Detalhe do Produto** — imagem grande, adicionais, ponto da carne, quantidade,
   observações e preço total em tempo real.
6. **Carrinho** — quantidades editáveis, remoção por swipe, cupom de desconto,
   subtotal/entrega/total.
7. **Checkout** — endereço, forma de pagamento (Dinheiro/Cartão/Pix), troco e resumo.
8. **Acompanhamento** — número do pedido, tempo estimado (35 min) e timeline animada
   com atualização automática a cada 15s: Recebido → Em preparo → Saiu para entrega → Entregue.
9. **Histórico** — pedidos anteriores com data, valor, status, detalhes e "Pedir novamente".
10. **Fidelidade** — pontos acumulados, barra de progresso (10 pts = hambúrguer grátis) e cupons.
11. **Perfil** — dados editáveis, notificações, tema claro/escuro, sobre e logout.

## Regras de negócio

- **Moeda**: formatação em Real (`R$`) via `NumberFormat` (pt-BR).
- **Fidelidade**: a cada R$ 50,00 em pedidos confirmados o cliente ganha 1 ponto;
  10 pontos = 1 hambúrguer grátis.
- **Cupons**: `BURGER10` e `PRIMEIRA15` (percentual), `FRETEGRATIS` (isentação da entrega).
- **Entrega**: taxa fixa de R$ 8,00 (zerada com cupom de frete grátis).
- **Seed**: na primeira execução o Room é populado com 18 produtos e 3 cupons.

## Identidade visual

- Primary `#FF6B35` (laranja burger) • Secondary `#2D2D2D` (grafite) • Background `#FFF8F0` (creme)
- Tema claro e escuro completos, com suporte opcional a cores dinâmicas (Material You).

## Screenshots esperados

| Tela | Descrição |
|---|---|
| Splash | Fundo laranja com o ícone 🍔 e o nome "Burger House" |
| Home | Header com avatar/carrinho, chips de categorias, carrossel de promoções e cards |
| Detalhe | Imagem grande, adicionais em chips, seletor de quantidade e botão com total |
| Carrinho | Itens com swipe-to-remove, resumo financeiro e campo de cupom |
| Tracking | Timeline com os 4 status e atualização automática |
| Fidelidade | Card de pontos com barra de progresso e lista de cupons |

---

Projeto de demonstração — "Burger House" é uma hamburgueria fictícia.
