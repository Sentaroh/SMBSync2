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

###Filtro del nome del file  

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

###Filtro dimensione file  

Puoi scegliere quali file sincronizzare a seconda della loro dimensione.  

- Meno di/più grande di  
Puoi specificare qualsiasi dimensione di file.   

###Filtro data ultima modifica file  
Potete selezionare un file in base alla sua ultima data di modifica.  

- Se si specifica 1 giorno più vecchio di seleziona i file la cui ultima data di modifica è compresa tra la data di inizio e il giorno precedente la data di inizio.  
- Se specifichi Più vecchio di 1 giorno seleziona i file la cui ultima data di modifica è precedente al giorno prima dell'inizio della sincronizzazione.  
- Dopo la data di inizio della sincronizzazione, verranno selezionati i file la cui ultima data di modifica è la data di inizio della sincronizzazione. (Se la sincronizzazione termina il giorno successivo, i file saranno selezionati dopo la data di inizio).  

### Selezionare le sottocartelle  

Se deselezionato, tutte le sottodirectory saranno sincronizzate. Se spuntato, apparirà un pulsante di filtro della directory.  

### Esegui sync tasks solo quando in carica  
Le attività pianificate di Auto Sync non si avviano se il dispositivo non è in carica. L'avvio manuale è sempre possibile.   

### Synchronize i file nella root della cartella sorgente (se non selezionata, solo le cartelle e i loro file/sottocartelle verranno sincronizzati).  

Se deselezionati, vengono sincronizzate solo le cartelle e i loro file/sottocartelle sotto la cartelle master. Per impostazione predefinita, è selezionata e anche i file che si trovano direttamente nella root della cartelle master saranno sincronizzati.   

### Conferma prima di sovrascrivere/cancellare  
Se spuntata, visualizzerà una finestra di dialogo di conferma prima di sovrascrivere o cancellare i file.   

### WiFi AP Opzioni  
È possibile impostare se la sincronizzazione può essere avviata dallo stato della rete.  

- Esegui anche da spento: cercherà di avviare la sincronizzazione anche se il WiFi è spento  

- Conn a qualunque AP: si sincronizzerà solo se connesso ad una rete WiFi. Accetterà qualsiasi nome SSID WLAN.  
Si sincronizza solo quando è collegato a una rete WiFi. Accetterà qualsiasi nome SSID WLAN.  
- Come indirizzo privato  
è possibile avviare la sincronizzazione solo se l'indirizzo IP assegnato alla rete WiFi si trova nei seguenti campi: 10.0.0.0.0 - 10.255.255.255.255, 172.16.0.0.0 - 172.31.255.255.255 o 192.168.0.0.0 - 192.168.255.255.255.  
- indirizzo lista IP  
è possibile avviare la sincronizzazione solo se l'indirizzo IP WiFi corrisponde a uno degli indirizzi specificati.  
È inoltre possibile aggiungere direttamente l'indirizzo IP corrente al quale l'apparecchio è collegato attraverso l'elenco di selezione IP.È possibile utilizzare i caratteri jolly per il filtro. (ad es. 192.168.100.\*, 192.168.\*).  

Ci sono diversi modi per sincronizzare quando ci si connette a un WiFi specifico. Vedi le [FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm) per i dettagli.  

1. Cambiare l'indirizzo IP in qualcosa di diverso da 192.168.0.0.0/24 sul router WiFi e aggiungerlo alla lista degli indirizzi IP  
2. Fissare un indirizzo IP sul lato Android e registrarlo nell'elenco degli indirizzi IP  

### Salta iltask se l\'IP WLAN non corrisponde all\'indirizzo IP specificato  

Auto Sync salterà l'attività se non corrisponde ai criteri specificati   

### Permetti sync con tutti gli indirizzi IP (inclusi pubblici)  

La sincronizzazione sarà consentita ogni volta che il WiFi è connesso a qualsiasi rete, anche su intervalli IP pubblici (come in un WiFi pubblico).  

### Mostra opzioni avanzate  

**Si prega di utilizzarlo quando si impostano le opzioni dettagliate.**   

### Includisottocartelle  

Includerà ricorsivamente le sottocartelle sotto la cartella master specificata.   

### Includi cartelle vuote  

Sincronizza le cartelle vuote (anche se una cartelle è vuota sul master, verrà creata sul target). Se deselezionata, le cartelle vuote sul master vengono ignorate.   

### Includi cartelle nascoste  

Quando spuntata, Sync includerà le cartelle linux nascoste (quelle con un nome che inizia con un punto). Si noti che in Windows e Samba, l'attributo nascosto non è impostato dal nome della cartella. Pertanto, la cartella sincronizzata sulla destinazione SMB/Windows non avrà l'attributo nascosto dell'host.   

### Includi file nascosti  

Quando spuntata, Sync includerà i file linux nascosti (quelli con un nome che inizia con un punto). Si noti che in Windows e Samba, l'attributo nascosto non è impostato dal nome del file. Quindi, il file sincronizzato sulla destinazione SMB/Windows non avrà l'attributo nascosto dell'host.   

