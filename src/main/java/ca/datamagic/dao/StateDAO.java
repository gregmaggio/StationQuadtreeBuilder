/**
 * 
 */
package ca.datamagic.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import ca.datamagic.dto.StateDTO;

/**
 * @author Greg
 *
 */
public class StateDAO extends BaseDAO {
	private String fileName = null;
	private Hashtable<String, StateDTO> states = new Hashtable<String, StateDTO>();

	public StateDAO() throws IOException {
		this.fileName = MessageFormat.format("{0}/states.csv", getDataPath());
		InputStream inputStream = null;	
		try {
			inputStream = new FileInputStream(this.fileName);
			CsvFormat format = new CsvFormat();
			format.setDelimiter(',');
			format.setLineSeparator("\n");
			format.setQuote('\"');
			CsvParserSettings settings = new CsvParserSettings();
			settings.setFormat(format);
			CsvParser csvParser = new CsvParser(settings);
			List<String[]> lines = csvParser.parseAll(inputStream);
			for (int ii = 1; ii < lines.size(); ii++) {
				String[] currentLineItems = lines.get(ii);
				String name = currentLineItems[0];
				String abbreviation = currentLineItems[1];
				String code = currentLineItems[2];
				if ((name != null) && (name.length() > 0) && (code != null) && (code.length() > 0)) {
					code = code.toUpperCase();
					if (!this.states.containsKey(code)) {
						this.states.put(code, new StateDTO(name, abbreviation, code));
					}
				}
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}
	
	public StateDTO getState(String code) {
		if ((code != null) && (code.length() > 0)) {
			code = code.toUpperCase();
			if (this.states.containsKey(code)) {
				return this.states.get(code);
			}
		}
		return null;
	}
}
