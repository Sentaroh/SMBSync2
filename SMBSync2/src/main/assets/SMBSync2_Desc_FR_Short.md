## 1.Functions  
SMBSync2 est un outil permettant de synchroniser des fichiers via un réseau local sans fil en utilisant le protocole SMB1, SMB2 ou SMB3 entre le stockage interne du terminal Android, SDCARD/USB-OTG et PC/NAS. La synchronisation est un aller simple du maître à la cible. Les modes miroir, déplacement, copie et archivage sont pris en charge. De nombreuses combinaisons de stockage sont supportées (stockage interne, SDCARD, USB-OTG, SMB, ZIP)  
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
  Déplacez les photos et les vidéos du répertoire du maître vers la cible si elles ont été prises avant 7 jours ou 30 jours avant la date d'exécution de l'archive. (Toutefois, vous ne pouvez pas utiliser le zip à la cible).  
Les types de fichiers suivants sont éligibles pour l'archivage.  
gif, "jpg", "jpeg", "jpe", "png", "mp4", "mov".  

**Comparer les critères :**  
Les dossiers sont considérés comme différents sur la base de ces critères:  

1. Le nom du fichier n'existe que sur le maître ou la cible, et non sur les deux côtés  
2. Les fichiers ont des tailles différentes  
3. Les fichiers ont un horodatage différent (date et heure de la dernière modification)   

Dans les options avancées, de nombreux paramètres de comparaison peuvent être ajustés (en voici un exemple)  
- L'intervalle de tolérance temporelle peut être fixé de manière à ignorer la différence si celle-ci est inférieure à 1, 3, 5 ou 10 secondes pour des raisons de compatibilité avec les médias FAT/exFAT.   
- Ignorer l'heure d'été est pris en charge.   
- Possibilité de ne pas écraser le fichier cible s'il est plus récent que le fichier maître ou s'il est plus volumineux.  

## 2.Documents  
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_FR.htm)  
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_EN.htm)  
