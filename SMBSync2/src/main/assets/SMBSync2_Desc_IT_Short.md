## 1. Funzioni  
SMBSync2 è uno strumento per la sincronizzazione di file via LAN wireless utilizzando il protocollo SMB1, SMB2 o SMB3 tra la memoria interna del terminale Android, SDCARD/USB-OTG e PC/NAS. La sincronizzazione è a senso unico dal master all'obiettivo. Sono supportate le modalità Mirror, Sposta, Copia e Archivia. Sono supportate molte combinazioni di archiviazione (archiviazione interna, SDCARD, USB-OTG, SMB, ZIP)La sincronizzazione può essere avviata automaticamente da applicazioni esterne (Tasker, AutoMagic ecc.) o dal pianificatore SMBSync2.   
La sincronizzazione avviene tra due coppie di cartelle chiamate Master (cartella sorgente) e Target (cartella di destinazione). Si tratta di una sincronizzazione in una sola direzione, dal Master al Target.  
Le modalità di sincronizzazione supportate sono:  

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
  Spostare le foto e i video nella directory del master se sono state scattate prima di 7 giorni o 30 giorni prima della data di esecuzione dell'archivio. (Tuttavia, non è possibile utilizzare lo zip per raggiungere l'obiettivo).  
I seguenti tipi di file possono essere archiviati.  
gif, "jpg", "jpeg", "jpe", "png", "mp4", "mov".  

**Criteri di comparazione:**   
I file sono considerati diversi in base a questi criteri:  

1. Il nome del file esiste solo sul master o sul target, non su entrambi i lati  
2. I file hanno dimensioni diverse  
3. I file hanno un orario diverso (data e ora dell'ultima modifica)   

Nelle Opzioni Avanzate è possibile regolare molte impostazioni di confronto. (Ecco un esempio)  
- L'intervallo di tolleranza temporale può essere impostato in modo da ignorare la differenza se inferiore a 1, 3, 5 o 10 sec per la compatibilità con i media FAT/exFAT.   
- Ignora l'ora legale è supportata.   
- Opzione per non sovrascrivere il file di destinazione se è più nuovo del master o se è di dimensioni maggiori.  

## 2.Documents  
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm)  
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_EN.htm)  
