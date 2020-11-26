### Modalità Test (non copia o cancella file)

Se spuntata, nessun file viene cancellato, copiato o sovrascritto. Non vengono apportate modifiche ai file sia sul target che sul master. Si prega di utilizzare la modalità Test per controllare i file che saranno cancellati/copiati/sostituiti durante la reale attività di sincronizzazione. Le operazioni eseguite sui file appariranno nella scheda Messaggi. 

### Auto sync

Se l'opzione è selezionata, l'attività può essere avviata automaticamente a intervalli pianificati nello scheduler. Le attività che sono impostate per la sincronizzazione automatica si avvieranno quando si preme il pulsante di sincronizzazione nell'angolo in alto a destra della schermata principale dell'applicazione. 

### Nome Sync task

Specificare un nome per l'attività. Il nome dell'attività di sincronizzazione non è sensibile alle maiuscole e minuscole. 

### Tipo di sync

Le modalità di sincronizzazione attualmente supportate sono Specchio, Copia, Spostamento o Archivio. <span style="color: red; "><u>La sincronizzazione avviene in una direzione del bersaglio dal master.</u></span>

- Mirror

  La cartella di destinazione viene conservata come copia esatta del master. Se un file è diverso tra il master e la destinazione, il file sul master sovrascrive il file sulla destinazione. La cartella e i file non presenti sulla destinazione vengono copiati dal master. Anche i file e le cartelle non presenti sul master vengono cancellati dal master. Solo i file modificati (per dimensione e/o data/ora) vengono aggiornati sulla destinazione. 

- Sposta

  Se un file è diverso tra il master e il target, il file sul master sovrascrive il file sul target. Una volta copiati nella destinazione, i file e le cartelle vengono cancellati dal master (come il comando sposta).

  Solo i file modificati (per dimensione e/o data/ora) vengono copiati nella destinazione. I file identici, in base ai criteri di confronto selezionati, vengono cancellati dal master senza essere copiati. I file e le cartelle sul target, non presenti sul master, vengono ovviamente conservati. 

- Copia

  Come per permeti, ma i file non vengono cancellati dal master dopo essere stati copiati.

  Se un file è diverso tra il master e la destinazione, il file sul master sovrascrive il file sulla destinazione. Una volta copiati nella destinazione, i file e le cartelle vengono mantenuti sul master (come un comando di copia).

  Solo i file modificati (per dimensione e/o data/ora) vengono copiati sulla destinazione. I file identici, in base ai criteri di confronto selezionati, vengono ignorati e non vengono più copiati. 

- Archivia

  Archiviare foto e video spostandoli dal master alla cartella di destinazione. Per l'archiviazione possono essere specificati criteri specifici: data/ora di ripresa, data e ora dell'ultima esecuzione dell'archivio (ad esempio 7 giorni o prima o 30 giorni o prima).

  ZIP non può essere specificato come target per le operazioni di archivio. 

**Criteri di comparazione:** 

I file sono considerati diversi in base a questi criteri:

1. Il nome del file/cartella esiste solo sul master o sul target, non su entrambi i lati

2. I file hanno dimensioni diverse

