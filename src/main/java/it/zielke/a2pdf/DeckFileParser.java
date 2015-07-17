package it.zielke.a2pdf;

import it.zielke.a2pdf.data.Deck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses Anki deck export files and returns the decks found.
 * 
 */
public class DeckFileParser {

	private File deckFile;
	private Deck deck;
	private Logger logger;

	/**
	 * @param deckFile
	 *            an Anki TXT deck file
	 */
	public DeckFileParser(File deckFile) {
		deck = new Deck();
		this.deckFile = deckFile;
		logger = LoggerFactory.getLogger(DeckFileParser.class);
	}

	/**
	 * Parses the deck from the given text file and return its object
	 * representation.
	 * 
	 * @return deck in object representation
	 * @throws IOException
	 *             if the deck text file could not be read
	 */
	public Deck processDeck() {
		boolean errors = parse(deckFile);
		return deck;

	}

	private boolean parse(File f) {
		List<String> lines;
		try {
			lines = IOUtils.readLines(new FileInputStream(deckFile), "UTF-8");
			for (String line : lines) {
				parseLine(line);
			}
			return true;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			logger.debug("", e);
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.debug("", e);
		}
		return false;

	}

	private void parseLine(String line) {
		String[] parts = line.split("\t");
		// TODO make this configurable
		String front = "";
		String back = "";
		// check for cards with bad format, ie. missing content
		if (parts.length > 0) {
			front = replaceHTML(parts[0]);
		}
		if (parts.length > 1) {
			back = replaceHTML(parts[1]);
		}
		deck.addCardSides(front, back);
	}

	private String replaceHTML(String s) {
		// Anki sometimes uses non-breakable spaces. We want text to break
		// so it does not overflow.
		return s.replace("&nbsp;", " ");
	}
}
