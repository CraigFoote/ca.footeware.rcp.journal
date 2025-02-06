package ca.footeware.rcp.journal.ui.parts;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.datechooser.DateChooser;
import org.eclipse.nebula.widgets.datechooser.DateChooserTheme;
import org.eclipse.nebula.widgets.passwordrevealer.PasswordRevealer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import ca.footeware.rcp.journal.ui.exceptions.JournalException;
import ca.footeware.rcp.journal.ui.model.Journal;
import jakarta.annotation.PostConstruct;

public class JournalPart {

	private static final String ERROR = "Error";
	private Text contentText;
	private DateChooser dateChooser;
	private Journal journal;
	private Label messageLabel;
	private String password;
	private Shell shell;
	private DateChooserTheme theme;
	private Date currentDate;

	@PostConstruct
	public void createComposite(Composite parent) {
		shell = parent.getShell();
		shell.setSize(900, 600);
		parent.setLayout(new GridLayout(2, false));

		// tab folder
		TabFolder tabFolder = new TabFolder(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(tabFolder);
		// tabs
		createOpenTab(tabFolder);
		createNewTab(tabFolder);

		// calendar and buttons container
		Composite dateChooserComposite = new Composite(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(dateChooserComposite);
		GridLayoutFactory.swtDefaults().numColumns(4).equalWidth(false).applyTo(dateChooserComposite);

		// DateChooser
		dateChooser = new DateChooser(dateChooserComposite, SWT.SIMPLE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).span(4, 1).applyTo(dateChooser);
		dateChooser.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		dateChooser.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		dateChooser.setAutoSelectOnFooter(true);
		dateChooser.setFooterVisible(true);
		createDateChooserTheme();
		dateChooser.setTheme(theme);
		dateChooser.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (journal != null) {
					currentDate = dateChooser.getSelectedDate();
					// find entry for date, if any, and display its contentText
					String decrypted = "";
					try {
						decrypted = journal.decrypt(currentDate);
					} catch (JournalException | IllegalArgumentException e1) {
						showError("An error occurred.\n" + e1.getMessage());
					}
					contentText.setText(decrypted);
				}
			}
		});

