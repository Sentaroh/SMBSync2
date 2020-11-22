### tipo memoria

Select mount point for internal storage.

### Lista cartelle

Mostra l'elenco delle directory nella memoria interna. 
Quando si preme il tasto elenco directory, vengono visualizzate le directory selezionabili. Premete sulla directory che volete scegliere e poi sul tasto "Seleziona". Per navigare attraverso le sottodirectory, toccate il nome della directory.

### Aggiungi parole chiave

%YEAR%, %MONTH%, %DAY% e %DAY-OF-YEAR% possono essere utilizzati come nomi di directory di destinazione. Le parole chiave vengono convertite nell'anno, nel mese e nel giorno in cui è iniziata la sincronizzazione.

**Queste opzioni vengono visualizzate solo quando il tipo di sincronizzazione è Copy/Move.**

### Aggiungi tempo/data del sync nel nome della cartella (the keyword will be replaced by the date e time in which the sync was performed)

Se spuntata, i dati EXIF dei supporti di backup vengono utilizzati per ottenere la data e l'ora di ripresa. Il timestamp della data e dell'ora di ripresa viene utilizzato e allegato alla cartella di destinazione. Quando l'applicazione non è in grado di ottenere l'ora di ripresa dall'EXIF, utilizza l'ultima ora modificata del file.

### Cartelle

Si prega di inserire l'elenco di destinazione o l'elenco principale. Se si specifica una directory che non esiste nel target, questa verrà creata durante la sincronizzazione.

### Seleziona cartella USB

Selezionare la directory principale USB per accedere a USB. Se non è possibile selezionarla, inviare le informazioni di sistema allo sviluppatore con "Menu -> Informazioni di sistema".

**Le seguenti opzioni vengono visualizzate solo quando il tipo di sincronizzazione è Archivio.**

### If the date e time cannot be determined by EXIF data, display a confirmation message

Se l'opzione è selezionata, quando la data e l'ora di ripresa non possono essere acquisite da EXIF , viene visualizzato un messaggio di conferma dell'utilizzo o meno dell'ultima ora modificata del file. Se si seleziona Annulla nella finestra di dialogo di conferma, il file non verrà archiviato. 

### Archivia file se

Scegliere i criteri temporali per determinare quali file archiviare. La selezione dell'ora si basa sulla data di ripresa delle foto/video o sull'ultima ora modificata se non è possibile acquisire il timestamp dall'intestazione EXIF.

- Qualunque data (Tutti)

  Archivia tutte le immagini/video

- Più vecchio di 7 giorni

  Archiviare solo i file con una data di ripresa più vecchia di 7 giorni o più rispetto all'ora attuale

- Più vecchio di 30 giorni

  Archiviare solo i file con una data di ripresa più vecchia di 30 giorni o più rispetto all'ora attuale

- Più vecchio di 60 giorni

  Archiviare solo i file con una data di ripresa più vecchia di 60 giorni o più rispetto all'ora attuale

- Più vecchio di 90 giorni

  Archiviare solo i file con una data di ripresa più vecchia di 90 giorni o più rispetto all'ora attuale

- Più vecchio di 180 giorni

  Archiviare solo i file con una data di ripresa più vecchia di 180 giorni o più rispetto all'ora attuale

- Più vecchio di 1 anno

  Archiviare solo i file con una data di ripresa più vecchia di 1 anno o più rispetto all'ora attuale

### Increment file names by appending

Aggiungere il numero di sequenza incrementale al nome del file durante l'archiviazione.

- Non cambiare

  Non aggiungere un numero progressivo

- 2 cifre (01-99)

  Append 001-999

- 3 cifre (001-999)

  Append 001-999

- 4 cifre (0001-9999)

  Append 0001-9999

- 5 cifre (00001-99999)

  Append 00001-99999

- 6 cifre (000001-999999)

  Append 000001-999999 

### Cambia il nome del file before archiving it in the cartella destinazione 

Se l'opzione è selezionata, il nome del file archiviato verrà modificato in base ai criteri selezionati. Potete aggiungere la data e l'ora nel nome del file "Modello del nome del file". 

### File name template

Inserire gli schemi da utilizzare per rinominare i file archiviati. Il valore predefinito è DSC_%DATE%.

- %ORIGINAL-NAME%

  Sarà sostituito dal nome originale del file durante l'archiviazione dell'archivio (es. DSC_0001)

- %DATE%

  Sarà sostituito dalla data di ripresa.(Es. 2018-01-01)

- %TIME%

  Sarà sostituito dal tempo di ripresa.(Es. 13:01:10) 

L'ultima riga della schermata mostra come apparirà la cartella di destinazione e il nome del file una volta archiviati.

### Create a directory based on the shooting date to store the files

Crea una directory con indicazione dell'ora in cui memorizzare i file archiviati.

Quando viene selezionato, viene visualizzato il "Modello di nome della directory". 

### Directory template

Inserire gli schemi da utilizzare per rinominare la directory da creare (exp. DIR-% YEAR% -% MONTH% e così via). Premendo i tasti dei modelli, potete inserire le parole chiave dietro il cursore.

- %YEAR%

  Sarà sostituito dall'anno di riprese. (Es. 2018)

- %MONTH%

  Sarà sostituito dal Mese di ripresa (Es. 01)

- %DAY%

  Sarà sostituito dal giorno delle riprese (Es. 29)

 L'ultima riga della schermata mostra come apparirà la cartella di destinazione e il nome del file una volta archiviati.

### Informazioni dettagliate

https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing