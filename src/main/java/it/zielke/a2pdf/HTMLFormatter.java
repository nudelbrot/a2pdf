package it.zielke.a2pdf;

import it.zielke.a2pdf.data.Card;
import it.zielke.a2pdf.data.Deck;
import it.zielke.a2pdf.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formats a deck of cards by applying a template from a file.
 * 
 */
public class HTMLFormatter {

	public static final int SIDE_FRONT = 0;
	public static final int SIDE_BACK = 1;
	private File templateFront;
	private File templateBack;
	private String template;
	private Logger logger;

	/**
	 * @param templateName
	 *            file name of the template to use.
	 */
	public HTMLFormatter(File templateFront, File templateBack) {
		this.templateBack=templateBack;
		this.templateFront=templateFront;
		logger = LoggerFactory.getLogger(HTMLFormatter.class);
	}

	/**
	 * Sets the content of the template file as a string. Will look for
	 * templateName_front.txt or templateName_back.txt and return their contents
	 * based on which side of the card to format. If no template file can be
	 * found, will return an HTML formatted error.
	 * 
	 * @return
	 */
	private void setTemplate(int side) {
		File templateFile;
		if (side == SIDE_FRONT) {
			templateFile = templateFront;
		} else {
			templateFile = templateBack;
		}
		logger.debug("Looking for template file: "
				+ templateFile.getAbsolutePath());

		try {
			this.template = FileUtils.readFileToString(templateFile, "UTF-8");
		} catch (IOException e) {
			logger.warn("template not found: " + templateFile.getAbsolutePath());
			this.template = "<html><body>Error: template file not found in directry</body></html>";
		}
	}

	/**
	 * Fills the template by replacing template variables $$FRONT$$ (front side)
	 * and $$BACK$$ (back side).
	 * 
	 * @param front
	 *            front side content
	 * @param back
	 *            back side content
	 * @return template including the content
	 */
	private String fillTemplate(String front, String back) {
		return template.replace("$$FRONT$$", front).replace("$$BACK$$", back);
	}

	/**
	 * Returns a list of HTML formatted sides of all cards in a deck. Either
	 * front side or back side.
	 * 
	 * @param deck
	 *            a deck to format
	 * @param side
	 *            side to format. 0: front side, 1: back side
	 * @return HTML formatted sides
	 */
	public List<String> format(Deck deck, int side) {
		List<String> htmlContent = new Vector<String>();
		setTemplate(side);
		for (int i = 0; i < deck.getCards().size(); i++) {
			Card c = deck.getCard(i);
			htmlContent.add(fillTemplate(c.getFront(), c.getBack()));
		}
		return htmlContent;
	}
}
