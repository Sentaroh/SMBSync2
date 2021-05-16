## 1.Dati raccolti<br>
### 1.1.Dati forniti dall'utente a SMBSync2<br>

I dati forniti dall'utente per usare SMBSync2 saranno salvati nell'area di archiviazione dell'applicazione.<br>
Quando si memorizzano i dati, il nome dell'account SMB, la password dell'account SMB, la password ZIP e la password dell'applicazione sono criptati con una password generata dal sistema.<br>
<span style="color: red;"><u>I dati non saranno inviati all'esterno a meno che non venga eseguita l'operazione "1.4.Invio o scrittura di dati al di fuori di SMBSync2".</u></span>.<br>

- Informazioni sul file (nome della directory, nome del file)<br>
- Informazioni sul server SMB (nome host/indirizzo IP, numero di porta, nome dell'account, password dell'account)<br>
- Informazioni sul file ZIP (metodo di compressione, metodo di crittografia, password di crittografia)<br>
- Opzioni di impostazione delle applicazioni (messaggi di avviso, lingua e dimensione dei caratteri, ecc.)<br>
- Password dell'applicazione (password usata per l'autenticazione all'avvio dell'applicazione, autenticazione quando si cambiano le impostazioni di sicurezza, ecc.)<br>

### 1.2.Risultato dell'esecuzione di SMBSync2<br>

Salva i dati nell'area di memorizzazione dell'applicazione in modo che l'utente possa controllare il risultato dell'esecuzione di SMBSync2.<br>
<span style="color: red;"><u>I dati non saranno inviati all'esterno a meno che non venga eseguita l'operazione "1.4.Invio o scrittura di dati al di fuori di SMBSync2".</u></span>.<br>

- Nome della directory, nome del file, stato di esecuzione<br>
- Dimensione dei file sincronizzati, data e ora di aggiornamento dei file<br>
- Informazioni di errore<br>

### 1.3.Registro di attività di SMBSync2<br>

Salva i dati nell'area di memorizzazione dell'applicazione per verificare il risultato dell'esecuzione di SMBSync2 e per informare lo sviluppatore.<br>
<span style="color: red;"><u>I dati non saranno inviati all'esterno a meno che non venga eseguita l'operazione "1.4.Invio o scrittura di dati al di fuori di SMBSync2".</u></span>.<br>

- Informazioni sul dispositivo (nome del produttore, nome del modello, versione del sistema operativo, punto di montaggio, directory specifica dell'applicazione, StorageAccessFramework, gestore dello storage, indirizzo IP, abilitazione/disab<br>
- Versione SMBSync2, opzioni di esecuzione SMBSync2<br>
- Nome della directory, nome del file, stato di esecuzione<br>
- Dimensione dei file sincronizzati, data e ora di aggiornamento dei file<br>
- Informazioni di debug<br>
- Informazioni di errore<br>

### 1.4.Invio o scrittura di dati al di fuori di SMBSync2<br>

I dati di SMBSync2 non possono essere inviati o scritti all'esterno a meno che non sia l'utente a farlo.<br>

- Premi [Pulsante di condivisione] dalla scheda Storia.<br>
- Fare clic sul pulsante "Invia allo sviluppatore" da Informazioni di sistema.<br>
- Cliquez sur le bouton "Envoyer au développeur" à partir de la gestion du journal.<br>
- Eseguendo "Impostazioni di esportazione" dal menu, "1.1.Dati forniti dall'utente a SMBSync2" saranno esportati.<br>
Specificando una password durante l'esportazione, le informazioni vengono criptate e salvate nel file.<br>

### 1.5.Cancellare i dati memorizzati in SMBSync2<br>

Disinstallando SMBSync2, "1.1. Dati forniti dagli utenti a SMBSync2" e "1.3. Record di attività di SMBSync2" saranno cancellati dal dispositivo.<br>
<span style="color: red;"><u>Tuttavia, le seguenti informazioni non saranno cancellate, quindi per favore cancella la directory "/storage/emulated/0/SMBSync2" e i file salvati nel file manager. </u></span>.<br>

- "1.2.Risultato dell'esecuzione di SMBSync2"<br>
- Messaggio visualizzato (circa 5000 linee)<br>
- Informazioni sul salvataggio dell'elenco delle attività<br>
- Data e ora dell'aggiornamento del file (se il modello non permette l'impostazione)<br>
- Informazioni salvate nella memoria esterna salvando l'elenco attività<br>

### 2.Permessi richiesti per eseguire l'applicazione<br>

### 2.1.Foto, media, file<br>
**leggere il contenuto della vostra memoria USB**.<br>
**modificare o cancellare il contenuto della vostra memoria USB**.<br>
Utilizzato per la sincronizzazione dei file e la lettura/scrittura di file di gestione.<br>

### 2.2.Stoccaggio<br>
**leggere il contenuto della vostra memoria USB**.<br>
**modificare o cancellare il contenuto della vostra memoria USB**.<br>
Utilizzato per la sincronizzazione dei file e la lettura/scrittura di file di gestione.<br>

### 2.3.Informazioni sulla connessione Wi-Fi<br>
**vedi le connessioni Wi-Fi**.<br>
Utilizzato per controllare lo stato del Wi-Fi quando inizia la sincronizzazione.<br>

### 2.4.Altri<br>
### 2.4.1.View network connections<br>
Usalo per controllare le connessioni di rete quando viene avviata la sincronizzazione.<br>
### 2.4.2.connect and disconnect from Wi-Fi<br>
Questa funzione è usata per attivare/disattivare il Wi-Fi per la sincronizzazione programmata su Andoid 8/9.<br>
### 2.4.3.Full network access<br>
Questo è usato per sincronizzare tramite il protocollo SMB attraverso la rete.<br>
### 2.4.4.Run at startup<br>
Utilizzato per eseguire la sincronizzazione programmata.<br>
### 2.4.5.Control vibration<br>
Questo è usato per notificare all'utente quando la sincronizzazione è finita.<br>
### 2.4.6.Prevent device from sleeping<br>
Utilizzato per avviare la sincronizzazione da una pianificazione o da un'applicazione esterna.<br>
### 2.4.7.Install shortcuts<br>
Utilizzato per aggiungere un collegamento di avvio della sincronizzazione al desktop.<br>
