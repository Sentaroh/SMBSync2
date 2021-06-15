### Mode Test  

Lors de la vérification, aucun fichier n'est supprimé, copié ou écrasé. Aucune modification n'est apportée à vos fichiers, tant sur la cible que sur le maître. Veuillez utiliser le mode Test pour vérifier les fichiers qui seront supprimés/copiés/remplacés pendant la tâche de synchronisation réelle. Les opérations effectuées sur les fichiers apparaîtront dans l'onglet Messages.   

### Synchronisation Auto  

Si elle est cochée, la tâche peut être lancée automatiquement à des intervalles planifiés dans le programmateur. Les tâches qui sont réglées sur la synchronisation automatique démarrent lorsque vous appuyez sur le bouton de synchronisation en haut à droite de l'écran principal de l'application.   

### Nom de la tâche de synchronisation  

Indiquez un nom pour la tâche. Le nom de la tâche de synchronisation n'est pas sensible à la casse.   

### Mode de sync  

Les modes de synchronisation actuellement pris en charge sont Mirror, Copy, Move ou Archive. <span style="color: red; "><u>La synchronisation se fait dans une direction de la cible à partir du maître.</u></span>  

- Miroir  
Le dossier cible est conservé comme une copie exacte du dossier principal. Si un fichier est différent entre le dossier principal et le dossier cible, le fichier principal écrase le fichier cible. Les dossiers et fichiers non présents sur la cible sont copiés à partir du master. Les fichiers et dossiers qui n'existent pas sur le master sont également supprimés de la cible. Seuls les fichiers modifiés (par la taille et/ou la date/heure) sont mis à jour dans la cible.  
- Déplacer  
Si un fichier est différent entre le maître et la cible, le fichier sur le maître écrase le fichier sur la cible. Une fois copiés sur la cible, les fichiers et les dossiers sont supprimés du master (comme la commande move).
Seuls les fichiers modifiés (par la taille et/ou la date/heure) sont copiés dans la cible. Les fichiers identiques, sur la base des critères de comparaison sélectionnés, sont supprimés du fichier maître sans être copiés. Les fichiers et dossiers de la cible, non présents sur le master, sont évidemment préservés.  
- Copier  
Même chose que pour Move, mais les fichiers ne sont pas supprimés du master après avoir été copiés.
Si un fichier est différent entre le master et la cible, le fichier du master écrase le fichier de la cible. Une fois copiés sur la cible, les fichiers et les dossiers sont conservés sur le master (comme une commande de copie).
Seuls les fichiers modifiés (par la taille et/ou la date/heure) sont copiés vers la cible. Les fichiers identiques, sur la base des critères de comparaison sélectionnés, sont ignorés et ne sont pas copiés à nouveau.  
- Archiver  
Archivez des photos et des vidéos en les déplaçant du dossier principal au dossier cible. Des critères spécifiques aux médias peuvent être spécifiés pour l'archivage : date/heure de tournage, date et heure de la dernière exécution de l'archive (par exemple 7 jours ou plus tôt ou 30 jours ou plus tôt). Le ZIP ne peut pas être spécifié comme cible pour les opérations d'archivage.   

**Comparer les critères:**
Sur la base de ces critères, les dossiers sont considérés comme différents :  

1. Le nom du fichier/dossier n'existe que sur le maître ou la cible, et non sur les deux côtés  
2. Les fichiers ont des tailles différentes  
3. Les fichiers ont un horodatage différent (date et heure de la dernière modification)  
Consultez les options avancées ci-dessous pour obtenir des informations plus détaillées sur les critères de comparaison et les paramètres plus granulaires.  
### Inverser les dossiers source et destination  

Échangez les dossiers maître et cible : le maître devient la cible et la cible est changée en maître.   

### Dossier Maitre (Source)  

Appuyez sur l'icône/nom du stockage pour modifier le dossier principal.   

### Dossier Cible (Destination)  

Appuyez sur l'icône/nom du stockage pour modifier le dossier cible.   

