package ca.footeware.rcp.journal.ui.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ca.footeware.rcp.journal.ui.exceptions.JournalException;

public class Journal {

	public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private File file;
	private String password;
	private Properties properties;

	public Journal(File file, String password) throws IOException, JournalException {
		if (file == null || !file.exists() || !file.canRead() || !file.canRead()) {
			throw new IllegalArgumentException("Invalid file.");
		}
		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("Invalid password.");
		}
		this.file = file;
		this.password = password;
		this.properties = new Properties();
		try (final var in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			this.properties.load(in);
		} catch (IllegalArgumentException e) {
			throw new JournalException("Unable to create journal. \n" + e.getMessage(), e);
		}
	}

	public void addEntry(Date date, String plainText) throws JournalException {
		String dateStr = dateFormat.format(date);
		try {
			String encrypted = Superstar.encrypt(plainText, password);
			properties.setProperty(dateStr, encrypted);
			save();
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | IOException e) {
			throw new JournalException("An error occurred adding the journal entry.", e);
		}
	}

	public String decrypt(Date date) throws JournalException {
		String dateStr = dateFormat.format(date);
		Object object = properties.get(dateStr);
		String decrypted = "";
		if (object instanceof String encrypted) {
			try {
				decrypted = Superstar.decrypt(encrypted, password);
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | IllegalArgumentException e) {
				throw new JournalException("An error occurred decrypting the journal.\n" + e.getMessage(), e);
			}
		}
		return decrypted;
	}

	public List<Date> getDates() throws ParseException {
		List<Date> dates = new ArrayList<>();
		Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			Object element = keys.nextElement();
			if (element instanceof String dateStr) {
				Date date = dateFormat.parse(dateStr);
				dates.add(date);
			}
		}
		return dates;
	}

	public boolean isEmpty() {
		return properties.isEmpty();
	}

	private void save() throws IOException {
		try (final var out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			properties.store(out, null);
		}
	}
}
