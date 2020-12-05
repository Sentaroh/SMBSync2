## 1.Functions
SMBSync2 est un outil permettant de synchroniser des fichiers via un réseau local sans fil en utilisant le protocole SMB1, SMB2 ou SMB3 entre le stockage interne du terminal Android, SDCARD et PC/NAS. La synchronisation est un aller simple du maître à la cible. Les modes miroir, déplacement, copie et archivage sont pris en charge. De nombreuses combinaisons de stockage sont supportées (stockage interne, SDCARD, OTG-USB, SMB, ZIP)  
La synchronisation peut être lancée automatiquement par des applications externes (Tasker, AutoMagic, etc.) ou par le programme SMBSync2.  
La synchronisation s'effectue entre deux paires de dossiers appelés le Master (dossier source) et le Target (dossier de destination). Il s'agit d'une synchronisation unidirectionnelle, du maître à la cible.  
Les modes de synchronisation pris en charge sont les suivants:  
- Miroir

  Le dossier cible est conservé comme une copie exacte du dossier principal. Si un fichier est différent entre le dossier principal et le dossier cible, le fichier principal écrase le fichier cible. Les dossiers et fichiers non présents sur la cible sont copiés à partir du master. Les fichiers et dossiers qui n'existent pas sur le master sont également supprimés de la cible. Seuls les fichiers modifiés (par la taille et/ou la date/heure) sont mis à jour dans la cible.

- Déplacement

  Si un fichier est différent entre le maître et la cible, le fichier sur le maître écrase le fichier sur la cible. Une fois copiés sur la cible, les fichiers et les dossiers sont supprimés du master (comme la commande move).
  Seuls les fichiers modifiés (par la taille et/ou la date/heure) sont copiés dans la cible. Les fichiers identiques, sur la base des critères de comparaison sélectionnés, sont supprimés du fichier maître sans être copiés. Les fichiers et dossiers de la cible, non présents sur le master, sont évidemment préservés.

- Copie

  Même chose que pour Move, mais les fichiers ne sont pas supprimés du master après avoir été copiés.
  Si un fichier est différent entre le master et la cible, le fichier du master écrase le fichier de la cible. Une fois copiés sur la cible, les fichiers et les dossiers sont conservés sur le master (comme une commande de copie).
  Seuls les fichiers modifiés (par la taille et/ou la date/heure) sont copiés vers la cible. Les fichiers identiques, sur la base des critères de comparaison sélectionnés, sont ignorés et ne sont pas copiés à nouveau.

- Archives

  Archivez des photos et des vidéos en les déplaçant du dossier principal au dossier cible. Des critères spécifiques aux médias peuvent être spécifiés pour l'archivage : date/heure de tournage, date et heure de la dernière exécution de l'archive (par exemple 7 jours ou plus tôt ou 30 jours ou plus tôt).
  Le ZIP ne peut pas être spécifié comme cible pour les opérations d'archivage.

**Comparer les critères :**  
Les dossiers sont considérés comme différents sur la base de ces critères:  
1. Le nom du fichier/dossier n'existe que sur le maître ou la cible, et non sur les deux côtés
2. Les fichiers ont des tailles différentes
3. Les fichiers ont un horodatage différent (date et heure de la dernière modification) 

Dans les options avancées, de nombreux paramètres de comparaison peuvent être ajustés : l'intervalle de tolérance temporelle peut être réglé pour ignorer la différence si elle est inférieure à 1, 3, 5 ou 10 secondes pour la compatibilité avec les médias FAT/exFAT. Ignorer l'heure d'été est pris en charge. Possibilité de ne pas écraser le fichier cible s'il est plus récent que le fichier maître ou s'il est plus volumineux...  
Lorsque la cible se trouve sur le stockage interne ou sur la carte SD, la plupart des systèmes Android ne permettent pas de régler l'heure de la dernière modification du fichier cible pour qu'elle corresponde à l'heure du fichier source. Lorsque la cible est une SMB (PC/NAS), ou un stockage OTG-USB, cela ne pose généralement pas de problème. SMSync2 détecte si l'heure/la date peut être réglée sur la cible pour correspondre au fichier source. Si ce n'est pas le cas, la dernière heure de mise à jour du fichier est enregistrée dans les fichiers de la base de données de l'application. Elle est ensuite utilisée pour comparer les fichiers et vérifier s'ils diffèrent dans le temps. Dans ce cas, si vous essayez de synchroniser la paire maître/cible avec une application tierce ou si les fichiers de données SMBSync2 sont effacés, les fichiers sources seront à nouveau copiés sur la cible. Vous pouvez régler l'option "Ne pas écraser le fichier de destination s'il est plus récent que le maître" en plus de la comparaison par taille pour surmonter ce problème.

## 2.FAQs
[Veuillez vous référer au lien PDF](https://drive.google.com/file/d/1QPz_VN8Hur0cfvzF35SP-dt9_QTkv-ZA/view?usp=sharing)

## 3. Bibliothèque externe
- [jcifs-ng](https://github.com/AgNO3/jcifs-ng)
- [jcifs-1.3.17](https://jcifs.samba.org/)
- [bcprov-jdk15to18-1.66](https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15to18/1.66)  
- [Zip4J 1.3.2](https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j/1.3.2)
- [juniversalchardet-1.0.3](https://code.google.com/archive/p/juniversalchardet/)
- [Metadata-extractor](https://github.com/drewnoakes/metadata-extractor)

## 4.Documents
[Veuillez vous référer au lien PDF](https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing)