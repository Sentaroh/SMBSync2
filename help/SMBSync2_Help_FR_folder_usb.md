### Afficher les dossiers
Afficher la liste des répertoires sur le support USB. 
Lorsque vous appuyez sur le bouton "list directory", les répertoires sélectionnables sont affichés. Appuyez sur le répertoire que vous souhaitez choisir, puis sur le bouton "Sélectionner". Pour revenir aux sous-répertoires, appuyez sur le nom du répertoire. 

### Ajouter des mots-clés
%YEAR%, %MONTH%, %DAY% et %DAY-OF-YEAR% peuvent être utilisés comme noms de répertoires cibles. Les mots-clés sont convertis en année, mois et jour de début de la synchronisation. 

**Ces options ne sont affichées que lorsque le type de synchronisation est Copier/Déplacer.**

### Ajouter au nom du dossier la date/heure à laquelle la photo/vidéo a été prise (le mot-clé sera remplacé par la date et l\'heure à laquelle la photo/vidéo a été prise)
Si elles sont vérifiées, les données EXIF des supports sauvegardés sont utilisées pour obtenir la date et l'heure du tournage. L'horodatage des tirs est utilisé et ajouté au dossier cible. Lorsque l'application ne parvient pas à obtenir l'heure de tir à partir des données EXIF, elle utilise la dernière heure modifiée du fichier.

### Répertoire
Veuillez entrer le répertoire cible ou le répertoire principal. Si vous indiquez un répertoire qui n'existe pas dans la cible, il sera créé lors de la synchronisation. 

### Choisir le dossier sur le stockage USB
Sélectionnez le répertoire racine du support USB pour accéder au stockage USB. Si vous ne pouvez pas le sélectionner, veuillez envoyer les informations système au développeur avec "Menu -> System Info".
Le support USB ne peut être utilisé que s'il existe un point de montage (exp. /storage/0123-0123, etc.) correspondant à l'UUID du support USB. 

**Les options suivantes ne sont affichées que lorsque le type de synchronisation est Archive.**

### Si la date et l'heure ne peuvent pas être déterminées depuis l'entête EXIF, afficher un message de confirmation.
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

 