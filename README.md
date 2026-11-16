# Painel Nexus V1

App Android (Kotlin + Jetpack Compose) com visual preto e vermelho neon.

## Fluxo

1. **Splash**: logo Painel Nexus + barra "carregando o painel" por 5 a 7 s (aleatório).
2. **Key**: logo no topo, "Digite a sua key para prosseguir", caixa de texto arredondada e botão vermelho com animação de clique (sem som). A key é o **nome do aplicativo**.
3. **Tela principal**: botão quadrado com 4 barras (canto superior esquerdo) abre o menu lateral:
   - **Home**: logo no topo + botão **Abrir flutuante**
   - **Permissões**: Shizuku, Exibir sobre outros apps, Notificações
   - **Atualizações**: "em breve" (Discord, YouTube) + aviso
4. **Flutuante**: começa só com a imagem (arrastável). Tocar na imagem abre o painel **PAINEL NEXUS V1** (sem funções, apenas teste). O botão ✕ fecha o painel e volta a imagem.

## Gerar o APK debug

**Android Studio:** `File > Open` na pasta, aguarde o sync e rode `Build > Build APK(s)`.
O APK sai em `app/build/outputs/apk/debug/app-debug.apk`.

**Terminal** (precisa de JDK 17 e Android SDK 34):

```
./gradlew assembleDebug
```

**GitHub Actions:** suba o projeto num repositório; o workflow `.github/workflows/build-debug-apk.yml` compila e publica o APK como artefato.

## Estrutura

```
app/src/main/java/com/nexus/painel/
  MainActivity.kt            fluxo Splash > Key > Principal
  data/KeyValidator.kt       validação da key
  permissions/ShizukuHelper.kt
  overlay/FloatingService.kt bolha + painel flutuante
  ui/theme/Theme.kt          cores e tema
  ui/components/NeonComponents.kt
  ui/screens/                Splash, Key, Main, Home, Permissions, Updates
```
