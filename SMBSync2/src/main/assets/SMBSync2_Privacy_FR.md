## 1. Données enregistrées par l'application

L'application enregistrera une "liste de tâches de synchronisation" et, selon les paramètres, un "enregistrement d'activité de l'application". <span style="color: red; "><u>En outre, les données enregistrées ne seront pas envoyées par l'application, sauf si l'utilisateur les a manipulées.</u></span>

### 1.1. Liste des tâches de synchronisation

L'application enregistre les données nécessaires pour effectuer la synchronisation.

- Nom de répertoire, nom de fichier, nom d'hôte du serveur SMB, adresse IP, numéro de port, nom de compte, mot de passe (***1**)
- Mot de passe de l'application (***1**) pour protéger le lancement de l'application et la modification des paramètres
- Paramètres de l'application

***1** Le mot de passe est crypté avec un mot de passe généré par le système et stocké dans le Keystore d'Android.

### 1.2. Fiche d'activité de l'application

L'application doit enregistrer les données suivantes pour vérifier les résultats de la synchronisation et pour le dépannage.

- Version Android, fabricant du terminal, nom du terminal, modèle du terminal, version de l'application
- Nom du répertoire, nom du fichier, taille du fichier, date de la dernière modification du fichier
- Nom d'hôte du serveur SMB, adresse IP, numéro de port, nom de compte
- Nom de l'interface réseau, adresse IP
- Paramètres du système
- Paramètres de l'application

### 1.3. Paramètres exportés et liste des tâches de synchronisation

L'application peut exporter "1.1 Liste des tâches de synchronisation" vers un fichier. Vous pouvez protéger le fichier par un mot de passe avant de l'exporter.

- Nom du répertoire, nom du fichier
- Nom d'hôte du serveur SMB, adresse IP, numéro de port, nom de compte, mot de passe
- Paramètres de l'application

### 1.4. Envoyer des données enregistrées à partir de l'application

Les données enregistrées par l'application peuvent être envoyées en suivant ces étapes :

- Appuyez sur le bouton "Partager" de l'onglet "Historique
- Appuyez sur le bouton "Envoyer au développeur" à partir des "Infos système".
- Appuyez sur le bouton "Partager" ou "Envoyer au développeur" à partir de "Gérer les fichiers journaux".

## 2. Autorisations

L'application utilise les autorisations suivantes.

### 2.1 Photos/Médias/Fichiers

**read the contents of your USB storage**  
**modify or delete the contents of your USB storage**

Nécessaire pour la synchronisation des fichiers avec le stockage interne/externe/USB et pour les opérations de lecture/écriture sur les fichiers de données d'application.

### 2.2 Stockage

**read the contents of your USB storage**  
**modify or delete the contents of your USB storage**

Nécessaire pour la synchronisation des fichiers avec le stockage interne/externe/USB et pour les opérations de lecture/écriture sur les fichiers de données d'application.

### 2.3 Informations relatives à la connexion Wi-Fi

**view Wi-Fi connections**

Il est nécessaire de vérifier le statut du Wi-Fi (on/off) au début de la synchronisation.

### 2.4 Autres

### 2.4.1 view network connections

Nécessaire pour confirmer que l'appareil est connecté au réseau au début de la synchronisation.

### 2.4.2 connect and disconnect from Wi-Fi

Il est nécessaire d'activer / désactiver le Wi-Fi avant et après une synchronisation programmée.

### 2.4.3 full network access

Nécessaire pour effectuer la synchronisation du réseau en utilisant le protocole SMB.

### 2.4.4 run at startup

Nécessaire pour effectuer une synchronisation programmée.

### 2.4.5 control vibration

Obligation de notifier l'utilisateur par vibration à la fin de la synchronisation.

### 2.4.6 prevent device from sleeping

Nécessaire pour lancer la synchronisation à partir d'une application programmée ou externe.

### 2.4.7 install shortcuts

Nécessaire pour ajouter un raccourci de tâche de synchronisation sur l'écran d'accueil.
