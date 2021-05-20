## 1.Dati raccolti  
### 1.1.Dati forniti dall'utente a SMBSync2  

I dati forniti dall'utente per usare SMBSync2 saranno salvati nell'area di archiviazione dell'applicazione.  
<span style="color: red;"><u>I dati non saranno inviati all'esterno a meno che non venga eseguita l'operazione "1.4.Invio o scrittura di dati al di fuori di SMBSync2".</u></span>.  

- Informazioni sul file (nome della directory, nome del file)  
- Informazioni sul server SMB se si usa un server SMB (nome host/indirizzo IP, numero di porta, nome dell'account(**<u>\*1</u>**), password dell'account(**<u>\*1</u>**))  
- Informazioni sul file ZIP se si usa un file ZIP (metodo di compressione, metodo di crittografia, password di crittografia(**<u>\*1</u>**))  
- Opzioni di impostazione delle applicazioni (messaggi di avviso, lingua e dimensione dei caratteri, ecc.)  
- Password dell'applicazione(**<u>\*1</u>**)  

**<u>\*1</u>**I dati sono criptati e conservati.  

### 1.2.Risultato dell'esecuzione di SMBSync2  

Salva i dati nell'area di memorizzazione dell'applicazione in modo che l'utente possa controllare il risultato dell'esecuzione di SMBSync2.  
<span style="color: red;"><u>I dati non saranno inviati all'esterno a meno che non venga eseguita l'operazione "1.4.Invio o scrittura di dati al di fuori di SMBSync2".</u></span>.  

- Nome della directory, nome del file, stato di esecuzione  
- Dimensione dei file sincronizzati, data e ora di aggiornamento dei file  
- Informazioni di errore  

### 1.3.Registro di attività di SMBSync2  

Abilitando la registrazione verranno salvati i dati di registrazione delle attività nell'area di memoria dell'app per la verifica dei risultati di esecuzione dell'app e per il supporto tecnico. Se la registrazione è disabilitata, la registrazione dei dati sarà interrotta, ma i dati già registrati non saranno cancellati.  
<span style="color: red;"><u>I dati non saranno inviati all'esterno a meno che non venga eseguita l'operazione "1.4.Invio o scrittura di dati al di fuori di SMBSync2".</u></span>.  

- Informazioni sul dispositivo (nome del produttore, nome del modello, versione del sistema operativo, punto di montaggio, directory specifica dell'applicazione, StorageAccessFramework, gestore dello storage, indirizzo IP, abilitazione/disab  
- Versione SMBSync2, opzioni di esecuzione SMBSync2  
- Nome della directory, nome del file, stato di esecuzione  
- Dimensione dei file sincronizzati, data e ora di aggiornamento dei file  
- Informazioni di debug  
- Informazioni di errore  

### 1.4.Invio o scrittura di dati al di fuori di SMBSync2  

I dati di SMBSync2 non possono essere inviati o scritti all'esterno a meno che non sia l'utente a farlo.  

- Premi [Pulsante di condivisione] dalla scheda Storia.  
- Fare clic sul pulsante "Invia allo sviluppatore" da Informazioni di sistema.  
- Cliquez sur le bouton "Envoyer au développeur" à partir de la gestion du journal.  
- Eseguendo "Impostazioni di esportazione" dal menu, "1.1.Dati forniti dall'utente a SMBSync2" saranno esportati.  
Specificando una password durante l'esportazione, le informazioni vengono criptate e salvate nel file.  

### 1.5.Cancellare i dati memorizzati in SMBSync2  

Disinstallando SMBSync2, "1.1. Dati forniti dagli utenti a SMBSync2" e "1.3. Record di attività di SMBSync2" saranno cancellati dal dispositivo.  
<span style="color: red;"><u>Tuttavia, le seguenti informazioni non saranno cancellate, quindi per favore cancella la directory "/storage/emulated/0/SMBSync2" e i file salvati nel file manager. </u></span>.  

- "1.2.Risultato dell'esecuzione di SMBSync2"  
- Messaggio visualizzato (circa 5000 linee)  
- Informazioni sul salvataggio dell'elenco delle attività  
- Data e ora dell'aggiornamento del file (se il modello non permette l'impostazione)  
- Informazioni salvate nella memoria esterna salvando l'elenco attività  

### 2.Permessi richiesti per eseguire l'applicazione  

### 2.1.Foto, media, file  
**leggere il contenuto della vostra memoria USB**.  
**modificare o cancellare il contenuto della vostra memoria USB**.  
Utilizzato per la sincronizzazione dei file e la lettura/scrittura di file di gestione.  

### 2.2.Stoccaggio  
**leggere il contenuto della vostra memoria USB**.  
**modificare o cancellare il contenuto della vostra memoria USB**.  
Utilizzato per la sincronizzazione dei file e la lettura/scrittura di file di gestione.  

### 2.3.Informazioni sulla connessione Wi-Fi  
**vedi le connessioni Wi-Fi**.  
Utilizzato per controllare lo stato del Wi-Fi quando inizia la sincronizzazione.  

### 2.4.Altri  
### 2.4.1.View network connections  
Usalo per controllare le connessioni di rete quando viene avviata la sincronizzazione.  
### 2.4.2.connect and disconnect from Wi-Fi  
Questa funzione è usata per attivare/disattivare il Wi-Fi per la sincronizzazione programmata su Andoid 5/6/7/8/9.  
### 2.4.3.Full network access  
Questo è usato per sincronizzare tramite il protocollo SMB attraverso la rete.  
### 2.4.4.Run at startup  
Utilizzato per eseguire la sincronizzazione programmata.  
### 2.4.5.Control vibration  
Questo è usato per notificare all'utente quando la sincronizzazione è finita.  
### 2.4.6.Prevent device from sleeping  
Utilizzato per avviare la sincronizzazione da una pianificazione o da un'applicazione esterna.  
### 2.4.7.Install shortcuts  
Utilizzato per aggiungere un collegamento di avvio della sincronizzazione al desktop.  
