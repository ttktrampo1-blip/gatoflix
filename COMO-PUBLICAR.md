# Publicar o APK do Gato Flix

O worker já está apontado para:

    https://github.com/ttktrampo1-blip/gatoflix/releases/latest/download/gato-flix.apk

Ou seja: se o repositório for **ttktrampo1-blip/gatoflix**, não existe nada para configurar. Basta subir o projeto e rodar o build uma vez.

O app é um invólucro WebView: abre o site em tela cheia, com ícone próprio, sem barra de navegador.

## Jeito rápido (o worker faz tudo)

1. Abra `/publicar` no worker.
2. Crie um token em https://github.com/settings/tokens/new com os escopos **repo** e **workflow**.
3. Cole o token, deixe o nome `gatoflix` e clique em **Publicar projeto**.
4. O worker cria o repositório, envia os arquivos e dispara o build.
5. Confira `/apk/info` retornando `"ok": true` e baixe em `/apk`.

## Jeito manual

1. Crie o repositório **ttktrampo1-blip/gatoflix** (pode ser **privado**, mas aí veja a seção do token).
2. Descompacte o `__fonte.zip` no computador.
3. Na página inicial do repositório, clique em **Add file > Upload files** e arraste a pasta descompactada inteira.
4. Confirme em **Commit changes**. A pasta `.github` começa com ponto e pode ficar oculta no seu sistema — confirme que ela subiu.
5. Aba **Actions** → workflow **APK** → **Run workflow** (branch `main`).
6. Espere ~3 minutos. O workflow cria a release `apk` com o arquivo `gato-flix.apk`.
7. Teste em `/apk/info`: deve responder `"ok": true`.

## Repositório privado (opcional)

Defina a env `GITHUB_TOKEN` no Worker, com um token de leitura de Releases. Sem isso, repositório privado não entrega o arquivo.

## Trocar de repositório

Duas opções:

- Definir a env `APK_URL` no Worker com a URL do release (tem prioridade);
- Ou mudar a constante `APK_REPO` no código.

## Observações

- `GATO_SITE` no workflow muda o endereço que o app abre.
- A chave de assinatura fica no cache do Actions. Se o cache expirar, a assinatura muda e é preciso desinstalar a versão antiga antes de instalar a nova.
- Para trocar ícone ou banner: `app/src/main/res/drawable/icone.xml` e `banner.xml`.
