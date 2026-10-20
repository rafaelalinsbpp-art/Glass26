# Glass26 para Android

Projeto Android independente inspirado em interfaces translúcidas modernas, voltado ao Samsung Galaxy A54. Não é iOS, não é produto da Apple e não substitui a One UI.

## Módulos

- Tela inicial (launcher) com aplicativos Android originais, relógio e bateria
- Tema claro, escuro ou automático, tamanhos de fonte e espaçamento de ícones configuráveis
- Central de notificações com cartões estilizados; acesso opcional autorizado na configuração do sistema
- Central de controle que abre as configurações oficiais do Android
- Papel de parede selecionável e prévia não invasiva de bloqueio
- Teclado local (IME) ativável nas configurações de teclado; pode ser selecionado no WhatsApp original
- Ilha sobreposta opcional, somente relógio e bateria, ativada manualmente

## Limites e privacidade

O Glass26 **não modifica o código nem a interface interna** do WhatsApp, Instagram, aplicativos bancários, apps Samsung ou do sistema. Também não assume o lugar da barra de notificações ou da tela de bloqueio protegida da Samsung. Não usa root nem as permissões de acessibilidade para imitar privilégios de sistema.

A central de notificações exige autorização **separada e opcional**. Os dados são mantidos na memória do processo, sem armazenamento permanente e sem conexão de rede. Notificações também podem conter informações sensíveis: antes de conceder acesso, leia o aviso do aplicativo. O Android mantém o controle para revogar essa permissão.

O teclado não pede acesso à Internet. Não há serviços remotos ou coleta de texto digitado.

## Compilar e verificar

Abra a aba [Actions](https://github.com/rafaelalinsbpp-art/Glass26/actions) e procure **Glass26 Android - build, assinatura e teste**. A automação executa:

1. Compilação com Android Gradle Plugin/SDK Android 35 e Java 17
2. Integridade do pacote ZIP e assinatura da variante debug
3. Identificação do pacote `com.glass26.app` e cálculo SHA-256
4. Tentativa de instalação, inicialização, teste de ausência de falhas fatais e captura de tela em emulador Android 15

O APK **debug** é assinado pela chave de depuração gerada no runner, não por uma chave de produção persistente. Isso significa que a mesma assinatura pode não ser preservada em builds futuros e uma instalação posterior pode exigir desinstalação (o que apaga os dados locais do aplicativo). Para distribuição estável, deve-se criar uma chave de release própria, guardar fora do repositório e usar assinatura reproduzível em um fluxo seguro.

Um teste em emulador não substitui testes no Galaxy A54 físico, especialmente os recursos de sobreposição, IME, tema do fabricante e notificação. **Não afirmar ausência total de bugs nem fidelidade de 100% ao iOS.**

## Instalação

Após a execução integral dos testes, baixe o artefato do APK assinado na página do workflow. Verifique a assinatura e o SHA-256 antes da instalação. Ative a instalação por aquela fonte apenas se você confiar nela e desative novamente após concluir.

Se houver uma versão previamente instalada com outra assinatura, poderá aparecer "App não instalado" ou "Pacote conflitante". Nessa situação, não desative proteções do sistema; verifique assinatura, versão do Android e o aplicativo existente.

