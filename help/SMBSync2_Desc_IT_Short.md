## 1. Funzioni

SMBSync2 è uno strumento per la sincronizzazione di file via LAN wireless utilizzando il protocollo SMB1, SMB2 o SMB3 tra la memoria interna del terminale Android, SDCARD e PC/NAS. La sincronizzazione è a senso unico dal master all'obiettivo. Sono supportate le modalità Mirror, Move, Copy e Archive. Sono supportate molte combinazioni di archiviazione (archiviazione interna, SDCARD, OTG-USB, SMB, ZIP)La sincronizzazione può essere avviata automaticamente da applicazioni esterne (Tasker, AutoMagic ecc.) o da SMBSync2 schedule. 
La sincronizzazione avviene tra due coppie di cartelle chiamate Master (cartella sorgente) e Target (cartella di destinazione). Si tratta di una sincronizzazione in una sola direzione, dal Master al Target.
Le modalità di sincronizzazione supportate sono:
- Mirror

  La cartella di destinazione viene conservata come copia esatta del master. Se un file è diverso tra il master e la destinazione, il file sul master sovrascrive il file sulla destinazione. La cartella e i file non presenti sulla destinazione vengono copiati dal master. Anche i file e le cartelle non presenti sul master vengono cancellati dal master. Solo i file modificati (per dimensione e/o data/ora) vengono aggiornati sulla destinazione.

- Permeti

  Se un file è diverso tra il master e il target, il file sul master sovrascrive il file sul target. Una volta copiati nella destinazione, i file e le cartelle vengono cancellati dal master (come il comando move).
  Solo i file modificati (per dimensione e/o data/ora) vengono copiati nella destinazione. I file identici, in base ai criteri di confronto selezionati, vengono cancellati dal master senza essere copiati. I file e le cartelle sul target, non presenti sul master, vengono ovviamente conservati.

- Copia

  Come per permeti, ma i file non vengono cancellati dal master dopo essere stati copiati.
  Se un file è diverso tra il master e la destinazione, il file sul master sovrascrive il file sulla destinazione. Una volta copiati nella destinazione, i file e le cartelle vengono mantenuti sul master (come un comando di copia).
  Solo i file modificati (per dimensione e/o data/ora) vengono copiati sulla destinazione. I file identici, in base ai criteri di confronto selezionati, vengono ignorati e non vengono più copiati.

- Archivia

  Archiviare foto e video spostandoli dal master alla cartella di destinazione. Per l'archiviazione possono essere specificati criteri specifici: data/ora di ripresa, data e ora dell'ultima esecuzione dell'archivio (ad esempio 7 giorni o prima o 30 giorni o prima).
  ZIP non può essere specificato come target per le operazioni di archivio.

**Compare criteria:** 
I file sono considerati diversi in base a questi criteri:
1. Il nome del file/cartella esiste solo sul master o sul target, non su entrambi i lati
2. I file hanno dimensioni diverse
3. I file hanno un orario diverso (data e ora dell'ultima modifica) 
Nelle Opzioni Avanzate è possibile regolare molte impostazioni di confronto: l'intervallo di tolleranza temporale può essere impostato per ignorare la differenza se inferiore a 1, 3, 5 o 10 sec per la compatibilità con i media FAT/exFAT. Ignora l'ora legale è supportata. Opzione per non sovrascrivere il file di destinazione se è più nuovo del master o se è di dimensioni maggiori...

Quando il target si trova nella memoria interna o sulla scheda SD, la maggior parte dei sistemi Android non consente di impostare l'ultimo orario modificato del file di destinazione in modo che corrisponda all'orario del file di origine. Quando il target è SMB (PC/NAS), o storage OTG-USB, di solito questo non è un problema. SMSync2 rileva se l'ora/data può essere impostata sulla destinazione per corrispondere al file di origine. In caso contrario, l'ultimo tempo di aggiornamento del file viene registrato nei file del database dell'applicazione. Viene quindi utilizzato per confrontare i file e verificare se differiscono per tempo. In tal caso, se si tenta di sincronizzare la coppia master/target con un'applicazione di terze parti o se i file di dati SMBSync2 vengono cancellati, i file di origine vengono copiati nuovamente sul target. È possibile impostare l'opzione "Non sovrascrivere il file di destinazione se è più recente del master", oltre al confronto per dimensioni per superare questo problema.
## 2.FAQs
Si prega di fare riferimento al link PDF qui sotto.
https://drive.google.com/file/d/1a8CTRu9xoCD74Qn0YZxzry-LHxQ8j7dE/view?usp=sharing
## 3. Libreria
- [jcifs-ng ClientLibrary](https://github.com/AgNO3/jcifs-ng)
- [jcifs-1.3.17](https://jcifs.samba.org/)
- [Zip4J 1.3.2](https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j/1.3.2)
- [Xmpcore-5.1.3](https://www.adobe.com/devnet/xmp.html)
- [Metadata-extractor](https://github.com/drewnoakes/metadata-extractor)
## 4. Documenti
Si prega di fare riferimento al link PDF qui sotto.
https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing