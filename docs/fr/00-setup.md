# Mise en place de l'environnement de développement

Ce guide installe tous les outils nécessaires pour compiler, exécuter et tester l'application Android sur Windows. Aucune expérience Android préalable n'est supposée.

---

## Prérequis

| Outil | Version | Remarques |
|---|---|---|
| Java | 17 | Réutiliser l'installation existante de `spring-boot-tutorial` - ne pas installer un second JDK |
| IntelliJ IDEA Ultimate | Dernière version | Le plugin Android doit être activé |
| Android SDK | API 36 | Installé via `sdkmanager`, pas via Android Studio |
| Git | Toute version récente | Nécessaire pour le workflow de branches |

> **Pourquoi IntelliJ IDEA et pas Android Studio ?**
> IntelliJ IDEA Ultimate inclut le plugin Android complet et supporte aussi le backend Spring Boot dans la même instance d'IDE. Android Studio est une bonne alternative si vous ne travaillez que sur le mobile.

---

## 1. Installer le SDK Android

Android Studio installe le SDK automatiquement. Comme nous utilisons IntelliJ IDEA, nous installons le SDK séparément via les outils en ligne de commande.

### Télécharger les outils en ligne de commande

1. Aller sur [developer.android.com/studio#command-line-tools-only](https://developer.android.com/studio#command-line-tools-only) et télécharger le zip Windows.
2. Extraire vers `C:\Users\<vous>\AppData\Local\Android\Sdk\cmdline-tools\latest\`.
3. Le répertoire résultant doit contenir `bin\sdkmanager.bat` directement sous `latest\`.

### Installer les packages SDK nécessaires

Ouvrir PowerShell et exécuter :

```powershell
$env:ANDROID_HOME = "$env:USERPROFILE\AppData\Local\Android\Sdk"
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.1.0"
sdkmanager --licenses
```

Accepter tous les accords de licence. Le téléchargement prend quelques minutes.

### Définir les variables d'environnement de façon permanente

Dans Windows, ouvrir **Propriétés système - Variables d'environnement** et ajouter :

| Variable | Valeur |
|---|---|
| `ANDROID_HOME` | `C:\Users\<vous>\AppData\Local\Android\Sdk` |
| Ajouter à `PATH` | `%ANDROID_HOME%\platform-tools` |
| Ajouter à `PATH` | `%ANDROID_HOME%\cmdline-tools\latest\bin` |

Redémarrer PowerShell après l'enregistrement. Vérifier :

```powershell
adb version
sdkmanager --version
```

---

## 2. Configurer IntelliJ IDEA

1. Installer les plugins **Android** et **Kotlin** depuis le marketplace si ce n'est pas déjà fait (**Paramètres - Plugins**).
2. Définir le SDK du projet sur votre installation Java 17 existante : **Fichier - Structure du projet - Projet - SDK**.
3. Pointer IntelliJ vers le SDK Android : **Paramètres - Langages & Frameworks - SDK Android - Emplacement du SDK Android** - `C:\Users\<vous>\AppData\Local\Android\Sdk`.
4. Ouvrir la racine du projet comme projet Gradle.
5. Attendre que la synchronisation Gradle initiale se termine. La première synchronisation télécharge toutes les dépendances et peut prendre plusieurs minutes.

---

## 3. Créer un appareil virtuel Android (émulateur)

Un appareil virtuel permet d'exécuter l'application sans téléphone physique.

```powershell
# Télécharger une image système pour API 36
sdkmanager "system-images;android-36;google_apis;x86_64"

# Créer un appareil virtuel nommé Pixel9
avdmanager create avd -n Pixel9 -k "system-images;android-36;google_apis;x86_64"

# Démarrer l'émulateur
emulator -avd Pixel9
```

L'émulateur est prêt quand l'écran d'accueil Android apparaît.

> **Atteindre le backend depuis l'émulateur :** L'émulateur Android fonctionne dans un réseau isolé. `localhost` pointe vers l'émulateur lui-même, pas vers votre machine. Utiliser `10.0.2.2` pour atteindre un serveur qui tourne sur votre hôte (ex. `http://10.0.2.2:8080` pour le backend Spring Boot).

---

## 4. Connecter un appareil physique (optionnel)

Un appareil physique offre de meilleures performances pour les tests en conditions réelles.

1. Sur l'appareil : ouvrir **Paramètres - À propos du téléphone**, taper **Numéro de build** 7 fois pour activer les options développeur.
2. Ouvrir **Options développeur** et activer le **Débogage USB**.
3. Connecter l'appareil en USB et confirmer la demande d'autorisation sur l'écran de l'appareil.

Vérifier que l'appareil est reconnu :

```powershell
adb devices
```

Votre appareil doit apparaître avec le statut `device`.

---

## 5. Compiler et installer l'application

```powershell
# Depuis la racine du projet
.\gradlew app:assembleDebug
.\gradlew app:installDebug
```

> `assembleDebug` compile l'APK. `installDebug` le pousse en plus sur l'appareil connecté ou l'émulateur.

L'application requiert que le backend soit démarré. Lancer `spring-boot-tutorial` avant de lancer l'application.

---

## 6. Lancer les tests

Les tests unitaires s'exécutent sur la JVM - aucun appareil nécessaire :

```powershell
.\gradlew test
```

Les tests instrumentés s'exécutent sur un appareil connecté ou un émulateur :

```powershell
.\gradlew connectedDebugAndroidTest
```

---

## Dépannage

| Symptôme | Solution |
|---|---|
| `ANDROID_HOME` introuvable lors de la synchronisation Gradle | Définir la variable d'environnement et redémarrer IntelliJ |
| `adb: command not found` | Ajouter `%ANDROID_HOME%\platform-tools` à `PATH` |
| L'émulateur démarre mais reste noir | Attendre 60 secondes ; le démarrage à froid prend du temps au premier lancement |
| `Connection refused` quand l'application appelle l'API | Vérifier que le backend est démarré et utiliser `10.0.2.2`, pas `localhost` |
| `adb devices` affiche `unauthorized` | Reconnecter l'USB ; confirmer la demande sur l'appareil |
