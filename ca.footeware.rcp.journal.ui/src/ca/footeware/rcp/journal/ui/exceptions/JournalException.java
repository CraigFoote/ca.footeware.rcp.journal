package ca.footeware.rcp.journal.ui.exceptions;

public class JournalException extends Exception {

	private static final long serialVersionUID = 1L;

	public JournalException(String string) {
		super(string);
	}

	public JournalException(String string, Throwable cause) {
		super(string, cause);
	}

}
