package com.mchehab.services.pdfService.PdfService.chrome;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static java.nio.file.Files.write;
import static java.util.Arrays.asList;

public class ChromeContext {

    private  final List<String> HEADLESS_CHROME_ARGUMENTS = asList("--headless", "--no-sandbox");
	private  final int CHROME_STARTUP_TIMEOUT = 0;
	private  static SessionFactory globalSessionFactory;
	private  int globalPort;

	public  void generatePDF(Path path, String url, String customChromePath) throws IOException {
		executeInHeadlessChrome(customChromePath, (session) -> {
			try {
				session.navigate(url);
				session.waitDocumentReady();
				byte[] preRender = session
						.getCommand()
						.getPage()
						.printToPDF();

				write(path, preRender);
			} catch (Exception e) {
				throw new RuntimeException("Could not execute pdf export in chrome session",e);
			}
		});
	}

	private  void executeInHeadlessChrome(String customChromePath, Consumer<Session> consumer) {
		SessionFactory factory = createOrGetSessionFactory(customChromePath);
		try {
			Thread.sleep(CHROME_STARTUP_TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String context = factory.createBrowserContext();
		try (Session session = factory.create(context)) {
			consumer.accept(session);
		}
	}

	private  SessionFactory createOrGetSessionFactory(String customChromePath) {
		synchronized (ChromeContext.class) {
			if (globalSessionFactory == null) {
				Launcher launcher = getLauncher(customChromePath);
				globalSessionFactory = launcher.launch(HEADLESS_CHROME_ARGUMENTS);
			}
		}

		return globalSessionFactory;
	}

	private  Launcher getLauncher(String customChromePath) {
		globalPort = findAvailablePort();

		CustomLauncher result = new CustomLauncher(globalPort);
		if (customChromePath != null) result.setCustomChromePaths(asList(customChromePath));

		return result;
	}

	private  int findAvailablePort() {
		try {
			ServerSocket serverSocket = new ServerSocket(0);
			int result = serverSocket.getLocalPort();
			serverSocket.close();

			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}