		// 'first' button
		Button firstButton = new Button(dateChooserComposite, SWT.PUSH);
		firstButton.setText("First");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(firstButton);
		firstButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (journal != null) {
					try {
						currentDate = journal.getFirstDate();
						dateChooser.setSelectedDate(currentDate);
						String decrypted = journal.decrypt(currentDate);
						contentText.setText(decrypted);
					} catch (ParseException | JournalException e1) {
						showError("An error occurred.\n" + e1.getMessage());
					}
				}
			}
		});

		// 'previous' button
		Button previousButton = new Button(dateChooserComposite, SWT.PUSH);
		previousButton.setText("Previous");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(previousButton);
		previousButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (journal != null) {
					try {
						currentDate = journal.getPreviousDate(currentDate);
						dateChooser.setSelectedDate(currentDate);
						String decrypted = journal.decrypt(currentDate);
						contentText.setText(decrypted);
					} catch (ParseException | JournalException e1) {
						showError("An error occurred.\n" + e1.getMessage());
					}
				}
			}
		});

		// 'next' button
		Button nextButton = new Button(dateChooserComposite, SWT.PUSH);
		nextButton.setText("Next");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(nextButton);
		nextButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (journal != null) {
					try {
						currentDate = journal.getNextDate(currentDate);
						dateChooser.setSelectedDate(currentDate);
						String decrypted = journal.decrypt(currentDate);
						contentText.setText(decrypted);
					} catch (ParseException | JournalException e1) {
						showError("An error occurred.\n" + e1.getMessage());
					}
				}
			}
		});

		// 'last' button
		Button lastButton = new Button(dateChooserComposite, SWT.PUSH);
		lastButton.setText("Last");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(lastButton);
		lastButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (journal != null) {
					try {
						currentDate = journal.getLastDate();
						dateChooser.setSelectedDate(currentDate);
						String decrypted = journal.decrypt(currentDate);
						contentText.setText(decrypted);
					} catch (ParseException | JournalException e1) {
						showError("An error occurred.\n" + e1.getMessage());
					}
				}
			}
		});

		// content text
		contentText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).applyTo(contentText);
		contentText.addModifyListener(_ -> {
			String text = shell.getText();
			if (!text.startsWith("• ")) {
				shell.setText("• " + shell.getText());
			}
		});

		// message label
		messageLabel = new Label(parent, SWT.NONE);
		messageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// save button
		Button saveButton = new Button(parent, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					saveJournal();
				} catch (IOException | JournalException e1) {
					showError("An error occurred saving the journal.\n" + e1.getMessage());
				}
			}
		});

		parent.layout();// needed for nebula DateChooser widget
	}

	private void createDateChooserTheme() {
		theme = new DateChooserTheme();
		theme.clearCustomColors();
		theme.setHeaderBack(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		theme.setDayCellBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		theme.setHeaderForg(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		theme.setWeekendForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		theme.setSelectedForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		theme.setSelectedBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
	}

	private void createJournal(String filename, String foldername) {
		String path = foldername + File.separator + filename;
		File file = new File(path);
		if (file.exists()) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox.setText("Overwrite?");
			messageBox.setMessage("A file by that name already exists.\nDo you want to overwrite it?");
			int result = messageBox.open();
			if (result == SWT.NO) {
				file = null;
			} else {
				try {
					journal = new Journal(file, password);
					messageLabel.setText("Journal created.");
					contentText.setFocus();
				} catch (IOException | JournalException e) {
					showError("An error occurred creating the journal.\n" + e.getMessage());
				}
			}
		} else {
			try {
				boolean created = file.createNewFile();
				if (created) {
					journal = new Journal(file, password);
					messageLabel.setText("Journal created.");
					contentText.setFocus();
				} else {
					showError("An unknown error occurred creating journal.\nFile#createNewFile() failed.");
				}
			} catch (IOException | JournalException e) {
				showError("An error occurred creating the journal.\n" + e.getMessage());
			}
		}
	}

	private void createNewTab(TabFolder tabFolder) {
		TabItem newTabItem = new TabItem(tabFolder, SWT.BORDER);
		newTabItem.setText("New");

		Composite newComposite = new Composite(tabFolder, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(newComposite);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.verticalSpacing = 10;
		gridLayout.horizontalSpacing = 10;
		gridLayout.marginTop = 10;
		newComposite.setLayout(gridLayout);
		newTabItem.setControl(newComposite);

		Label nameLabel = new Label(newComposite, SWT.NONE);
		nameLabel.setText("Name:");
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(nameLabel);

		Text nameText = new Text(newComposite, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false).applyTo(nameText);

		Label folderLabel = new Label(newComposite, SWT.NONE);
		folderLabel.setText("Folder");
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(folderLabel);

		Text folderText = new Text(newComposite, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(folderText);

		Button browseButton = new Button(newComposite, SWT.PUSH);
		browseButton.setText("Browse...");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(browseButton);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(shell);
				dialog.setFilterPath(System.getProperty("user.dir"));
				String path = dialog.open();
				if (path != null) {
					folderText.setText(path);
				}
			}
		});

		Label passwordLabel1 = new Label(newComposite, SWT.NONE);
		passwordLabel1.setText("Password:");
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(passwordLabel1);

		PasswordRevealer passwordRevealer1 = new PasswordRevealer(newComposite, SWT.PASSWORD | SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false)
				.applyTo(passwordRevealer1);

		Label passwordLabel2 = new Label(newComposite, SWT.NONE);
		passwordLabel2.setText("Repeat Password:");
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(passwordLabel2);

		PasswordRevealer passwordRevealer2 = new PasswordRevealer(newComposite, SWT.PASSWORD | SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false)
				.applyTo(passwordRevealer2);

		Button createButton = new Button(newComposite, SWT.PUSH);
		createButton.setText("Create");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(3, 1).applyTo(createButton);
		createButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filename = nameText.getText().trim();
				String foldername = folderText.getText().trim();
				String password1 = passwordRevealer1.getText(); // do not trim
				String password2 = passwordRevealer2.getText(); // do not trim
				if (!filename.isEmpty() && !foldername.isEmpty() && !password1.isEmpty() && !password2.isEmpty()) {
					if (password1.equals(password2)) {
						password = password1; // or 2, whatever, I don't care anymore
						createJournal(filename, foldername);
						shell.setText(filename);
						messageLabel.setText("Journal created.");
					} else {
						showError("Passwords do not match.");
					}
				}
			}
		});
	}

	private void createOpenTab(TabFolder tabFolder) {
		TabItem openTabItem = new TabItem(tabFolder, SWT.BORDER);
		openTabItem.setText("Open");

		Composite openComposite = new Composite(tabFolder, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(openComposite);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.verticalSpacing = 10;
		gridLayout.horizontalSpacing = 10;
		gridLayout.marginTop = 10;
		openComposite.setLayout(gridLayout);
		openTabItem.setControl(openComposite);

		Label fileLabel = new Label(openComposite, SWT.NONE);
		fileLabel.setText("File:");
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(fileLabel);

		Text fileText = new Text(openComposite, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(fileText);

		Button browseButton = new Button(openComposite, SWT.PUSH);
		browseButton.setText("Browse...");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(browseButton);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				String path = dialog.open();
				if (path != null) {
					fileText.setText(path);
				}
			}
		});

		Label passwordLabel = new Label(openComposite, SWT.NONE);
		passwordLabel.setText("Password:");
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(passwordLabel);

		PasswordRevealer passwordRevealer = new PasswordRevealer(openComposite, SWT.PASSWORD | SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).grab(true, false)
				.applyTo(passwordRevealer);

		Button openButton = new Button(openComposite, SWT.PUSH);
		openButton.setText("Open");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(3, 1).applyTo(openButton);
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filepath = fileText.getText().trim();
				password = passwordRevealer.getText(); // do not trim
				if (!filepath.isEmpty() && !password.isEmpty()) {
					File file = new File(filepath);
					if (!file.exists() || !file.canRead() || !file.canWrite()) {
						showError("Unable to open " + filepath);
					} else {
						try {
							loadJournalFromFile(file);
							shell.setText(file.getName());
							messageLabel.setText("Journal opened.");
						} catch (JournalException | IOException | ParseException e1) {
							showError("Unable to open journal at " + filepath + ",\n" + e1.getMessage());
						}
					}
				}
			}
		});
	}

	protected void loadJournalFromFile(File file) throws JournalException, IOException, ParseException {
		journal = new Journal(file, password);
		Date today = dateChooser.getTodayDate();

		// get decrypted content for date if value present
		String decrypted = journal.decrypt(today);
		contentText.setText(decrypted);

		// highlight all dates that have an entry
		List<Date> dates = journal.getDates();
		for (Date date : dates) {
			theme.setCustomColor(date, Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
			dateChooser.setFocusOnToday(true);
		}
	}

	protected void saveJournal() throws IOException, JournalException {
		if (journal == null) {
			return;
		}
		String plainText = contentText.getText();
		Date date = dateChooser.getSelectedDate() == null ? dateChooser.getTodayDate() : dateChooser.getSelectedDate();
		journal.addEntry(date, plainText);
		theme.setCustomColor(date, Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
		String title = shell.getText();
		if (title.startsWith("• ")) {
			shell.setText(title.substring(2));
		}
		messageLabel.setText("Journal saved.");
	}

	protected void showError(String string) {
		messageLabel.setText("");
		MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
		messageBox.setText(ERROR);
		messageBox.setMessage(string);
		messageBox.open();
	}
}