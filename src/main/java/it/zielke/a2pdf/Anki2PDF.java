package it.zielke.a2pdf;

/*
 * Copyright (c) 2013, Bjoern Zielke
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

import it.zielke.a2pdf.data.Card;
import it.zielke.a2pdf.data.Deck;
import it.zielke.a2pdf.data.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

/**
 * A2PDF can convert Anki SRS cards to PDF.
 * 
 */
public class Anki2PDF {

	private Settings settings;
	private Deck deck;
	public Logger logger;

	public Anki2PDF(Settings settings) {
		this.settings = settings;
		this.deck = new Deck();
		if (settings.getDebugging()) {
			System.setProperty(
					org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
		}
		if (settings.getVerbose()) {
			System.setProperty(
					org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
		}
		logger = LoggerFactory.getLogger(Anki2PDF.class);

	}

	/**
	 * Get a filename of the actual output file based on the current side. If
	 * the user specified filename is "C:/anki.pdf", then the resulting output
	 * file will either be "C:/anki_0.pdf" or "C:/anki_1.pdf" based on the
	 * current side.
	 * 
	 * @param side
	 *            current side, 0: front side, 1: back side.
	 * @return resulting file name
	 */
	private File getCurrentOutputFile(int side) {
		return new File(String.format("%s_%s.pdf", FilenameUtils
				.removeExtension(this.settings.getOutputFile()
						.getAbsolutePath()), side));
	}

	public void run() {
		this.deck = new DeckFileParser(this.settings.getDeckFile())
				.processDeck();

		// generate different PDFs for each side. 0: front side, 1: back side
		HTMLFormatter htmlFormatter = new HTMLFormatter(
				settings.getTemplateFront(), settings.getTemplateBack());
		for (int i = 0; i <= 1; i++) {
			List<String> currentSideContent = htmlFormatter
					.format(this.deck, i);
			if (i == Card.SIDE_BACK) {
				currentSideContent = reorderPages(currentSideContent);
			}
			generatePDF(currentSideContent, getCurrentOutputFile(i));
		}

	}

	private List<String> reorderPages(List<String> sideContent) {
		LinkedList<String> ret = new LinkedList<String>();
		int rowsize = settings.getRowSize();
		for (int i = 0; i < sideContent.size(); i += rowsize) {
			List<String> tmp = sideContent.subList(i,
					Math.min(i + rowsize, sideContent.size()));
			Collections.reverse(tmp);
			ret.addAll(tmp);
		}
		return ret;
	}

	/**
	 * Entry point.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Settings settings = new Settings();
		CmdLineParser parser = new CmdLineParser(settings);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err
					.println("java -jar myprogram.jar [options...] arguments...");
			parser.printUsage(System.err);
			System.exit(1);
		}

		Anki2PDF anki2PDF = new Anki2PDF(settings);
		// set a valid base URI for images to display correctly
		anki2PDF.settings.setBaseURI(anki2PDF.settings.getDeckFile().toURI()
				.toASCIIString());
		anki2PDF.logger.debug("Base URI: " + anki2PDF.settings.getBaseURI());

		anki2PDF.run();

	}

	public void generatePDF(List<String> content, File outputFile) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(outputFile);
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(content.get(0),
					settings.getBaseURI());
			renderer.layout();
			renderer.createPDF(os, false);

			for (String input : content) {
				if (content.get(0).equals(input))
					continue;
				renderer.setDocumentFromString(input, settings.getBaseURI());
				renderer.layout();
				renderer.writeNextDocument();
			}

			// complete the PDF
			renderer.finishPDF();

			logger.debug("PDF File with " + content.size()
					+ " documents rendered as PDF to " + outputFile);
		} catch (DocumentException e) {
			logger.error(e.getMessage());
			logger.debug("", e);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			logger.debug("", e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.debug(e.getMessage(), e);
				}
			}
		}
	}
}
