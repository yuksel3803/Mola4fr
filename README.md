# MOLA4FR

Application Android en français pour la voiture : TV/IPTV, radio, podcasts, YouTube, navigateur et recopie d'écran, utilisables avec Android Auto.

**[⬇ Télécharger la dernière version (APK)](https://github.com/yuksel3803/Mola4fr/releases/latest/download/MOLA4FR.apk)** · [Notes de version](NOTES_DE_VERSION.md) · [Toutes les versions](https://github.com/yuksel3803/Mola4fr/releases)

## Fonctions

- **TV / IPTV** : chaînes françaises gratuites par défaut (liste publique [iptv-org](https://github.com/iptv-org/iptv)) et ajout de vos propres listes M3U avec le bouton **+**.
- **Radio** : radios françaises en tête (annuaire Radio Browser), recherche par pays, popularité et étiquettes, ajout de vos propres flux.
- **Podcasts** : recherche, abonnements et reprise de lecture là où vous vous êtes arrêté.
- **YouTube et navigateur** web.
- **Recopie de l'écran** du téléphone sur l'écran de la voiture (avec l'autorisation d'Android à chaque fois).
- **Touches au volant** pour changer de chaîne, de station ou d'épisode.
- **Commandes vocales en français** : « mets la radio France Inter », « chaîne suivante », « pause », « ouvre YouTube »…
- **Accueil vocal** facultatif quand la voiture se connecte.
- Pas de notifications publicitaires, pas de mise à jour en arrière-plan.

## Installation

MOLA4FR fonctionne sur les téléphones Android 9 ou plus récents équipés d'un processeur 64 bits (la quasi-totalité des téléphones récents).

1. Télécharge `MOLA4FR.apk` avec le lien ci-dessus, puis ouvre-le sur ton téléphone.
2. Si Android le demande, autorise l'installation depuis ton navigateur ou ton gestionnaire de fichiers.
3. Ouvre MOLA4FR une fois sur le téléphone.

### Faire apparaître MOLA4FR dans Android Auto

Android Auto masque par défaut les applications qui ne viennent pas du Play Store. Pour l'autoriser :

1. Sur le téléphone, ouvre les **paramètres d'Android Auto**.
2. Touche **Version** une dizaine de fois, puis accepte d'activer les paramètres pour développeurs.
3. Dans le menu **⋮ → Paramètres pour les développeurs**, coche **Sources inconnues**.
4. Reconnecte le téléphone à la voiture.

La radio et les podcasts fonctionnent généralement ainsi. La vidéo, YouTube et la recopie d'écran peuvent rester masquées selon le téléphone et la version d'Android Auto : aucune garantie n'est possible.

## Sécurité routière

N'utilise la vidéo, YouTube, le navigateur et la recopie d'écran **qu'à l'arrêt**. Le conducteur reste responsable du respect du Code de la route.

## Mises à jour

Dans l'application : **Paramètres → Interface → Mises à jour de l'application**. La recherche se fait uniquement quand l'application est ouverte, et l'installation demande toujours ta confirmation.

## Pour les développeurs

Java 21, Android SDK 36 et le wrapper Gradle :

```bash
./gradlew :fermata:testMobileDebugUnitTest :fermata:testAutoDebugUnitTest
./gradlew :fermata:packageAutoDebugUniversalApk
```

Publier une version :

1. Mettre à jour `appVersionName` et `appVersionCode` dans `gradle/libs.versions.toml`, et `NOTES_DE_VERSION.md`.
2. Créer le tag correspondant : `git tag v1.0.0 && git push origin v1.0.0`.
3. Le workflow **Publish app update** compile, signe et publie l'APK. Il a besoin du secret `MOLA4FR_KEYSTORE_BASE64`.

Voir aussi le [rapport d'audit](AUDIT_FR.md).

## Licence et crédits

MOLA4FR est dérivée de [Mola](https://github.com/moris545454/araba-), elle-même dérivée de [Fermata Media Player](https://github.com/AndreyPavlenko/Fermata) par Andrey Pavlenko et ses contributeurs. Elle est distribuée sous licence **GPL-3.0** : le code source reste public et les mentions de droits d'auteur sont conservées. Voir [LICENSE](LICENSE) et [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