3. I file hanno un orario diverso (data e ora dell'ultima modifica) 

Controlla le opzioni Avanzate sotto per maggiori dettagli sui criteri di comparazione ed altre impostazioni granulari. 

### Inverti sorgente e destinazione

Scambiare le cartelle master e target: il master diventa il target e il target viene cambiato in master. 

### Cartella sorgente (Sorgente)

Toccare l'icona/nome della memoria per modificare la cartella principale. 

### Cartella destinazione (Destinazione)

Toccare l'icona/nome della memoria per modificare la cartella di destinazione. 

### Selezionare i file per la sincronizzazione

Se deselezionati, tutti i file sono sincronizzati. Se si seleziona il filtro dei file, si ottengono le seguenti opzioni:

- Sync file audio

  Quando spuntata, la sincronizzazione includerà i file con le seguenti estensioni:  
  aac, aif, aifc, aiff, flac, kar, m3u, m4a, mid, midi, mp2, mp3, mpga, ogg, ra, ram, wav

- Sync immagini

  Quando spuntata, la sincronizzazione includerà i file con le seguenti estensioni:  
  bmp, cgm, djv, djvu, gif, ico, ief, jpe, jpeg, jpg, pbm, pgm, png, pnm, ppm, ras, rgb, svg, tif, tiff, wbmp, xbm, xpm, xwd

- Sync file video

  Quando spuntata, la sincronizzazione includerà i file con le seguenti estensioni:  
  avi, m4u, mov, mp4, movie, mpe, mpeg, mpg, mxu, qt, wmv 

- Filtro file

  È un filtro di inclusione/esclusione dei file personalizzato. È possibile selezionare il nome e l'estensione dei file che si desidera escludere o includere dal processo di sincronizzazione. 

### Selezionare le sottocartelle

Se non viene selezionato, tutte le cartelle sono sincronizzate. Se si seleziona il filtro delle sottocartelle, si ottengono le seguenti opzioni:

- Filtro cartelle

  È un filtro di inclusione/esclusione cartelle personalizzato. È possibile selezionare il nome delle cartelle che si desidera escludere o includere dal processo di sincronizzazione. 

### Esegui sync tasks solo quando in carica

Le attività pianificate di Auto Sync non si avviano se il dispositivo non è in carica. L'avvio manuale è sempre possibile. 

### Synchronize i file nella root della cartella sorgente (se non selezionata, solo le cartelle e i loro file/sottocartelle verranno sincronizzati).

Se deselezionati, vengono sincronizzate solo le cartelle e i loro file/sottocartelle sotto la cartelle master. Per impostazione predefinita, è selezionata e anche i file che si trovano direttamente nella root della cartelle master saranno sincronizzati. 

### Conferma prima di sovrascrivere/cancellare

Se spuntata, visualizzerà una finestra di dialogo di conferma prima di sovrascrivere o cancellare i file. 

### Wifi AP Opzioni:

- Esegui anche da spento: cercherà di avviare la sincronizzazione anche se il Wifi è spento

- Conn a qualunque AP: si sincronizzerà solo se connesso ad una rete wifi. Accetterà qualsiasi nome SSID WLAN.

- Come indirizzo privato: è possibile avviare la sincronizzazione solo se l'indirizzo IP assegnato alla rete WiFi si trova nei seguenti campi: 10.0.0.0.0 - 10.255.255.255.255, 172.16.0.0.0 - 172.31.255.255.255 o 192.168.0.0.0 - 192.168.255.255.255.

- indirizzo lista IP: è possibile avviare la sincronizzazione solo se l'indirizzo IP WiFi corrisponde a uno degli indirizzi specificati. È inoltre possibile aggiungere direttamente l'indirizzo IP corrente al quale l'apparecchio è collegato attraverso l'elenco di selezione IP.  
  È possibile utilizzare i caratteri jolly per il filtro. (ad es. 192.168.100.\*, 192.168.\*).

Ci sono diversi modi per sincronizzarsi quando ci si connette ad un WiFi specifico. Vedi le FAQ per i dettagli.

1. 2. Cambiare l'indirizzo IP in qualcosa di diverso da 192.168.0.0.0/24 sul router WiFi
2. 3. Fissare l'indirizzo IP sul lato Android

### Salta il  task se l\'IP WLAN non corrisponde all\'indirizzo IP specificato

Auto Sync salterà l'attività se non corrisponde ai criteri specificati 

### Permetti sync con tutti gli indirizzi IP (inclusi pubblici)

La sincronizzazione sarà consentita ogni volta che il Wifi è connesso a qualsiasi rete, anche su intervalli IP pubblici (come in un Wifi pubblico).

### Mostra opzioni avanzate

**Si prega di utilizzarlo quando si impostano le opzioni dettagliate.** 

### Includi  sottocartelle

Includerà ricorsivamente le sottocartelle sotto la cartella master specificata. 

### Includi cartelle vuote

Sincronizza le cartelle vuote (anche se una cartelle è vuota sul master, verrà creata sul target). Se deselezionata, le cartelle vuote sul master vengono ignorate. 

### Includi cartelle nascoste

Quando spuntata, Sync includerà le cartelle linux nascoste (quelle con un nome che inizia con un punto). Si noti che in Windows e Samba, l'attributo nascosto non è impostato dal nome della cartella. Pertanto, la cartella sincronizzata sulla destinazione SMB/Windows non avrà l'attributo nascosto dell'host. 

### Includi file nascosti

Quando spuntata, Sync includerà i file linux nascosti (quelli con un nome che inizia con un punto). Si noti che in Windows e Samba, l'attributo nascosto non è impostato dal nome del file. Quindi, il file sincronizzato sulla destinazione SMB/Windows non avrà l'attributo nascosto dell'host. 

### Sovrascrivi file destinazione

Se deselezionati, i file sul target non saranno mai sovrascritti anche se i criteri di confronto per dimensioni e tempo sono diversi. 

### Usa il  filtro di selezione cartelle migliorato

Se la cartelle superiore viene esclusa da un filtro mentre una delle sue sottocartelle viene selezionata/inclusa, le sottocartelle della cartella selezionata verranno sincronizzate. 

### Usa la  nuova versione 2 del filtro

Dopo averla abilitata, puoi usare il nuovo filtro. <span style="color: red; "><u>It may behave differently from the old filter, so please test it thoroughly before using it.</u></span> 

### Rimuovi cartelle e file esclusi dai filtri

Se abilitato, **rimuove cartelle/file che sono esclusi dai filtri.** 

### Riprova ad errori di rete

In caso di errori di connessione lato server, SMBSync2 riproverà la sincronizzazione per un massimo di 3 volte ad un intervallo di 30 secondi. 

### Limita buffer di scrittura SMB I/O a 16 KB

**Per favore provala se in presenza dell'errore "Accesso negato" error durante la scrittura sulle cartelle del PC/NAS.**

Quando spuntata, limiterà il buffer di I/O a 16KB per le operazioni di scrittura sull'host SMB. 

### Scrivi file direttamente sulla cartella SMB senza usare file temporanei

Controllato di default (consigliato). Quando viene copiato nell'host SMB, il file verrà copiato in una cartella temporanea sull'host. Una volta che l'operazione di copia è riuscita, il file temporaneo viene spostato nella sua destinazione finale sovrascrivendo il file di destinazione. Se deselezionato, il file di destinazione sull'host viene immediatamente sovrascritto all'inizio della copia. Se si verifica un errore di connessione, il file sull'host rimane danneggiato fino alla successiva sincronizzazione. 

### Non settare l'ultima volta di modifica del file destinazione per trovare il file sorgente

Si prega di abilitare se si ottiene un errore come SmbFile#setLastModified()/File#setLastModified() fallisce. Significa che l'host remoto non permette l'impostazione del file modificato l'ultima volta. Se deselezionato, l'ultimo tempo modificato del file copiato sul target sarà impostato all'ora in cui è stato copiato / sincronizzato. Ciò significa che il file di destinazione apparirà più nuovo del master. 

Per le sincronizzazioni successive, è possibile:

- attenersi a confrontare solo in base alle dimensioni, oppure

- è possibile attivare l'opzione "Non sovrascrivere il file di destinazione se è più recente del file sorgente" per copiare solo i file modificati in seguito sul master, oppure

- è possibile attivare l'opzione task "Ottenere l'ora dell'ultima modifica dei file dalla lista personalizzata dell'applicazione SMBSync2". Tuttavia, questa opzione non è attualmente disponibile se l'obiettivo è SMB. La maggior parte degli host SMB supporta l'impostazione dell'ultimo orario di modifica. 

Vedere sotto per informazioni dettagliate su ogni opzione. 

### Ottieni l'ora di ultima modifica dei file dalla lista personalizzata di SMBSync2

Si prega di provare se tutti i file vengono copiati sopra i file di destinazione ad ogni sincronizzazione.

Questa opzione è disponibile solo per l'utente quando la destinazione è la memorizzazione interna. Per altri tipi di archiviazione, SMBSync2 cerca di rilevare automaticamente se la destinazione supporta l'impostazione dell'ultima volta che il file è stato modificato. In caso contrario, utilizzerà automaticamente questa funzione integrata. Un'eccezione è quando il target è SMB, questa funzione non verrà utilizzata. Vedere sopra l'opzione "Non impostare l'ora dell'ultima modifica del file di destinazione per far corrispondere il file di origine" per altre soluzioni. Si noti che i dispositivi Android più recenti non supportano l'aggiornamento dell'ultimo orario di modifica del file di destinazione.

Durante la prima sincronizzazione, SMBSync2 memorizzerà la lista dei file con i loro timestamp nel suo database (cfr. 1.3). Poiché il database deve essere creato la prima volta, tutti i file con lo stesso nome e le stesse dimensioni saranno sovrascritti di nuovo con una copia completa dal master durante la prima sincronizzazione. Nelle sincronizzazioni successive, il database verrà utilizzato e non verrà più eseguita la sovrascrittura di tutti i file con lo stesso nome/stessa dimensione. L'ultimo tempo modificato del file locale viene salvato dal codice proprietario SMBSync (non si basa sulla funzione Java File#setLastModified()).

 

### Elimina precedenti file su sync (Solo modalità Mirror)

Una volta spuntate, le cartelle e i file che sono presenti nella cartella di destinazione ma che non esistono sul master, verranno prima cancellati. Dopo di che, i file e le cartelle che sono diversi saranno copiati nella cartella di destinazione.

Se la cartella master è SMB, il tempo di elaborazione sarà più lungo perché la struttura delle cartelle e il loro contenuto viene scansionato attraverso la rete. Si raccomanda vivamente di attivare l'opzione "Usa la negoziazione SMB2" perché SMB1 sarà molto lenta. 

### Usa dimensione file per determinare se i file sono differenti

Quando si seleziona, i file sono considerati diversi se differiscono per dimensioni. 

### Compara solo dimensione

I file sono considerati diversi solo se la dimensione della fonte è maggiore della destinazione. Questo disabiliterà il confronto in base al tempo del file. 

### Usa tempo dell'ultima modifica per determinare se i file sono differenti 

Quando si seleziona, i file sono considerati diversi in base al loro ultimo tempo di modifica  

### Differenza min di ora permessa (in secondi) tra i file sorgente e destinazione

I file sono considerati identici se la differenza tra i loro ultimi tempi modificati è inferiore o uguale al tempo selezionato in secondi. Sono considerati diversi se la differenza di tempo tra i file è superiore al tempo selezionato. FAT e ExFAT richiedono una tolleranza minima di 2 secondi. Se si seleziona 0 secondi, i file devono avere esattamente lo stesso tempo per essere considerati simili. 

### Non sovrascrivere i file destinazione se più nuovi dei file sorgente

Se spuntata, il file viene sovrascritto solo quando il file master è più nuovo del file di destinazione, anche se le dimensioni del file e gli ultimi tempi di aggiornamento sono diversi. Tenere presente che se si cambiano i fusi orari o se i file vengono modificati durante il periodo di intervallo della modifica dell'ora legale, l'ultimo file modificato potrebbe apparire più vecchio del file non aggiornato. Ciò è legato alle differenze del file system e solo un controllo manuale prima di sovrascrivere il file eviterà la perdita di dati. Si raccomanda generalmente di non modificare i file durante l'intervallo del cambio dell'ora legale se sono destinati ad essere auto-sincronizzati.  

### Ignora differenza dell'Ora Legale tra file

Permette di selezionare la differenza di fuso orario in minuti tra l'ora legale e quella invernale. I file sono considerati diversi se la differenza di orario non è esattamente uguale all'intervallo specificato (+/- la "differenza di orario minima consentita (in secondi)" specificata nell'opzione precedente) 

###  Salta cartelle e file che contengono caratteri non validi(", :, \, *, <, >, |)

Se spuntata, visualizzerà un messaggio di avviso e la sincronizzazione continuerà senza elaborare le cartelle/file contenenti caratteri non validi. 

###  Elimina la cartella master quando è vuota (solo quando l'opzione di Sync è Sposta)

Quando la modalità di sincronizzazione è Sposta, dopo che i file sono stati spostati nella destinazione, viene cancellata anche la cartella Master. 

### Informazioni dettagliate

https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing

 