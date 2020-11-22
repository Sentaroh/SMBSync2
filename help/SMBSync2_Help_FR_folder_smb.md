### Rechercher les serveurs SMB

Scanner le réseau pour trouver le serveur SMB disponible

### Nom du serveur/Adresse IP

Précisez le nom du serveur SMB ou l'adresse IP 

### Protocole SMB

Vous pouvez spécifier le protocole SMB.

- Utiliser SMB V1

- Utiliser SMB V2 (2.01)

  SMB min v2.1, max v2.1

- Utiliser SMB V2 (2.11)

  SMB min v2.1, max v2.1

- Utiliser SMB V2/3 (2.12)

  Prise en charge du protocole SMB V2 ou V3

  SMB min v2.0.2, max v3.0

- Utiliser SMB V2/3 (2.14)

  SMB min v2.0.2, max v3.0

### Utiliser le protocole SMB2

Lorsqu'elle est cochée, elle force setProperty("jcifs.smb.client.useSMB2Negotiation", "true") dans la fonction JcifsAuth(). Cela garantit l'utilisation du style de négociation SMB2 non rétrocompatible, incompatible avec les versions antérieures à SMB 2.0.2

### Exiger l'authentification SMB IPC

Lors de la vérification, le client est tenu d'utiliser la signature SMB pour les connexions IPC$ comme transport DCERPC. Sinon, c'est autorisé, mais pas obligatoire. 

### Saisir numéro de port

Précisez le numéro de port s'il est différent de celui par défaut. S'il est laissé vide, SMBSync2 essaiera de se connecter aux numéros de port standard par défaut 139 et 445. 

### Utiliser un compte d'authentification

Doit être rempli si le serveur le demande. 

### Nom d'utilisateur

Nom de compte d'utilisateur pour se connecter à l'hôte. Un compte Microsoft ne peut pas être utilisé avec SMBSync2. Veuillez créer un compte local et l'utiliser. 

### Mot de passe

Mot de passe du compte pour se connecter à l'hôte. 

### Afficher les dossiers partagés

Se connecte à l'hôte spécifié et liste toutes les actions disponibles pour le compte. 

