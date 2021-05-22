## 1.Données collectées  
### 1.1.Données fournies par l'utilisateur à SMBSync2  

#### 1.1.1.Données à sauvegarder  
Les données suivantes seront sauvegardées dans l'application.  

- Informations sur les fichiers (nom du répertoire, nom du fichier)  
- Informations sur le serveur SMB si vous utilisez un serveur SMB (nom d'hôte/adresse IP, numéro de port, nom de compte(**<u>\*1</u>**), mot de passe de compte(**<u>\*1</u>**))  
- Informations sur le fichier ZIP en cas d'utilisation d'un fichier ZIP (méthode de compression, méthode de cryptage, mot de passe de cryptage(**<u>\*1</u>**)).  
- Options de paramétrage de l'application (messages d'avertissement, langue et taille de la police, etc.)  
- Mot de passe de l'application(**<u>\*1</u>**)  

**<u>\*1</u>**Les données sont cryptées et stockées.  

#### 1.1.2.Données à ne pas sauvegarder  
Les données suivantes ne seront pas sauvegardées.  

- Les mots de passe pour protéger les données dans "1.4. Envoi ou écriture de données en dehors de SMBSync2".  
Le mot de passe sera éliminé et ne sera pas sauvegardé lorsque le processus sera terminé.  

### 1.2.Résultat de l'exécution de SMBSync2  

Sauvegarder les données dans la zone de stockage de l'application afin que l'utilisateur puisse vérifier le résultat de l'exécution de SMBSync2.  

- Nom du répertoire, nom du fichier, état d'exécution  
- Taille des fichiers synchronisés, date et heure de mise à jour des fichiers.  
- Informations sur les erreurs  

### 1.3.Fiche d'activité de SMBSync2  

Lorsque l'enregistrement est activé, les données d'activité seront stockées dans la zone de mémoire de l'application pour la vérification des résultats d'exécution de l'application et pour l'assistance technique. Si la journalisation est désactivée, l'enregistrement des données sera arrêté, mais les données déjà enregistrées ne seront pas supprimées.  

- Informations sur le dispositif (nom du fabricant, nom du modèle, version du système d'exploitation, point de montage, répertoire spécifique à l'application, StorageAccessFramework, gestionnaire de stockage, adresse IP, activation/désactivation du WiFi, vitesse de la liaison WiFi)  
- Version de SMBSync2, options d'exécution de SMBSync2  
- Nom du répertoire, nom du fichier, état d'exécution  
- Taille des fichiers synchronisés, date et heure de mise à jour des fichiers.  
- Informations sur le débogage  
- Informations sur les erreurs  

### 1.4 Envoi ou écriture de données en dehors de SMBSync2  

Les données de SMBSync2 ne peuvent être envoyées ou écrites vers l'extérieur que si l'utilisateur l'actionne.  

- Appuyez sur le [bouton Partager] dans l'onglet Historique.  
- Cliquez sur le bouton "Envoyer au développeur" à partir des informations système.  
- Cliquez sur le bouton "Envoyer au développeur" à partir de la gestion du journal.  
Si vous spécifiez un mot de passe, le fichier joint sera protégé par un mot de passe.  
- En exécutant "Exporter la configuration" à partir du menu, "1.1.Données fournies par l'utilisateur à SMBSync2" sera exporté.  

### 1.5.supprimer les données stockées dans SMBSync2  

En désinstallant SMBSync2, "1.1. Données fournies par les utilisateurs à SMBSync2" et "1.3. Enregistrement des activités de SMBSync2" seront supprimés de l'appareil.  
<span style="color : red ;"><u>Cependant, les informations suivantes ne seront pas supprimées, veuillez donc supprimer le répertoire "/storage/emulated/0/SMBSync2" et les fichiers enregistrés dans le gestionnaire de fichiers. </u></span>.  

- "1.2.Résultat de l'exécution de SMBSync2"  
- Message affiché (environ 5000 lignes)  
- Informations sur la sauvegarde de la liste des tâches  
- Date et heure de la mise à jour du fichier (si le modèle ne permet pas le paramétrage).  
- Informations sauvegardées dans le stockage externe lors de la sauvegarde de la liste des tâches.  

### 2.les autorisations requises pour exécuter l'application.  

### 2.1.Photos, médias, fichiers  
**Lisez le contenu de votre stockage USB**.  
**modifier ou supprimer le contenu de votre stockage USB**.  
Utilisé pour la synchronisation des fichiers et la lecture/écriture des fichiers de gestion.  

### 2.2.Informations sur la connexion Wi-Fi  
**voir les connexions Wi-Fi**.  
Utilisé pour vérifier l'état du Wi-Fi lorsque la synchronisation commence.  

### 2.3.Autres  
### 2.3.1.View network connections  
Utilisez cette option pour vérifier les connexions réseau lorsque la synchronisation est lancée.  
### 2.3.2.connect and disconnect from Wi-Fi  
Cette fonction est utilisée pour activer/désactiver le Wi-Fi pour la synchronisation programmée sur Andoid 5/6/7/8/9.  
### 2.3.3.Full network access  
Il est utilisé pour synchroniser via le protocole SMB à travers le réseau.  
### 2.3.4.Run at startup  
Utilisé pour effectuer une synchronisation programmée.  
### 2.3.5.Control vibration  
Ceci est utilisé pour notifier l'utilisateur lorsque la synchronisation est terminée.  
### 2.3.6.Prevent device from sleeping  
Utilisé pour démarrer la synchronisation à partir d'une planification ou d'une application externe.  
### 2.3.7.Install shortcuts  
Permet d'ajouter un raccourci de démarrage de la synchronisation sur le bureau.  