### Sélectionner les fichiers à synchroniser  

Si cette option n'est pas cochée, tous les fichiers sont synchronisés. Si vous cochez le filtre de fichiers, vous obtenez les options suivantes :  

###Filtre sur les noms de fichiers  

- Synchro des fichiers audio  
Une fois vérifiée, la synchronisation inclura les fichiers avec les extensions suivantes :  
aac, aif, aifc, aiff, flac, kar, m3u, m4a, mid, midi, mp2, mp3, mpga, ogg, ra, ram, wav  
- Synchro des fichiers images  
Une fois vérifiée, la synchronisation inclura les fichiers avec les extensions suivantes :  
bmp, cgm, djv, djvu, gif, ico, ief, jpe, jpeg, jpg, pbm, pgm, png, pnm, ppm, ras, rgb, svg, tif, tiff, wbmp, xbm, xpm, xwd  
- Synchro des fichiers vidéo  
Une fois vérifiée, la synchronisation inclura les fichiers avec les extensions suivantes :  
avi, m4u, mov, mp4, movie, mpe, mpeg, mpg, mxu, qt, wmv   
- Filtre de fichiers  
Il s'agit d'un filtre personnalisé d'inclusion/exclusion de fichiers. Vous pouvez sélectionner le nom et l'extension des fichiers que vous voulez exclure ou inclure du processus de synchronisation.   

###Filtre sur la taille des fichiers  

Vous pouvez choisir les fichiers à synchroniser en fonction de leur taille.  

- Moins de / Plus de  
Vous pouvez spécifier n'importe quelle taille de fichier.  

###Filtre de la date de dernière modification du fichier  
Vous pouvez sélectionner un fichier en fonction de sa dernière date de modification.  

- Si vous spécifiez 1 jour plus vieux que, vous sélectionnez les fichiers dont la dernière date de modification se situe entre la date de début et le jour précédant la date de début.  
- Si vous spécifiez Plus de 1 jour, vous sélectionnez les fichiers dont la date de dernière modification est antérieure au jour précédant le début de la synchronisation.  
- Après la date de début de la synchronisation, les fichiers dont la date de dernière modification est la date de début de la synchronisation seront sélectionnés. (Si la synchronisation se termine le jour suivant, les fichiers seront sélectionnés après la date de début).  

### Sélectionner les sous-dossiers  

Si cette option n'est pas cochée, tous les sous-répertoires seront synchronisés. Si cette case est cochée, un bouton de filtre de répertoire apparaîtra.  

### Démarrer la synchronisation seulement si l'appareil est en charge  
Les tâches planifiées par Auto Sync ne démarrent pas si l'appareil ne se charge pas. Il est toujours possible de les démarrer manuellement.   

### Synchroniser les fichiers situés dans la racine du dossier source (si décoché, seuls les dossiers et leurs fichiers/sous-dossiers sont synchronisés)  

Si la case n'est pas cochée, seuls les dossiers et leurs fichiers/sous-dossiers sous le répertoire principal sont synchronisés. Par défaut, il est coché et les fichiers situés directement à la racine du répertoire principal seront également synchronisés.   

### Confirmer avant de remplacer/supprimer  
Lorsqu'elle est cochée, elle affiche un dialogue de confirmation avant d'écraser ou de supprimer des fichiers.   

### Options WiFi AP  
Vous pouvez définir si la synchronisation peut être lancée par l'état du réseau.  

- Même si WiFi éteint:essaiera de démarrer la synchronisation même si le WiFi est désactivé  

