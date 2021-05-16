### Rechercher les serveurs SMB<br>
Scanner le réseau pour trouver le serveur SMB disponible<br>

### Nom du serveur/Adresse IP<br>
Précisez le nom du serveur SMB ou l'adresse IP <br>

### Protocole SMB<br>
Vous pouvez spécifier le protocole SMB.<br>

- Utiliser SMB V1<br>
- Utiliser SMB V2 (2.01)<br>
- Utiliser SMB V2 (2.11)<br>
- Utiliser SMB V2/3 (2.12)<br>
- Utiliser SMB V2/3 (2.14)<br>

### Saisir numéro de port<br>
Précisez le numéro de port s'il est différent de celui par défaut. S'il est laissé vide, SMBSync2 essaiera de se connecter aux numéros de port standard par défaut 139 et 445. <br>

### Utiliser un compte d'authentification<br>
Doit être rempli si le serveur le demande. <br>

### Nom d'utilisateur<br>
Nom de compte d'utilisateur pour se connecter à l'hôte. Un compte Microsoft ne peut pas être utilisé avec SMBSync2. Veuillez créer un compte local et l'utiliser. <br>

### Mot de passe<br>
Mot de passe du compte pour se connecter à l'hôte. <br>

### Afficher les dossiers partagés<br>
Se connecte à l'hôte spécifié et liste toutes les actions disponibles pour le compte. <br>

### Afficher les dossiers<br>
Lorsque vous appuyez sur le bouton de liste, les répertoires sélectionnables s'affichent, alors sélectionnez-le. Appuyez sur le nom du répertoire pour ouvrir un sous-répertoire.<br>

### Répertoire<br>
Vous pouvez saisir directement un nom de répertoire. Si vous spécifiez un répertoire inexistant, il sera créé lors de l'exécution de la synchronisation.<br>
%YEAR%, %MONTH%, %DAY% et %DAY-OF-YEAR% peuvent être utilisés comme noms de répertoires. Les mots-clés sont convertis en année, mois et jour de début de la synchronisation.<br>

### Ajouter au nom du dossier la date/heure à laquelle la photo/vidéo a été prise (le mot-clé sera remplacé par la date et l'heure à laquelle la photo/vidéo a été prise)<br>

**Ces options ne sont affichées que lorsque le type de synchronisation est Copier/Déplacer.**<br>
Si elles sont vérifiées, les données EXIF des supports sauvegardés sont utilisées pour obtenir la date et l'heure du tournage. L'horodatage des tirs est utilisé et ajouté au dossier cible. Lorsque l'application ne parvient pas à obtenir l'heure de tir à partir des données EXIF, elle utilise la dernière heure modifiée du fichier. <br>

**<u>Les options suivantes ne sont affichées que lorsque le type de synchronisation est Archive.</u>**<br>

### Si la date et l'heure ne peuvent pas être déterminées depuis l'entête EXIF, afficher un message de confirmation<br>

Si la case est cochée, lorsque la date et l'heure de tournage ne peuvent pas être obtenues à partir du fichier EXIF, un message de confirmation s'affiche pour indiquer si l'heure de la dernière modification du fichier a été utilisée ou non. Si vous sélectionnez Annuler dans la boîte de dialogue de confirmation, le fichier ne sera pas archivé. <br>

### Archiver les fichiers si:<br>

Choisissez les critères de temps pour déterminer les fichiers à archiver. La sélection de l'heure est basée sur la date de prise de vue de la photo/vidéo, ou sur la dernière heure modifiée s'il n'est pas possible d'acquérir l'horodatage à partir de l'en-tête EXIF.<br>

- Toutes les dates<br>
 Archivez toutes les photos/vidéos<br>
- Plus ancien que 7 jours<br>
 N'archivez que les fichiers dont la date de tournage est antérieure de 7 jours ou plus à l'heure actuelle.<br>
- Plus ancien que 30 jours<br>
 N'archivez que les fichiers dont la date de tournage est antérieure de 30 jours ou plus à l'heure actuelle.<br>
- Plus ancien que 60 jours<br>
 N'archivez que les fichiers dont la date de tournage est antérieure de 60 jours ou plus à l'heure actuelle.<br>
- Plus ancien que 90 jours<br>
 N'archivez que les fichiers dont la date de tournage est antérieure de 90 jours ou plus à l'heure actuelle.<br>
- Plus ancien que 180 jours<br>
 N'archivez que les fichiers dont la date de tournage est antérieure de 180 jours ou plus à l'heure actuelle.<br>
- Plus ancien que 1 année<br>
 N'archivez que les fichiers dont la date de tournage est antérieure d'un an ou plus à l'heure actuelle. <br>

### Renommer les fichiers lors de l'archivage et les stocker dans le répertoire spécifié.<br>

If checked, the file will be renamed when it is archived. You can use the "File name template" to add date and time to the file name. You can also create a directory to store the files. To store files in a directory, enable "Save to directory when archiving". <br>

### Incrémenter les noms de fichiers<br>

Add an order number to the file name when archiving.<br>

- Ne pas changer<br>
Ne pas ajouter un numéro de séquence.<br>
- 2 chiffres (001-999)<br>
Appendice 01-99<br>
- 3 chiffres (001-999)<br>
Appendice 001-999<br>
- 4 chiffres (0001-9999)<br>
Appendice 0001-9999<br>
- 5 chiffres (00001-99999)<br>
Appendice 00001-99999<br>
- 6 chiffres (000001-999999)<br>
Appendice 000001-999999<br>

### Changer le nom du fichier avant de l'archiver dans le dossier cible <br>

Si cette case est cochée, le nom du fichier archivé sera modifié en fonction des critères que vous aurez choisis. Vous pouvez ajouter la date et l'heure dans le nom du fichier "Modèle de nom de fichier".<br>

- %Original name%<br>
Sera remplacé par le nom de fichier original lors de l'archivage des archives (Ex. DSC_0001)<br>
- %DAte%<br>
Sera remplacé par la date de tournage (ex. 2018-01-01)<br>
- %TIME%<br>
Sera remplacé par l'heure de tournage (ex. 13:01:10)<br>
La dernière ligne de l'écran indique à quoi ressembleront votre dossier cible et le nom du fichier une fois archivé.<br>

### Créer un dossier dont le nom comprend la date de capture des photos et y stocker les fichiers archivés.<br>

Crée un répertoire horodaté où stocker les fichiers archivés.<br>

### Modèle de répertoire<br>

Saisissez les modèles à utiliser pour renommer le répertoire à créer (exp. DIR-% ANNÉE% -% MOIS% et ainsi de suite). En appuyant sur les boutons des modèles, vous pouvez entrer des mots-clés derrière le curseur.<br>

- %YEAR%<br>
Sera remplacé par l'année de tournage. (Ex. 2018)<br>
- %MONTH%<br>
Sera remplacé par le mois de tournage (Ex. 01)<br>
- %DAY%<br>
Sera remplacé par le jour du tournage (Ex. 29)<br>

La dernière ligne de l'écran indique à quoi ressembleront votre dossier cible et le nom du fichier une fois archivé.<br>

### Manuels<br>
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_FR.htm)<br>
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_EN.htm) <br>
