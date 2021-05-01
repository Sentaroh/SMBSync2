## 1. Dati registrati dall'applicazione

L'app registrerà un "Elenco attività di sincronizzazione" e, a seconda delle impostazioni, un "Registro attività dell'app". <span style="color: red; "><u>Inoltre, i dati registrati non verranno inviati dall'app a meno che l'utente non li abbia manipolati.</u></span>

### 1.1. Elenco dei compiti di sincronizzazione

L'app registra i dati necessari per eseguire la sincronizzazione.

- Nome della cartella, nome del file, nome host del server SMB, indirizzo IP, numero di porta, nome dell&#39;account, password (***1**)
- Password dell'applicazione (***1**) per proteggere il lancio dell'applicazione e la modifica delle impostazioni
- Impostazioni dell'applicazione

***1** la password è criptata con una password generata dal sistema e memorizzata nel Keystore Android.

### 1.2. Record di attività dell'applicazione

L'app deve registrare i seguenti dati per verificare i risultati della sincronizzazione e per la risoluzione dei problemi.

- Versione Android, produttore del terminale, nome del terminale, modello del terminale, versione dell'applicazione
- Nome della cartella, nome del file, dimensione del file, ultima modifica del file
- Nome host del server SMB, indirizzo IP, numero di porta, nome dell'account
- Nome dell'interfaccia di rete, indirizzo IP
- Impostazioni di sistema
- Impostazioni dell'applicazione

### 1.3. Impostazioni esportate e lista dei compiti di sincronizzazione

L'app può esportare "1.1.Elenco dei compiti di sincronizzazione" in un file. È possibile proteggere il file con una password prima di esportarlo.

- Nome della cartella, nome del file
- Nome host del server SMB, indirizzo IP, numero di porta, nome dell'account, password
- Impostazioni dell'applicazione

### 1.4. Inviare i dati registrati dall'app

I dati registrati dall'app possono essere inviati seguendo questi passi:

- Premere il "Pulsante Condividi" dalla scheda Cronologia
- Premere il pulsante "Invia allo sviluppatore" da "Informazioni sul sistema".
- Premere il pulsante "Condividi" o "Invia allo sviluppatore" da "Gestisci file di log".

## 2. Autorizzazioni

L'applicazione utilizza le seguenti autorizzazioni.

### 2.1. Foto/Media/File

**read the contents of your USB storage**  
**modify or delete the contents of your USB storage**

Necessario per la sincronizzazione dei file con la memoria interna/esterna/USB e per le operazioni di lettura/scrittura dei file di dati dell'applicazione.

### 2.2. Conservazione

**read the contents of your USB storage**  
**modify or delete the contents of your USB storage**

Necessario per la sincronizzazione dei file con la memoria interna/esterna/USB e per le operazioni di lettura/scrittura dei file di dati dell'applicazione.

### 2.3. Informazioni sulla connessione Wi-Fi

**view Wi-Fi connections**

Necessario per verificare lo stato del Wi-Fi (on/off) all'inizio della sincronizzazione.

### 2.4. Altro

### 2.4.1.view network connections

Necessario per confermare che il dispositivo sia collegato alla rete all'inizio della sincronizzazione.

### 2.4.2.connect and disconnect from Wi-Fi

Necessario per attivare/disattivare il Wi-Fi prima e dopo una sincronizzazione programmata.

### 2.4.3.full network access

Necessario per eseguire la sincronizzazione della rete utilizzando il protocollo SMB.

### 2.4.4.run at startup

Necessario per eseguire la sincronizzazione programmata.

### 2.4.5.control vibration

Necessaria la notifica all'utente tramite vibrazione al termine della sincronizzazione.

### 2.4.6.prevent device from sleeping

Necessario per avviare la sincronizzazione da un'applicazione programmata o esterna.

### 2.4.7.install shortcuts

Necessario per aggiungere una scorciatoia di sincronizzazione nella schermata iniziale.
