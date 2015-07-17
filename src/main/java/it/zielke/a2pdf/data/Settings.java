package it.zielke.a2pdf.data;

import java.io.File;

import org.kohsuke.args4j.Option;

/**
 * Runtime settings
 * 
 */
public class Settings {
	
	@Option(name = "-i", required = true, usage = "input file")
	private File deckFile;
	@Option(name = "-o", usage = "output name pattern")
	private File outputFile;

	@Option(name = "-tf", usage = "front template")
	private File templateFront = new File("template_front.txt");

	@Option(name = "-tb", usage = "back template")
	private File templateBack = new File("template_back.txt");

	private String baseURI;

	@Option(name = "-d", usage = "debug logging")
	private Boolean debugging = false;

	@Option(name = "-v", usage = "increase verbosity")
	private Boolean verbose = false;

	@Option(name = "-r", usage = "cards per row")
	private int rowSize = 3;

	public File getDeckFile() {
		return deckFile;
	}

	public File getOutputFile() {
		if (outputFile == null || outputFile.getName().trim().isEmpty()) {
			return new File(deckFile.getParent(), "output");
		} else {
			return outputFile;
		}
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public Boolean getDebugging() {
		return debugging;
	}

	public Boolean getVerbose() {
		return verbose;
	}

	public File getTemplateFront() {
		return templateFront;
	}

	public File getTemplateBack() {
		return templateBack;
	}

	public int getRowSize() {
		return rowSize;
	}

}
