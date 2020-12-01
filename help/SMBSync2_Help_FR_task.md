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

### Filtre de fichiers / Sélectionner les fichiers à synchroniser

Si cette option n'est pas cochée, tous les fichiers sont synchronisés. Si vous cochez le filtre de fichiers, vous obtenez les options suivantes :

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

Les caractères génériques comme *.docx sont pris en charge.

 

### Filtre de fichiers / Sélectionner les sous-dossiers

Si la case n'est pas cochée, tous les dossiers sont synchronisés. Si vous cochez le filtre des sous-répertoires, vous obtenez les options suivantes :

- Filtre de dossiers

  Il s'agit d'un filtre personnalisé d'inclusion/exclusion de répertoire. Vous pouvez sélectionner le nom des dossiers que vous souhaitez exclure ou inclure du processus de synchronisation. 

  Les caractères génériques comme /*cahe/ sont pris en charge.

### Démarrer la synchronisation seulement si l'appareil est en charge

Les tâches planifiées par Auto Sync ne démarrent pas si l'appareil ne se charge pas. Il est toujours possible de les démarrer manuellement. 

### Synchroniser les fichiers situés dans la racine du dossier source (si décoché, seuls les dossiers et leurs fichiers/sous-dossiers sont synchronisés)

Si la case n'est pas cochée, seuls les dossiers et leurs fichiers/sous-dossiers sous le répertoire principal sont synchronisés. Par défaut, il est coché et les fichiers situés directement à la racine du répertoire principal seront également synchronisés. 

### Confirmer avant de remplacer/supprimer

Lorsqu'elle est cochée, elle affiche un dialogue de confirmation avant d'écraser ou de supprimer des fichiers. 

### Options Wifi AP

- Même si Wifi éteint:essaiera de démarrer la synchronisation même si le Wifi est désactivé
- Tous les réseaux:se synchronise uniquement lorsqu'il est connecté à un réseau wifi. Il acceptera n'importe quel nom de SSID WLAN.
- Wifi avec IP privée:vous ne pouvez lancer la synchronisation que si l'adresse IP attribuée au réseau Wifi se situe dans les plages suivantes : 10.0.0.0 - 10.255.255.255, 172.16.0.0 - 172.31.255.255 ou 192.168.0.0 - 192.168.255.255.
- Liste d'adresse IP: vous ne pouvez commencer la synchronisation que si l'adresse IP WiFi correspond à l'une des adresses spécifiées. Vous pouvez également ajouter directement l'adresse IP actuelle à laquelle votre appareil est connecté via la liste de sélection des adresses IP.   
Vous pouvez utiliser des jokers pour le filtre. (par exemple, 192.168.100.\*, 192.168.\*)

Il existe plusieurs façons de se synchroniser lors de la connexion à un WiFi spécifique. Voir la FAQ pour plus de détails.

1. Changez l'adresse IP de votre routeur WiFi pour une autre adresse que 192.168.0.0/24 et ajoutez-la à la liste des adresses IP
2. Fixer une adresse IP du côté d'Android et l'enregistrer dans la liste des adresses IP

### Sauter la tâche si l'adresse IP du WLAN ne correspond pas à l'adresse IP spécifiée.

Auto Sync sautera la tâche si elle ne correspond pas aux critères spécifiés. 

### Autoriser la connexion aux adresses IP publiques

La synchronisation sera autorisée chaque fois que le Wifi est connecté à n'importe quel réseau, même sur des plages IP publiques (comme dans un Wifi public). 

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

### Supprimer les fichiers avant la sync(mode Miroir uniquement)

On server-side connection errors, SMBSync2 will try again the synchronization for a maximum of 3 times at a 30 seconds interval. 

### Supprimer les répertoires et les fichiers exclus par les filtres

Si elle est activée, **elle supprime les répertoires/fichiers exclus du filtre.** 

### Limiter le cache SMB E/S en écriture à 16Ko (uniquement pour les actions des SMB)

**Veuillez essayer si vous obtenez une erreur "Access is denied" lorsque vous écrivez dans le dossier PC/NAS.**

Lorsqu'il est vérifié, il limite le tampon d'entrée/sortie à 16KB pour l'écriture des opérations sur l'hôte SMB. 

### Ne pas créer de fichier temporaire avant l'écriture dans le dossier SMB (uniquement pour les actions des SMB)

Vérifié par défaut (recommandé). Lorsqu'il est copié sur l'hôte SMB, le fichier sera copié dans un dossier temporaire de l'hôte. Une fois l'opération de copie réussie, le fichier temporaire est déplacé vers sa destination finale en écrasant le fichier cible. Si la case n'est pas cochée, le fichier cible sur l'hôte est immédiatement écrasé au début de la copie. Si une erreur de connexion se produit, le fichier sur l'hôte reste corrompu jusqu'à la prochaine synchronisation. 

### Ne pas changer la date de dernière modification du fichier destination pour qu'elle soit identique à la source

Veuillez l'activer si vous obtenez une erreur telle que SmbFile#setLastModified()/File#setLastModified() échoue. Cela signifie que l'hôte distant n'autorise pas le paramétrage du fichier de la dernière modification. Si cette case n'est pas cochée, la dernière heure de modification du fichier copié sur la cible sera fixée à l'heure à laquelle il a été copié / synchronisé. Cela signifie que le fichier cible apparaîtra plus récent que le fichier maître. 

Pour les prochaines synchronisations, vous pouvez :

- vous en tenir à une comparaison par taille uniquement, ou

- vous pouvez activer l'option "Ne pas écraser le fichier de destination s'il est plus récent que le fichier source" pour ne copier que les fichiers modifiés ultérieurement sur le master, ou

- vous pouvez activer l'option de tâche "Obtenir l'heure de dernière modification des fichiers de la liste personnalisée de l'application SMBSync2". Cependant, cette option n'est pas disponible actuellement si la cible est SMB. La plupart des hôtes SMB prennent en charge la définition de l'heure de dernière modification. 

Voir ci-dessous pour des informations détaillées sur chaque option.

 

### Obtenir la date de dernière modification des fichiers par une liste interne de l'application

Veuillez essayer de copier tous les fichiers sur les fichiers cibles à chaque synchronisation.

Cette option n'est disponible pour l'utilisateur que lorsque la cible est le stockage interne. Pour les autres types de stockage, SMBSync2 essaie de détecter automatiquement si la cible prend en charge le paramétrage de la dernière modification du fichier. Si ce n'est pas le cas, il utilisera automatiquement cette fonction intégrée. Une exception est lorsque la cible est SMB, cette fonction ne sera pas utilisée. Voir ci-dessus l'option "Ne pas définir l'heure de dernière modification du fichier de destination pour qu'elle corresponde au fichier source" pour d'autres solutions de contournement. Notez que la plupart des appareils androïdes récents ne prennent pas en charge la mise à jour de l'heure de dernière modification du fichier cible.

Lors de la première synchronisation, SMBSync2 stockera la liste des fichiers avec leurs horodatages dans sa base de données (cf. 1.3). Comme la base de données doit être créée la première fois, tous les fichiers ayant le même nom et la même taille seront à nouveau écrasés par une copie complète du fichier maître lors de la première synchronisation. Lors des synchronisations suivantes, la base de données sera utilisée et l'écrasement de tous les fichiers de même nom et de même taille ne sera plus effectué. La dernière heure de modification du fichier local est enregistrée par le code propriétaire SMBSync2 (il ne repose pas sur la fonction Java File#setLastModified()). 

### Supprimer les fichiers avant la sync (mode Miroir uniquement)

Lorsqu'elle est cochée, les répertoires et les fichiers qui sont présents sur le dossier cible mais qui n'existent pas sur le master, seront d'abord supprimés. Ensuite, les fichiers et les dossiers qui sont différents seront copiés sur la cible.

Si le dossier maître est SMB, le temps de traitement sera plus long car la structure des répertoires et leur contenu sont scannés à travers le réseau. Il est fortement recommandé d'activer l'option "Utiliser la négociation SMB2" car SMB1 sera très lent. 

### Utiliser la taille des fichiers pour déterminer s'ils sont différents

Lors de la vérification, les fichiers sont considérés comme différents s'ils diffèrent par leur taille. 

### Comparaison uniquement par la taille des fichiers (les fichiers sont considérés différents seulement si la taille de la source est supérieure à celle de la destination)

Les fichiers ne sont considérés comme différents que si la taille de la source est supérieure à celle de la destination. Cela désactivera la comparaison par temps de fichier. 

### Utiliser la date de dernière modification pour déterminer si les fichiers sont différents

Lorsqu'ils sont vérifiés, les fichiers sont considérés comme différents en fonction de la date de leur dernière modification. 

### Différence de temps tolérée (sec) entre les fichiers source et destination

Les fichiers sont considérés comme identiques si la différence entre leurs derniers temps modifiés est inférieure ou égale au temps sélectionné en secondes. Ils sont considérés comme différents si la différence de temps entre les fichiers est supérieure à l'heure sélectionnée. Les fichiers FAT et ExFAT ont besoin d'une tolérance minimale de 2 secondes. Si 0 seconde est sélectionnée, les fichiers doivent avoir exactement le même temps pour être considérés comme similaires. 

### Fichier non synchronisé, car l'option "Ne pas remplacer le fichier de destination s'il est plus récent que le fichier source" est activée.

Si la case est cochée, le fichier ne sera écrasé que si le fichier principal est plus récent que le fichier cible, même si la taille du fichier et les heures de la dernière mise à jour sont différentes. Gardez à l'esprit que si vous changez de fuseau horaire ou si les fichiers sont modifiés pendant la période d'intervalle du changement d'heure d'été, le dernier fichier modifié pourrait apparaître plus ancien que le fichier non mis à jour. Ceci est lié aux différences de système de fichiers et seule une vérification manuelle avant d'écraser le fichier évitera la perte de données. Il est généralement recommandé de ne pas modifier les fichiers pendant l'intervalle de changement d'heure d'été s'ils sont destinés à être auto-synchronisés. 

###  Ignorer la différence de temps liée au décalage de l'heure d'été

Permet de sélectionner le décalage horaire en minutes entre l'heure d'été et l'heure d'hiver. Les fichiers sont considérés comme différents si le décalage horaire n'est pas exactement égal à l'intervalle spécifié (+/- le "décalage horaire minimum autorisé (en secondes)" spécifié dans l'option précédente) 

###  Ignorer les dossiers et fichiers dont le nom contient des caractères invalides (", :, \, *, <, >, |)

Si elle est cochée, elle affichera un message d'avertissement et la synchronisation se poursuivra sans traiter les répertoires/fichiers contenant des caractères non valides. 

###  Supprimer le dossier source s'il est vide (uniquement en mode Déplacer)

Lorsque le mode de synchronisation est "Move", après que les fichiers aient été déplacés vers la cible, le dossier "Master" est également supprimé.  

### Informations détaillées

https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing

 