### Sovrascrivi file destinazione  
Se deselezionati, i file sul target non saranno mai sovrascritti anche se i criteri di confronto per dimensioni e tempo sono diversi.   

### Usa ilfiltro di selezione cartelle migliorato  

Se la cartelle superiore viene esclusa da un filtro mentre una delle sue sottocartelle viene selezionata/inclusa, le sottocartelle della cartella selezionata verranno sincronizzate.   

### Usa lanuova versione 2 del filtro  

Dopo averla abilitata, puoi usare il nuovo filtro. <span style="color: red; "><u>It may behave differently from the old filter, so please test it thoroughly before using it.</u></span>   

### Riprova ad errori di rete  

Quando è selezionata, elimina prima le directory e i file che non esistono nella cartella principale, e poi copia i file che hanno dimensioni e tempi di ultima modifica diversi dalla cartella principale. Se la cartella principale è SMB, il tempo di elaborazione sarà più lungo perché i file vengono scansionati attraverso la rete. Se possibile, usate il protocollo SMB "SMBv2/3".  

### Rimuovi cartelle e file esclusi dai filtri di nomi  

Se abilitato, **rimuove cartelle/file che sono esclusi dai filtri.**   

### Riprova se vi sono errori di rete  
Riprova la sincronizzazione solo in caso di errore del server SMB. Vengono eseguiti fino a tre tentativi, ciascuno dopo che sono trascorsi 30 secondi dal verificarsi dell'errore.  

### Limita buffer scrittura SMB I/O a 16 KB  

Quando spuntata, limiterà il buffer di I/O a 16KB per le operazioni di scrittura sull'host SMB. 
**Per favore provala se in presenza dell'errore "Accesso negato" error durante la scrittura sulle cartelle del PC/NAS.**  

### Scrivi file direttamente sulla cartella SMB senza usare file temporanei  

Se deselezionato, riscriverà direttamente il file sul server SMB di destinazione, il che romperà il file se l'aggiornamento viene interrotto da <span style="color: red;"><u>errore o cancellazione.</u></span>  

### Non settare l'ultima volta di modifica del file destinazione per trovare il file sorgente  

Si prega di abilitare se si ottiene un errore come SmbFile.setLastModified()/File.setLastModified() fallisce. Significa che l'host remoto non permette l'impostazione del file modificato l'ultima volta. Se deselezionato, l'ultimo tempo modificato del file copiato sul target sarà impostato all'ora in cui è stato copiato / sincronizzato. Ciò significa che il file di destinazione apparirà più nuovo del master.   

### Ottieni l'ora dell'ultima modifica dei file dalla lista personalizzata di SMBSync2  

Prova questo se tutti i file vengono copiati ogni volta. mantenere la data e l'ora dell'ultima modifica dei file locali usando il metodo proprio di SMBSync2 invece di usare Java File.setLastModified().  

### Ottieni l'ora di ultima modifica dei file dalla lista personalizzata di SMBSync2  

Si prega di provare se tutti i file vengono copiati sopra i file di destinazione ad ogni sincronizzazione.  

### Compara solo dimensione (i file sono considerati differenti solo se la dimensione della sorgente è più grande della destinazione)  

Quando è spuntato, solo quando la dimensione del file principale è grande, viene preso di mira per la sincronizzazione.  

### Usa tempo dell\'ultima modifica per determinare se i file sono differenti  
Quando è selezionato, se l'ultima ora modificata del file è diversa, viene giudicato come un file di differenza.  

Min differenza di tempo ammessa (in secondi) tra file sorgente e destinazione  
Scegli tra 1, 3 e 10 secondi. Se la differenza dell'ora dell'ultima modifica del file è all'interno della differenza di tempo selezionata, non si assume alcun cambiamento.  

### Non sovrascrivere il file destinazione se è più nuovo del file sorgente  
Se spuntato, il file viene sovrascritto solo quando il file master è più nuovo del file di destinazione, anche se la dimensione del file e l'ultimo tempo modificato sono diversi.  

### Ignora la differenza dell\'Ora Legale tra file  
Se selezionata, la differenza di orario tra l'ora legale e l'ora solare viene ignorata.  

### Differenza di ora tra ora legale e ora solare (minuti)  
Specificare la differenza di orario da ignorare.  

### Salta nomi cartelle e file che contengono caratteri non validi （"，：，\，*，<，>, |）  

Se spuntato, il sistema non processerà le directory/file che contengono caratteri non disponibili, ma mostrerà un messaggio di avvertimento e processerà la directory/file successiva.  

### Ignora i file con una dimensione di 0 byte  

Se spuntato, i file con una dimensione di 0 byte vengono ignorati.  

### Ignora il file se la lunghezza del nome del file è più lunga del valore specificato  

Specifica la lunghezza massima (in byte) del nome del file di output. Ignora il file se il numero di byte nel nome del file supera il massimo.  

### Manuali  
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_JA.htm)  
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_JA.htm)   
