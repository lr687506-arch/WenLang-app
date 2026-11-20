# WenLang Android app

Este repositório contém o aplicativo Android nativo WenLang, desenvolvido em Kotlin e Jetpack Compose.

## Visualizar o aplicativo

O projeto é um aplicativo Android nativo. A página do Google AI Studio pode exibir o projeto e oferecer o download do APK, mas não substitui a execução da interface Android em um emulador.

Para visualizar a interface:

1. Abra a pasta do projeto no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Abra Device Manager e crie/inicie um emulador Android (ou conecte um celular com depuração USB).
4. Selecione a configuração app e clique em Run.

## Gerar APK localmente

Pré-requisitos: Android Studio com Android SDK, JDK 17 e Gradle 9.3.1.

Na raiz do projeto, execute:

    gradle --no-daemon :app:assembleDebug

O APK de depuração será gerado em:

    app/build/outputs/apk/debug/app-debug.apk

Não instale um arquivo com extensão .apk se ele não tiver sido gerado por uma build concluída com sucesso.

## Build automático

O GitHub Actions executa uma build de depuração em pushes, pull requests e manualmente. Quando a execução terminar com sucesso, baixe o artefato wenlang-debug-apk na seção Artifacts da execução.

## Configuração de serviços

Crie um arquivo .env na raiz e configure as chaves necessárias conforme .env.example. A ausência de credenciais de serviços pode limitar funcionalidades online, mas não deve ser confundida com a geração/instalação do APK.