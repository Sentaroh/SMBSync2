### Elenca le directory  
Quando si preme il pulsante dell'elenco, verranno visualizzate le directory selezionabili, quindi selezionarlo. Tocca il nome della directory per aprire una sottodirectory.  

### Cartelle  
Potete inserire direttamente il nome di una directory. Se si specifica una directory inesistente, questa verrà creata quando la sincronizzazione viene eseguita.  
%YEAR%, %MONTH%, %DAY% e %DAY-OF-YEAR% possono essere utilizzati come nomi di cartelle. Le parole chiave vengono convertite nell'anno, nel mese e nel giorno in cui è iniziata la sincronizzazione.  

### Aggiungi tempo/data del sync nel nome della cartella (la parola chiave verrà sostituita dall'ora e la data nel quale è stat eseguita la sync)  
**Queste opzioni verranno mostrate solo quando il tipo di sync è Copia/Sposta.**  
Se selezionata, i dati EXIF del supporto sottoposto a backup vengono utilizzati per ottenere la data e l'ora dello scatto. Il timestamp di ripresa viene utilizzato e aggiunto alla cartella di destinazione. Quando l'applicazione non è in grado di ottenere l'ora di scatto dall'EXIF, utilizza l'ultima ora modificata del file.  

**<u>Queste opzioni verranno mostrate solo quando il tipo di sync è Archivia.</u>**  

### Se l'ora e la data non possono essere determinate dai dati EXIF, mostra un messaggio di conferma  
Se l'opzione è selezionata, quando la data e l'ora di scatto non possono essere acquisite da EXIF , viene visualizzato un messaggio di conferma dell'utilizzo o meno dell'ultima ora di modifica del file. Se si seleziona Annulla nella finestra di dialogo di conferma, il file non verrà archiviato.  

### Archivia file se  
Scegliere i criteri temporali per determinare quali file archiviare. La selezione dell'ora si basa sulla data di scatto delle foto/video o sull'ultima ora di modifica se non è possibile acquisire il timestamp dall'intestazione EXIF.  

- Qualunque data (Tutti)  
Archivia tutte le immagini/video  
- Più vecchio di 7 giorni  
Archiviare solo i file con una data di scatto più vecchia di 7 giorni o più rispetto all'ora attuale  
- Più vecchio di 30 giorni  
Archiviare solo i file con una data di scatto più vecchia di 30 giorni o più rispetto all'ora attuale  
- Più vecchio di 60 giorni  
Archiviare solo i file con una data di scatto più vecchia di 60 giorni o più rispetto all'ora attuale  
- Più vecchio di 90 giorni  
Archiviare solo i file con una data di scatto più vecchia di 90 giorni o più rispetto all'ora attuale  
- Più vecchio di 180 giorni  
Archiviare solo i file con una data di scatto più vecchia di 180 giorni o più rispetto all'ora attuale  
- Più vecchio di 1 anno  
Archiviare solo i file con una data di scatto più vecchia di 1 anno o più rispetto all'ora attuale  

### Rinomina i file durante l'archiviazione e li memorizza nella directory specificata.  
Se spuntato, il nome del file sarà cambiato durante l'archiviazione. Puoi usare "File name template" per aggiungere data e ora al nome del file. Potete anche creare una directory per memorizzare i file. Per memorizzare i file nella directory, abilita "Save in directory when archiving".   

### Incrementa nome dei file aggiungendo  
Add an order number to the file name when archiving.  

- Non cambiare  
Non aggiungere un numero progressivo  
- 2 cifre (01-99)  
Aggiungi 01-99  
- 3 cifre (001-999)  
Aggiungi 001-999  
- 4 cifre (0001-9999)  
Aggiungi 0001-9999  
- 5 cifre (00001-99999)  
Aggiungi 00001-99999  
- 6 cifre (000001-999999)  
Aggiungi 000001-999999  

### Cambia il nome del file prima di archiviarlo nella cartella destinazione   
Se l'opzione è selezionata, il nome del file archiviato verrà modificato in base ai criteri selezionati. Potete aggiungere la data e l'ora nel nome del file "Modello del nome del file".   

- %ORIGINAL-NAME%  
Sarà sostituito dal nome originale del file durante l'archiviazione dell'archivio (es. DSC_0001)  
- %DATE%  
Sarà sostituito dalla data di scatto.(Es. 2018-01-01)  
- %TIME%  
Sarà sostituito dal tempo di scatto.(Es. 13:01:10)  
L'ultima riga della schermata mostra come apparirà la cartella di destinazione e il nome del file una volta archiviati.  

### Crea una cartelle basata sull'ora di scatto per salvare i file  
Crea una cartelle con indicazione dell'ora in cui memorizzare i file archiviati.  

### Template Cartelle  
Inserire gli schemi da utilizzare per rinominare la cartelle da creare (exp. DIR-% YEAR% -% MONTH% e così via). Premendo i tasti dei modelli, potete inserire le parole chiave dietro il cursore.  

- %YEAR%  
Sarà sostituito dall'anno di riprese. (Es. 2018)  
- %MONTH%  
Sarà sostituito dal Mese di scatto (Es. 01)  
- %DAY%  
Sarà sostituito dal giorno delle riprese (Es. 29)  

L'ultima riga della schermata mostra come apparirà la cartella di destinazione e il nome del file una volta archiviati.  

### Manuali  
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm)  
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_EN.htm)   