- Tous les réseaux:se synchronise uniquement lorsqu'il est connecté à un réseau WiFi. Il acceptera n'importe quel nom de SSID WLAN.  
Ne se synchronise que lorsqu'il est connecté à un réseau WiFi. Il acceptera n'importe quel nom de SSID WLAN.  
- WiFi avec IP privée  
vous ne pouvez lancer la synchronisation que si l'adresse IP attribuée au réseau WiFi se situe dans les plages suivantes : 10.0.0.0 - 10.255.255.255, 172.16.0.0 - 172.31.255.255 ou 192.168.0.0 - 192.168.255.255.  
- Liste d'adresse IP  
vous ne pouvez commencer la synchronisation que si l'adresse IP WiFi correspond à l'une des adresses spécifiées.
Vous pouvez utiliser des jokers pour le filtre. (par exemple, 192.168.100.\*, 192.168.\*)  
Vous pouvez également ajouter directement l'adresse IP actuelle à laquelle votre appareil est connecté via la liste de sélection des adresses IP.  

Il existe plusieurs façons de se synchroniser lors de la connexion à un WiFi spécifique. Consultez la [FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_FR.htm) pour plus de détails.  

1. Changez l'adresse IP de votre routeur WiFi pour une autre adresse que 192.168.0.0/24 et ajoutez-la à la liste des adresses IP  
2. Fixer une adresse IP du côté d'Android et l'enregistrer dans la liste des adresses IP  

### Sauter la tâche si l'adresse IP du WLAN ne correspond pas à l'adresse IP spécifiée.  

Auto Sync sautera la tâche si elle ne correspond pas aux critères spécifiés.   

### Autoriser la connexion aux adresses IP publiques  

La synchronisation sera autorisée chaque fois que le WiFi est connecté à n'importe quel réseau, même sur des plages IP publiques (comme dans un WiFi public).  

### Options avancées  

**Veuillez l'utiliser lorsque vous définissez des options détaillées.**  

### Inclure les sous-dossiers  

It will recursively include subdirectories under the specified master folder.   

### Inclure les dossiers vides  

Synchronise les répertoires vides (même si un répertoire est vide sur le master, il sera créé sur la cible). Si elle n'est pas cochée, les répertoires vides sur le master sont ignorés.   

### Inclure les dossiers cachés  

Lorsqu'elle est cochée, Sync inclura les dossiers linux cachés (ceux dont le nom commence par un point). Notez que dans Windows et Samba, l'attribut caché n'est pas défini par le nom du dossier. Ainsi, le dossier synchronisé sur la cible SMB/Windows n'aura pas l'attribut caché host.   

### Inclure les fichiers cachés  

Lorsqu'il est vérifié, Sync inclura les fichiers linux cachés (ceux dont le nom commence par un point). Notez que dans Windows et Samba, l'attribut caché n'est pas défini par le nom du fichier. Ainsi, le fichier synchronisé sur la cible SMB/Windows n'aura pas l'attribut caché de l'hôte.   

### Remplacer les fichiers destination  
If unchecked, files on the target will never be overwritten even if the compare criteria by size and time are different.  

### Utiliser le filtre amélioré de sélection de dossiers  

If the upper directory is excluded by a filter while one of its sub-directories is selected/included, the sub-directories of the selected folder will be synchronized.  

### Utiliser la version 2 des filtres  

Après l'avoir activé, vous pouvez utiliser le nouveau filtre. <span style="color: red; "><u>Il se peut qu'il se comporte différemment de l'ancien filtre, c'est pourquoi nous vous invitons à le tester minutieusement avant de l'utiliser.</u></span>  

### Supprimer les répertoires et les fichiers exclus par les filtres  

Lorsque cette option est cochée, elle supprime d'abord les répertoires et les fichiers qui n'existent pas dans le dossier principal, puis elle copie les fichiers dont la taille et la date de dernière modification sont différentes de celles du dossier principal. Si le dossier principal est SMB, le temps de traitement sera plus long car les fichiers sont analysés sur le réseau. Si possible, veuillez utiliser le protocole SMB "SMBv2/3".  

### Supprimer les dossiers et fichiers exclus par les filtres de nom  

On server-side connection errors, SMBSync2 will try again the synchronization for a maximum of 3 times at a 30 seconds interval.   

### Réessayer en cas d\'erreur de connexion  
Retenter la synchronisation uniquement en cas d'erreur du serveur SMB. Jusqu'à trois tentatives sont effectuées, chacune après que 30 secondes se soient écoulées depuis l'apparition de l'erreur.  

### Limiter le cache SMB E/S en écriture à 16Ko  

Lorsqu'il est vérifié, il limite le tampon d'entrée/sortie à 16KB pour l'écriture des opérations sur l'hôte SMB. 
**Veuillez essayer si vous obtenez une erreur "Access is denied" lorsque vous écrivez dans le dossier PC/NAS.**  

### Ne pas créer de fichier temporaire avant l\'écriture dans le dossier SMB  

Si elle n'est pas cochée, elle réécrira directement le fichier sur le serveur SMB de destination, ce qui brisera le fichier si la mise à jour est interrompue par <span style="color : red ;"><u>une erreur ou une annulation.</u></span>  

### Ne pas changer la date de dernière modification du fichier destination pour qu'elle soit identique à la source  

Veuillez l'activer si vous obtenez une erreur telle que SmbFile.setLastModified()/File.setLastModified() échoue. Cela signifie que l'hôte distant n'autorise pas le paramétrage du fichier de la dernière modification. Si cette case n'est pas cochée, la dernière heure de modification du fichier copié sur la cible sera fixée à l'heure à laquelle il a été copié / synchronisé. Cela signifie que le fichier cible apparaîtra plus récent que le fichier maître.   

### Obtenir la date de dernière modification des fichiers par une liste interne de l\'application  

Essayez ceci si tous les fichiers sont copiés à chaque fois. Maintenez la date et l'heure de dernière modification des fichiers locaux en utilisant la propre méthode de SMBSync2 au lieu d'utiliser Java File.setLastModified().  

### Utiliser la taille des fichiers pour déterminer s'ils sont différents  

Lors de la vérification, les fichiers sont considérés comme différents s'ils diffèrent par leur taille.   

### Comparaison uniquement par la taille des fichiers (les fichiers sont considérés différents seulement si la taille de la source est supérieure à celle de la destination)  

Si cette case est cochée, la synchronisation ne sera ciblée que si la taille du fichier maître est importante.  

### Utiliser la date de dernière modification pour déterminer si les fichiers sont différents  
Lorsque cette case est cochée, si l'heure de dernière modification du fichier est différente, il est considéré comme un fichier de différence.  

Différence de temps tolérée (sec) entre les fichiers source et destination  
Choisissez entre 1, 3 et 10 secondes. Si la différence de l'heure de la dernière modification du fichier se situe dans la différence de temps sélectionnée, aucune modification n'est supposée.  

### Ne pas remplacer le fichier de destination s\'il est plus récent que le fichier source  
Si cette option est cochée, le fichier est écrasé uniquement lorsque le fichier maître est plus récent que le fichier cible, même si la taille du fichier et l'heure de dernière modification sont différentes.  

### Ignorer la différence de temps liée au décalage de l\'heure d\'été  
Si cette case est cochée, le décalage horaire entre l'heure d'été et l'heure normale est ignoré.  

### Différence de temps entre l'heure d'hiver et l\'heure d'été (minutes)  
Indiquez le décalage horaire à ignorer.  

### Ignorer les dossiers et fichiers dont le nom contient des caractères invalides （"，：，\，*，<，>, |）  

Si cette case est cochée, le système ne traitera pas les répertoires/fichiers contenant des caractères non disponibles, mais affichera un message d'avertissement et traitera le répertoire/fichier suivant.  

### Ignorer les fichiers vides (0 octets de taille)  

Si cette case est cochée, les fichiers dont la taille est de 0 octet sont ignorés.  

### Ignorer le fichier si la longueur du nom du fichier est supérieure à la valeur spécifiée.  

Spécifie la longueur maximale (en octets) du nom du fichier de sortie. Ignore le fichier si le nombre d'octets dans le nom du fichier dépasse le maximum.  

### Manuels  
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_JA.htm)  
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_JA.htm)   