(Les noms des actions seront affichés lorsque vous appuierez sur le bouton " Afficher les dossiers partagés ". Cliquez sur le nom de l'action souhaitée, puis appuyez sur OK pour valider)

### Afficher les dossiers

Afficher la liste des annuaires sur l'action sélectionnée. 

Lorsque vous appuyez sur le bouton "Répertoire de la liste", les répertoires sélectionnables sont affichés. 

Appuyez sur l'annuaire que vous souhaitez choisir, puis sur le bouton "Sélectionner". Pour récapituler 

en passant par les sous-répertoires, appuyez sur le nom du répertoire. 

### Dossier

Veuillez entrer le répertoire cible ou le répertoire principal. Si vous indiquez un répertoire qui n'existe pas dans la cible, il sera créé lors de la synchronisation. 

### Ajouter des mots-clés

%YEAR%, %MONTH%, %DAY% et %DAY-OF-YEAR% peuvent être utilisés comme noms de répertoires cibles. Les mots-clés sont convertis en année, mois et jour de début de la synchronisation.

 

**Ces options ne sont affichées que lorsque le type de synchronisation est Copier/Déplacer.**

### Ajouter au nom du dossier la date/heure à laquelle la photo/vidéo a été prise (le mot-clé sera remplacé par la date et l'heure à laquelle la photo/vidéo a été prise)

Si elles sont vérifiées, les données EXIF des supports sauvegardés sont utilisées pour obtenir la date et l'heure du tournage. L'horodatage des tirs est utilisé et ajouté au dossier cible. Lorsque l'application ne parvient pas à obtenir l'heure de tir à partir des données EXIF, elle utilise la dernière heure modifiée du fichier.



**Les options suivantes ne sont affichées que lorsque le type de synchronisation est Archive.**

### Si la date et l'heure ne peuvent pas être déterminées depuis l'entête EXIF, afficher un message de confirmation

Si la case est cochée, lorsque la date et l'heure de tournage ne peuvent pas être obtenues à partir du fichier EXIF, un message de confirmation s'affiche pour indiquer si l'heure de la dernière modification du fichier a été utilisée ou non. Si vous sélectionnez Annuler dans la boîte de dialogue de confirmation, le fichier ne sera pas archivé. 

### Archiver les fichiers si:

Choisissez les critères de temps pour déterminer les fichiers à archiver. La sélection de l'heure est basée sur la date de prise de vue de la photo/vidéo, ou sur la dernière heure modifiée s'il n'est pas possible d'acquérir l'horodatage à partir de l'en-tête EXIF.

- Toutes les dates

  Archivez toutes les photos/vidéos

- Plus ancien que 7 jours

  N'archivez que les fichiers dont la date de tournage est antérieure de 7 jours ou plus à l'heure actuelle.

- Plus ancien que 30 jours

  N'archivez que les fichiers dont la date de tournage est antérieure de 30 jours ou plus à l'heure actuelle.

- Plus ancien que 60 jours

  N'archivez que les fichiers dont la date de tournage est antérieure de 60 jours ou plus à l'heure actuelle.

- Plus ancien que 90 jours

  N'archivez que les fichiers dont la date de tournage est antérieure de 90 jours ou plus à l'heure actuelle.

- Plus ancien que 180 jours

  N'archivez que les fichiers dont la date de tournage est antérieure de 180 jours ou plus à l'heure actuelle.

- Plus ancien que 1 année

  N'archivez que les fichiers dont la date de tournage est antérieure d'un an ou plus à l'heure actuelle. 

### Incrémenter les noms de fichiers

Ajoutez le numéro de séquence incrémentielle au nom du fichier lors de l'archivage.

- Ne pas changer

  Ne pas ajouter un numéro de séquence.

- 3 chiffres (001-999)

  Appendice 001-999

- 4 chiffres (0001-9999)

  Appendice 0001-9999

- 5 chiffres (00001-99999)

  Appendice 00001-99999

- 6 chiffres (000001-999999)

  Appendice 000001-999999 

### Changer le nom du fichier avant de l'archiver dans le dossier cible 

Si cette case est cochée, le nom du fichier archivé sera modifié en fonction des critères que vous aurez choisis. Vous pouvez ajouter la date et l'heure dans le nom du fichier "Modèle de nom de fichier". 

### Modèle de nom de fichier

Saisissez les modèles à utiliser pour renommer les fichiers archivés. La valeur par défaut est DSC_%DATE%_%TIME%

- %Original name%

  Sera remplacé par le nom de fichier original lors de l'archivage des archives (Ex. DSC_0001)

- %Date%

  Sera remplacé par la date de tournage (ex. 2018-01-01)

- %Time%

  Sera remplacé par l'heure de tournage (ex. 13:01:10) 

La dernière ligne de l'écran indique à quoi ressembleront votre dossier cible et le nom du fichier une fois archivé. 

### Créer un dossier dont le nom comprend la date de capture des photos et y stocker les fichiers archivés.

Crée un répertoire horodaté où stocker les fichiers archivés.

Lorsque cette option est cochée, le "modèle de nom de répertoire" s'affiche. 

### Modèle de répertoire

Saisissez les modèles à utiliser pour renommer le répertoire à créer (exp. DIR-% ANNÉE% -% MOIS% et ainsi de suite). En appuyant sur les boutons des modèles, vous pouvez entrer des mots-clés derrière le curseur.

- %Year%

  Sera remplacé par l'année de tournage. (Ex. 2018)

- %Month%

  Sera remplacé par le mois de tournage (Ex. 01)

- %Day%

  Sera remplacé par le jour du tournage (Ex. 29)

La dernière ligne de l'écran indique à quoi ressembleront votre dossier cible et le nom du fichier une fois archivé. 

### Informations détaillées

https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing

 