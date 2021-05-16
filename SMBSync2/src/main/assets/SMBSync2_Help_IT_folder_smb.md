### Ricerca server SMB<br>
Scansiona la rete per trovare il server SMB disponibile <br>

### Server host name/Indirizzo IP<br>
Specificare il nome del server SMB o l'indirizzo IP <br>

### Protocollo SMB<br>
È possibile specificare il protocollo SMB.<br>

- Usa SMB V1<br>
- Usa SMB V2 (2.01)<br>
- Usa SMB V2 (2.11)<br>
- Usa SMB V2/3 (2.12)<br>
- Usa SMB V2/3 (2.14)<br>

### Specifica numero porta<br>
Specificare il numero di porta se diverso da quello predefinito. Se lasciato vuoto, SMBSync2 cercherà di connettersi ai numeri di porta standard di default 139 e 445. <br>

### Usa nome account e password<br>
Deve essere completato se richiesto dal server. <br>

### Nome utente<br>
Nome dell'account utente per connettersi all'host. L'account Microsoft non può essere usato con SMBSync2. Per favore crea un account locale ed usalo. <br>

### Password<br>
Password per connettersi all'host. <br>

### Lista Condivisioni<br>
Si collega all'host specificato ed elenca tutte le azioni disponibili per l'account. <br>

### Elenca le directory<br>
Quando si preme il pulsante dell'elenco, verranno visualizzate le directory selezionabili, quindi selezionarlo. Tocca il nome della directory per aprire una sottodirectory.<br>

### Cartelle<br>
Potete inserire direttamente il nome di una directory. Se si specifica una directory inesistente, questa verrà creata quando la sincronizzazione viene eseguita.<br>
%YEAR%, %MONTH%, %DAY% e %DAY-OF-YEAR% possono essere utilizzati come nomi di cartelle. Le parole chiave vengono convertite nell'anno, nel mese e nel giorno in cui è iniziata la sincronizzazione.<br>

### Aggiungi tempo/data del sync nel nome della cartella (la parola chiave verrà sostituita dall'ora e la data nel quale è stat eseguita la sync)<br>

**Queste opzioni verranno mostrate solo quando il tipo di sync è Copia/Sposta.**<br>
Se selezionata, i dati EXIF del supporto sottoposto a backup vengono utilizzati per ottenere la data e l'ora dello scatto. Il timestamp di ripresa viene utilizzato e aggiunto alla cartella di destinazione. Quando l'applicazione non è in grado di ottenere l'ora di scatto dall'EXIF, utilizza l'ultima ora modificata del file.<br>

**<u>Queste opzioni verranno mostrate solo quando il tipo di sync è Archivia.</u>**<br>

### Se l'ora e la data non possono essere determinate dai dati EXIF, mostra un messaggio di conferma<br>

Se l'opzione è selezionata, quando la data e l'ora di scatto non possono essere acquisite da EXIF , viene visualizzato un messaggio di conferma dell'utilizzo o meno dell'ultima ora di modifica del file. Se si seleziona Annulla nella finestra di dialogo di conferma, il file non verrà archiviato.<br>

### Archivia file se<br>

Scegliere i criteri temporali per determinare quali file archiviare. La selezione dell'ora si basa sulla data di scatto delle foto/video o sull'ultima ora di modifica se non è possibile acquisire il timestamp dall'intestazione EXIF.<br>

- Qualunque data (Tutti)<br>
Archivia tutte le immagini/video<br>
- Più vecchio di 7 giorni<br>
Archiviare solo i file con una data di scatto più vecchia di 7 giorni o più rispetto all'ora attuale<br>
- Più vecchio di 30 giorni<br>
Archiviare solo i file con una data di scatto più vecchia di 30 giorni o più rispetto all'ora attuale<br>
- Più vecchio di 60 giorni<br>
Archiviare solo i file con una data di scatto più vecchia di 60 giorni o più rispetto all'ora attuale<br>
- Più vecchio di 90 giorni<br>
Archiviare solo i file con una data di scatto più vecchia di 90 giorni o più rispetto all'ora attuale<br>
- Più vecchio di 180 giorni<br>
Archiviare solo i file con una data di scatto più vecchia di 180 giorni o più rispetto all'ora attuale<br>
- Più vecchio di 1 anno<br>
Archiviare solo i file con una data di scatto più vecchia di 1 anno o più rispetto all'ora attuale<br>

### Rinomina i file durante l'archiviazione e li memorizza nella directory specificata.<br>

Se spuntato, il nome del file sarà cambiato durante l'archiviazione. Puoi usare "File name template" per aggiungere data e ora al nome del file. Potete anche creare una directory per memorizzare i file. Per memorizzare i file nella directory, abilita "Save in directory when archiving". <br>

### Incrementa nome dei file aggiungendo<br>

Add an order number to the file name when archiving.<br>

- Non cambiare<br>
Non aggiungere un numero progressivo<br>
- 2 cifre (01-99)<br>
Aggiungi 01-99<br>
- 3 cifre (001-999)<br>
Aggiungi 001-999<br>
- 4 cifre (0001-9999)<br>
Aggiungi 0001-9999<br>
- 5 cifre (00001-99999)<br>
Aggiungi 00001-99999<br>
- 6 cifre (000001-999999)<br>
Aggiungi 000001-999999<br>

### Cambia il nome del file prima di archiviarlo nella cartella destinazione <br>

Se l'opzione è selezionata, il nome del file archiviato verrà modificato in base ai criteri selezionati. Potete aggiungere la data e l'ora nel nome del file "Modello del nome del file". <br>

- %ORIGINAL-NAME%<br>
Sarà sostituito dal nome originale del file durante l'archiviazione dell'archivio (es. DSC_0001)<br>
- %DATE%<br>
Sarà sostituito dalla data di scatto.(Es. 2018-01-01)<br>
- %TIME%<br>
Sarà sostituito dal tempo di scatto.(Es. 13:01:10)<br>
L'ultima riga della schermata mostra come apparirà la cartella di destinazione e il nome del file una volta archiviati.<br>

### Crea una cartelle basata sull'ora di scatto per salvare i file<br>

Crea una cartelle con indicazione dell'ora in cui memorizzare i file archiviati.<br>

### Template Cartelle<br>

Inserire gli schemi da utilizzare per rinominare la cartelle da creare (exp. DIR-% YEAR% -% MONTH% e così via). Premendo i tasti dei modelli, potete inserire le parole chiave dietro il cursore.<br>

- %YEAR%<br>
Sarà sostituito dall'anno di riprese. (Es. 2018)<br>
- %MONTH%<br>
Sarà sostituito dal Mese di scatto (Es. 01)<br>
- %DAY%<br>
Sarà sostituito dal giorno delle riprese (Es. 29)<br>

L'ultima riga della schermata mostra come apparirà la cartella di destinazione e il nome del file una volta archiviati.<br>

### Manuali<br>
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm)<br>
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_EN.htm) <br>
