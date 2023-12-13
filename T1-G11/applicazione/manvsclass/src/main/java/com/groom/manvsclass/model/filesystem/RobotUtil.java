package com.groom.manvsclass.model.filesystem;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.web.multipart.MultipartFile;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class RobotUtil {

	/**
	 * @param path Il percorso del file XML contenente le informazioni di copertura.
	 * @return La percentuale di copertura delle linee.
	 */
	public static int LineCoverage(String path) {
		Element line = null;
		String linecoverage = null;
		try {
			// creo un nuovo file che conterrà i valori della coverage

			File cov = new File(path);

			Document doc = Jsoup.parse(cov, null, "", Parser.xmlParser());
			// Assume che l'elemento "coverage" sia il quarto elemento "coverage" nel
			// documento XML
			line = doc.getElementsByTag("coverage").get(3);
			linecoverage = String.valueOf(line).substring(32, 35);
			// Estrai la percentuale di copertura dalle stringhe ottenute
			linecoverage = linecoverage.split("%", 0)[0];

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Integer.valueOf(linecoverage);
	}

	/**
	 * Calcola la copertura delle linee da un file CSV generato da un tool
	 * specifico.
	 *
	 * @param path Il percorso del file CSV contenente le informazioni di copertura.
	 * @return La percentuale di copertura delle linee.
	 */
	public static int LineCoverageE(String path) {
		Float elemento = 0.0f;

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			// Leggi la prima riga (la riga 1 è la seconda riga nel conteggio base 1)
			String firstLine = br.readLine();

			// Leggi la seconda riga
			String secondLine = br.readLine();

			if (secondLine != null) {
				// Dividi la seconda riga in elementi separati da virgole
				String[] elements = secondLine.split(",");

				// Prendo il valore di copertura per linea
				elemento = Float.parseFloat(elements[2]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Math.round(elemento * 100);
	}

	/**
	 * Genera e salva i file di robot e ne calcola la copertura delle linee.
	 *
	 * @param fileName      Il nome del file.
	 * @param className         L'ID della classe di test.
	 * @param formData Il file caricato.
	 * @throws IOException Eccezione di IO.
	 */
	public static void generateAndSaveRobots(String fileName, String className, MultipartFile formData)
			throws IOException {
		// RANDOOP - T9
		Path directory = Paths.get("/VolumeT9/app/FolderTree/" + className + "/" + className + "SourceCode");

		try {
			// Verifica se la directory esiste già
			if (!Files.exists(directory)) {
				// Crea la directory
				Files.createDirectories(directory);
				System.out.println("La directory è stata creata con successo.");
			} else {
				System.out.println("La directory esiste già.");
			}
		} catch (Exception e) {
			System.out.println("Errore durante la creazione della directory: " + e.getMessage());
		}
		// Legge l'input stream del file caricato e lo copia nella directory specificata
		try (InputStream inputStream = formData.getInputStream()) {
			// Risolve il percorso completo del file all'interno della directory
			// specificata.
			// Viene utilizzato il metodo 'directory.resolve(fileName)' per ottenere il
			// percorso completo
			// del file all'interno della directory 'directory'. Questo percorso completo
			// sarà utilizzato
			// successivamente per copiare il contenuto dell'input stream del file nella
			// posizione desiderata.
			Path filePath = directory.resolve(fileName);
			System.out.println(filePath.toString());

			// copio il contenuto dell'input stream nel file di destinaziione
			// l'ultimo parametro di questa funzione indica che se il file già esiste deve
			// essere sostituito
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

			// chiusura dell'input stream dopo aver completato la copia
			inputStream.close();
		}

		// creazione del processo esterno
		ProcessBuilder processBuilder = new ProcessBuilder();

		// con command si configura il comando del processo esterno per eseguire il file
		// JAR 'Task9-G19-0.0.1-SNAPSHOT.jar'
		// l'esecuzione avviene attraverso la JVM di Java.
		// Il parametro "-jar" specifica l'esecuzione di un file JAR.

		processBuilder.command("java", "-jar", "Task9-G19-0.0.1-SNAPSHOT.jar");

		// La directory di lavoro per il processo esterno viene impostata su
		// "/VolumeT9/app/" utilizzando
		// questo metodo garantisce che il processo lavori nella directory desiderata
		processBuilder.directory(new File("/VolumeT9/app/"));

		// linea di debugg--potremmo anche commentarla
		System.out.println("Prova");

		// si avvia il processo
		Process process = processBuilder.start();

		// Legge l'output del processo esterno tramite un BufferedReader, che a sua
		// volta usa
		// un InputStreamReader per convertire i byte in caratteri. Il metodo
		// 'process.getInputStream()'
		// restituisce lo stream di input del processo esterno.

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		// All'interno del loop viene letta ogni linea disponibile finché il processo
		// continua a produrre output.
		while ((line = reader.readLine()) != null)
			System.out.println(line);

		// funzionamento analogo al precedente, invece di leggere l'output leggiamo gli
		// errori
		reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		while ((line = reader.readLine()) != null)
			System.out.println(line);

		try {
			// Attende che il processo termini e restituisce il codice di uscita
			int exitCode = process.waitFor();

			System.out.println("ERRORE CODE: " + exitCode);
		} catch (InterruptedException e) {
			System.out.println(e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Crea un oggetto File che rappresenta il percorso della directory contenente i
		// risultati
		// della generazione di robot da Randoop. Il percorso è costruito in base all'ID
		// della classe di test 'className'.
		File resultsDir = new File("/VolumeT9/app/FolderTree/" + className + "/RobotTest/RandoopTest");

		// Inizializza la variabile 'liv' a 0, rappresentante il massimo livello di
		// robot prodotti da Randoop.
		// Questo valore sarà aggiornato successivamente durante l'analisi dei
		// risultati.
		int liv = 0; // livelli di robot prodotti da randoop

		File results[] = resultsDir.listFiles();
		// Itera attraverso tutti i file nella directory dei risultati della generazione
		// di robot da Randoop.

		for (File result : results) {

			// Calcola la copertura delle linee per ciascun file XML di copertura estraendo
			// il valore dal file XML 'coveragetot.xml' nella directory corrispondente.
			int score = LineCoverage(result.getAbsolutePath() + "/coveragetot.xml");
			// Stampa le informazioni sulla copertura del livello
			System.out.println(
					result.toString().substring(result.toString().length() - 7, result.toString().length() - 5));

			// Estrae il livello numerico dall'ultimo tratto del nome della directory,
			// basandosi
			// sulla convenzione specifica naming. Nella convenzione attuale, il livello è
			// rappresentato da due caratteri numerici situati nelle posizioni -7 e -5
			// rispetto
			// alla fine del nome della directory

			int livello = Integer.parseInt(
					result.toString().substring(result.toString().length() - 7, result.toString().length() - 5));

			System.out.println("La copertura del livello " + String.valueOf(livello) + " è: " + String.valueOf(score));

			// Configurazione di un client HTTP
			HttpClient httpClient = HttpClientBuilder.create().build();

			// Creazione di un oggetto HttpPost con l'URL "http://t4-g18-app-1:3000/robots"
			HttpPost httpPost = new HttpPost("http://t4-g18-app-1:3000/robots");

			// Creazione di un array JSON per contenere le informazioni sui robot generati
			JSONArray arr = new JSONArray();

			// Creazione di un oggetto JSON per rappresentare un singolo robot generato
			JSONObject rob = new JSONObject();

			// l'array JSON viene utilizzato per raggruppare gli oggetti JSON che
			// rappresentano le informazioni sui robot generati.
			// L'array arr contiene una serie di oggetti rob, ognuno dei quali rappresenta
			// le caratteristiche di un robot specifico generato da Randoop.

			// Aggiunge al robot l'informazione relativa al punteggio convertito in stringa
			rob.put("scores", String.valueOf(score));

			// aggiunge al robot l'informazione relativa a quale robot è stato utilizzato,
			// in questo caso randoop
			rob.put("type", "randoop");

			// aggiunge al robot l'informazione riguardante il livello di difficoltà
			// converitto in stringa
			rob.put("difficulty", String.valueOf(livello));

			// aggiunge al roboto l'informazione relativa all'id della classe di test
			rob.put("testClassId", className);

			// Aggiunge l'oggetto robot all'array JSON
			arr.put(rob);

			// Crea un oggetto JSON principale contenente l'array di robot
			JSONObject obj = new JSONObject();

			// inserimento dell'array di robot all'interno dell'oggetto
			obj.put("robots", arr);

			// Crea un'entità JSON utilizzando il contenuto dell'oggetto JSON principale.
			StringEntity jsonEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);

			// Configura la richiesta POST con l'entità JSON creata
			httpPost.setEntity(jsonEntity);

			// esegue la richiesta ed ottiene la risposta
			HttpResponse response = httpClient.execute(httpPost);

			// Se il livello del robot generato è superiore al livello massimo attuale,
			// aggiorna il livello massimo.
			if (livello > liv)
				liv = livello;

		}

		// Il seguente codice è l'adattamento ad evosuite del codice appena visto, i
		// passaggi sono gli stessi
		// EVOSUITE - T8
		// TODO: RICHIEDE AGGIUSTAMENTI IN T8
		Path directoryE = Paths.get("/VolumeT8/FolderTreeEvo/" + className + "/" + className + "SourceCode");

		try {
			// Verifica se la directory esiste già
			if (!Files.exists(directoryE)) {
				// Crea la directory
				Files.createDirectories(directoryE);
				System.out.println("La directory è stata creata con successo.");
			} else {
				System.out.println("La directory esiste già.");
			}
		} catch (Exception e) {
			System.out.println("Errore durante la creazione della directory: " + e.getMessage());
		}

		try (InputStream inputStream = formData.getInputStream()) {
			Path filePath = directoryE.resolve(fileName);
			System.out.println(filePath.toString());
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
		}

		ProcessBuilder processBuilderE = new ProcessBuilder();

		processBuilderE.command("bash", "robot_generazione.sh", className, "\"\"",
				"/VolumeT9/app/FolderTree/" + className + "/" + className + "SourceCode", String.valueOf(liv));
		processBuilderE.directory(new File("/VolumeT8/Prototipo2.0/"));

		Process processE = processBuilderE.start();

		BufferedReader readerE = new BufferedReader(new InputStreamReader(processE.getInputStream()));
		String lineE;
		while ((lineE = readerE.readLine()) != null)
			System.out.println(lineE);

		readerE = new BufferedReader(new InputStreamReader(processE.getErrorStream()));
		while ((lineE = readerE.readLine()) != null)
			System.out.println(lineE);

		try {
			int exitCode = processE.waitFor();

			System.out.println("ERRORE CODE: " + exitCode);
		} catch (InterruptedException e) {
			System.out.println(e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File resultsDirE = new File("/VolumeT8/FolderTreeEvo/" + className + "/RobotTest/EvoSuiteTest");

		File resultsE[] = resultsDirE.listFiles();
		for (File result : resultsE) {
			int score = LineCoverageE(result.getAbsolutePath() + "/TestReport/statistics.csv");

			System.out.println(
					result.toString().substring(result.toString().length() - 7, result.toString().length() - 5));
			int livello = Integer.parseInt(
					result.toString().substring(result.toString().length() - 7, result.toString().length() - 5));

			System.out.println("La copertura del livello " + String.valueOf(livello) + " è: " + String.valueOf(score));

			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost("http://t4-g18-app-1:3000/robots");

			JSONArray arr = new JSONArray();

			JSONObject rob = new JSONObject();
			rob.put("scores", String.valueOf(score));
			rob.put("type", "evosuite");
			rob.put("difficulty", String.valueOf(livello));
			rob.put("testClassId", className);

			arr.put(rob);

			JSONObject obj = new JSONObject();
			obj.put("robots", arr);

			StringEntity jsonEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);

			httpPost.setEntity(jsonEntity);

			HttpResponse response = httpClient.execute(httpPost);

		}

	}

}
