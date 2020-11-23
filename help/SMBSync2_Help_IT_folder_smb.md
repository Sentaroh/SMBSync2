### Ricerca server SMB

Scansiona la rete per trovare il server SMB disponibile 

### Server host name/Indirizzo IP

Specificare il nome del server SMB o l'indirizzo IP 

### Protocollo SMB

È possibile specificare il protocollo SMB.

- Usa SMB V1

- Usa SMB V2 (2.01)

  SMB min v2.1, max v2.1

- Usa SMB V2 (2.11)

  SMB min v2.1, max v2.1

- Usa SMB V2/3 (2.12)

  Support SMB V2 or V3 protocol

  SMB min v2.0.2, max v3.0

- Usa SMB V2/3 (2.14)

  SMB min v2.0.2, max v3.0 

### Usa SMB2 negotiation

Se spuntata, costringerà setProperty("jcifs.smb.client.useSMB2Negotiation", "true") nella funzione JcifsAuth(). Questo assicura l'uso di SMB2 non compatibile con lo stile di negoziazione non retrocompatibile, incompatibile con le versioni precedenti a SMB 2.0.2 

### SMB IPC Signing Enforced

Una volta controllato, il cliente è tenuto ad utilizzare la firma SMB per le connessioni IPC$ come trasporto DCERPC. Altrimenti è consentito, ma non richiesto. 

### Specifica numero porta

Specificare il numero di porta se diverso da quello predefinito. Se lasciato vuoto, SMBSync2 cercherà di connettersi ai numeri di porta standard di default 139 e 445. 

### Usa nome account e password

Deve essere completato se richiesto dal server. 

### Nome utente

Nome dell'account utente per connettersi all'host. L'account Microsoft non può essere usato con SMBSync2. Per favore crea un account locale ed usalo. 

### Password

Password per connettersi all'host. 

### Lista Condivisioni

Si collega all'host specificato ed elenca tutte le azioni disponibili per l'account. 

(I nomi delle azioni verranno visualizzati quando si preme il tasto "Lista Condivisioni". Fare clic sul nome desiderato, quindi premere OK per convalidare) 

### Lista cartelle

Mostra l'elenco delle cartelle sulla Condivisione selezionata. 

Quando si preme il tasto elenco cartelle, verranno visualizzate le cartelle selezionabili. 

Premete sulla cartelle che volete scegliere e poi sul tasto "Seleziona". Per ricapitolare 

attraverso le sottocartelle, toccare il nome della cartelle. 

### Cartelle

Si prega di inserire l'elenco di destinazione o l'elenco principale. Se si specifica una cartelle che non esiste nel target, questa verrà creata durante la sincronizzazione. 

### Aggiungi parole chiave

%YEAR%, %MONTH%, %DAY% e %DAY-OF-YEAR% possono essere utilizzati come nomi di cartelle di destinazione. Le parole chiave vengono convertite nell'anno, nel mese e nel giorno in cui è iniziata la sincronizzazione.

**Queste opzioni vengono visualizzate solo quando il tipo di sincronizzazione è Copia/Sposta.**

### Aggiungi tempo/data del sync nel nome della cartella (la parola chiave verrà sostituita dall'ora e la data nel quale è stata eseguita la sync)

Se spuntata, i dati EXIF dei supporti di backup vengono utilizzati per ottenere la data e l'ora di ripresa. Il timestamp della data e dell'ora di ripresa viene utilizzato e allegato alla cartella di destinazione. Quando l'applicazione non è in grado di ottenere l'ora di ripresa dall'EXIF, utilizza l'ultima ora modificata del file. 

**Le seguenti opzioni vengono visualizzate solo quando il tipo di sincronizzazione è Archivio.**

### Se l'ora e la data non possono essere determinate dai dati EXIF, mostra un messaggio di conferma

Se l'opzione è selezionata, quando la data e l'ora di ripresa non possono essere acquisite da EXIF , viene visualizzato un messaggio di conferma dell'utilizzo o meno dell'ultima ora modificata del file. Se si seleziona Annulla nella finestra di dialogo di conferma, il file non verrà archiviato. 

### Archivia file se…

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

 

### Nome file incrementale

Aggiungere il numero di sequenza incrementale al nome del file durante l'archiviazione.

- Non cambiare

  Non aggiungere un numero progressivo

- 2 cifre (01-99)

  Aggiungi 001-999

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

### Template nome file

Inserire gli schemi da utilizzare per rinominare i file archiviati. Il valore predefinito è DSC_%DATE%.

- %ORIGINAL-NAME%

  Sarà sostituito dal nome originale del file durante l'archiviazione dell'archivio (es. DSC_0001)

- %DATE%

  Sarà sostituito dalla data di scatto.(Es. 2018-01-01)

- %TIME%

  Sarà sostituito dal tempo di scatto.(Es. 13:01:10)

L'ultima riga della schermata mostra come apparirà la cartella di destinazione e il nome del file una volta archiviati.

### Crea una cartelle basata sull'ora di scatto per salvare i file

Crea una cartelle con indicazione dell'ora in cui memorizzare i file archiviati.

Quando viene selezionato, viene visualizzato il "Modello di nome della cartelle".

### Template Cartelle

Inserire gli schemi da utilizzare per rinominare la cartelle da creare (exp. DIR-% YEAR% -% MONTH% e così via). Premendo i tasti dei modelli, potete inserire le parole chiave dietro il cursore.

- %YEAR%

  Sarà sostituito dall'anno di riprese. (Es. 2018)

- %MONTH%

  Sarà sostituito dal Mese di scatto (Es. 01)

- %DAY%

  Sarà sostituito dal giorno delle riprese (Es. 29)

L'ultima riga della schermata mostra come apparirà la cartella di destinazione e il nome del file una volta archiviati.


### Informazioni dettagliate

https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing

 