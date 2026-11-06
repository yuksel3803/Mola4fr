# Audit du dépôt Mola (moris545454/araba-)

> Ce rapport décrit le dépôt d'origine avant la création de MOLA4FR. La plupart des points ont été traités depuis : version et tag alignés, APK servis en assets de release, README à jour, JDK 21, dépôt nettoyé.

Audit réalisé le 6 novembre 2026 sur `main` @ `3f45ad3` (le même commit que le tag `v4.1.10`).

## 1. Critique — la release 4.1.10 ne correspond pas au code source

| Élément | Valeur constatée |
|---|---|
| Tag `v4.1.10` | pointe sur `3f45ad3`, identique à `main`, `mola-3.1` et `mola-3.2` |
| `gradle/libs.versions.toml` à ce commit | `appVersionName = "2.2.2"`, `appVersionCode = "323"` |
| `distribution/update-auto-debug.json` | annonce 2.2.2 (323) |
| Notes de la release 4.1.10 | « Tam ekran modu, sesli karşılama özelleştirme ve APK imza koruması » |

Conséquences :

- **Licence GPL-3.0** : Mola dérive de Fermata (GPL-3.0). Distribuer l'APK 4.1.10 (ou 2.2.6, 3.x) oblige à publier la *source correspondante*. Aujourd'hui, le code des fonctions annoncées (mode plein écran, personnalisation du message vocal, protection de signature) n'est pas visible sur le dépôt. Il faut pousser les commits qui ont produit ces APK, puis recréer les tags dessus.
- **Updater intégré** : `GithubUpdater` lit `releases/latest`. Le workflow `publish-update.yml` refuse un tag différent de `v<appVersionName>` ; la release 4.1.10 a donc été créée à la main, hors de ce contrôle. Si elle ne contient pas `update-auto-debug.json`, l'application retombe sur `distribution/update-auto-debug.json` du tag, qui annonce 2.2.2 : les utilisateurs ne voient pas la nouvelle version.
- **Numérotation** : 2.2.2 → 2.2.6 (park) → mola-3.0/3.1/3.2 → 4.1.10, avec deux schémas de tags (`v*` et `mola-*`). À unifier sur `vX.Y.Z`.

**Correctif ajouté** : `.github/workflows/release-guard.yml` vérifie à chaque release publiée que le tag = `v<appVersionName>`, que l'APK et le manifeste sont présents et que le `versionCode` du manifeste correspond à la source.

**À faire de ton côté** :
1. Pousser le code source de 4.1.10 sur `main` (avec `appVersionName = "4.1.10"` et un `appVersionCode` > 323).
2. Supprimer puis recréer le tag `v4.1.10` sur ce commit (`git tag -f v4.1.10 && git push -f origin v4.1.10`), ce qui relance `publish-update.yml`.
3. Supprimer les tags `mola-3.1` et `mola-3.2` qui pointent sur du code 2.2.2.

## 2. Important — distribution des APK via Git LFS

Le README, `docs/index.html` et l'updater (`distribution: "repository-lfs"`) téléchargent des APK d'environ 160 Mo depuis Git LFS (`media.githubusercontent.com`). Le quota de bande passante LFS gratuit de GitHub est limité (de l'ordre de 1 Go/mois) : quelques téléchargements suffisent à le dépasser, et les liens cassent pour tout le monde.

Recommandation : servir les APK uniquement comme **assets de release** (bande passante non comptée), mettre `"distribution": "release"` dans le manifeste, et faire pointer le README et la page `docs/` vers `https://github.com/moris545454/araba-/releases/latest/download/FermataX-yeni-arayuz-debug.apk`.

## 3. Important — README et page de téléchargement obsolètes

- Le README annonce « 2.1.7 APK’sını indir » et « Mola 2.2.2 » alors que la dernière release est 4.1.10.
- Les sections sur 2.1.4/2.1.5 relèvent de l'historique : à déplacer dans `UPDATE_RELEASE_NOTES_TR.md`.
- Un lien pointe vers une page hébergée sur `chatgpt.site` (tiers). La page `docs/index.html` du dépôt peut être publiée via GitHub Pages et devenir le lien officiel.
- Les notes de release sont générées depuis `UPDATE_RELEASE_NOTES_TR.md` entier : mettre la version courante en tête est indispensable avant chaque tag.

## 4. Moyen — build et CI

- **Java** : `ci.yml` utilisait JDK 17 alors que le README et les deux autres workflows utilisent JDK 21. **Corrigé** (JDK 21).
- `build-test-apk.yml` ne se déclenche que sur la branche `feature/tv-user-playlists` : à supprimer ou à passer en `workflow_dispatch` seul.
- Le paquet s'appelle `me.app.fermataX.auto.debug` et l'APK `FermataX-yeni-arayuz-debug.apk`, signés avec un *debug keystore* stable (en secret). Fonctionnel, mais déroutant et risqué : un vrai keystore de release et un nom d'APK `Mola-<version>.apk` seraient plus clairs. Attention : changer la signature ou le paquet oblige les utilisateurs à désinstaller ; à faire en une seule fois, en le documentant.
- Aucune clé ni fichier secret n'est commité (vérifié).

## 5. Moyen — sécurité et application

- `android:usesCleartextTraffic="true"` est global. Il est justifié pour les flux IPTV HTTP, mais un `network_security_config.xml` peut réserver le HTTP aux flux média et imposer HTTPS au reste (updater, API).
- `UpdateCheckJob` reste déclaré dans le manifeste alors que les vérifications en arrière-plan ont été supprimées ; il est seulement annulé au démarrage. Il peut être retiré après une ou deux versions.
- L'updater est bien conçu : HTTPS obligatoire, taille limitée, SHA-256, vérification du paquet et de l'URL de l'asset.

## 6. Faible — organisation du dépôt

- Racine encombrée : 9 fichiers `.md` (dont `MASTER_CONTEXT.md`, 66 Ko), `open-dhu.bat`, `extracted_logo/`, `promo/`, `design/`. Regrouper la documentation dans `docs/` et les supports marketing dans `promo/`.
- Pas de modèles d'issue (`.github/ISSUE_TEMPLATE`) : un modèle « rapport de plantage » (modèle de téléphone, version Android/Android Auto, version Mola, logcat) aiderait pour des cas comme le crash de 2.1.4.
- Ajouter une version anglaise courte du README élargirait l'audience.

## 7. Sécurité routière

La « version park » est présentée comme « non utilisable en roulant ». Pour la vidéo, YouTube, le navigateur et la recopie d'écran, il est préférable d'appliquer ce blocage dans le code (état de stationnement fourni par Android Auto / CarUxRestrictions) plutôt que par un simple avertissement, et de le mentionner clairement dans le README.

## Changements inclus dans ce patch

- `.github/workflows/ci.yml` : JDK 17 → 21.
- `.github/workflows/release-guard.yml` : nouveau contrôle de cohérence des releases.
- `AUDIT_FR.md` : ce rapport.
