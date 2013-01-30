package net.lukegjpotter.app.footballcrestquiz;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * @author Luke Potter
 * 01/Dec/2012
 * 
 * This is the main activity for the Football Crest Quiz app.
 * 
 */
public class MainActivity extends Activity {
	
	// String used when logging error messages
	private static final String TAG = "FootballCrestQuiz MainActivity";
	
	// Instance Variables
	private List<String> fileNameList, quizClubsList; // Crest file names and a list of the clubs in the quiz
	private Map<String, Boolean> leaguesMap; // Which leagues are enabled
	private String correctAnswer; // Correct club for the current crest
	private int totalGuesses, correctAnswers, guessRows;
	private Random random;
	private Handler handler;
	private Animation shakeAnimation;
	
	// Views from XML file
	private TextView answerTextView, questionNumberTextView;
	private ImageView crestImageView;
	private TableLayout buttonTableLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		//initialiseVariables();
		// Initialise the variables
		fileNameList  = new ArrayList<String>();
		quizClubsList = new ArrayList<String>();
		leaguesMap    = new HashMap<String, Boolean>();
		guessRows     = 1;
		random        = new Random();
		handler       = new Handler();

		// Load the shake animation that's used for incorrect answers
		shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.incorrect_shake);
		shakeAnimation.setRepeatCount(3); // Animation repeats three times

		// Get the array of leagues from strings.xml
		String[] leagueNames = getResources().getStringArray(R.array.leaguesList);

		// By default, clubs are chosen from all leagues
		for (String league : leagueNames)
			leaguesMap.put(league, true);

		// Get references to GUI components
		questionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
		crestImageView         = (ImageView) findViewById(R.id.crestImageView);
		buttonTableLayout      = (TableLayout) findViewById(R.id.buttonTableLayout);
		answerTextView         = (TextView) findViewById(R.id.answerTextView);

		// Set questionNumberTextView's text
		questionNumberTextView.setText(""
				+ getResources().getString(R.string.question)
				+ " 1 "
				+ getResources().getString(R.string.of)
				+ " 10");
		
		resetQuiz(); // Starts a new quiz
	}

	//private void initialiseVariables() {}

	// Setup and start the next quiz
	private void resetQuiz() {

		// Use the AssetManager to get the crest image
		// file names for only the enabled leagues
		AssetManager assets = getAssets();
		fileNameList.clear();
		
		try {
			
			// Get Set of leagues
			Set<String> leagues = leaguesMap.keySet(); 
			
			// Loop through each league
			for (String league : leagues) {
				
				// If league is enabled
				if (leaguesMap.get(league)) {
					
					// Get a list of all crest image in this league
					String[] paths = assets.list(league);
					
					for (String path : paths)
						fileNameList.add(path.replace(".png", ""));
				} // End If
			} // End For
		} catch (IOException e) {
			
			Log.e(TAG, "Error loading image file names.", e);
		}
		
		correctAnswers = 0;
		totalGuesses = 0;
		quizClubsList.clear();
		
		// Add 10 random file names to the quizCrestsList
		int crestCounter = 1;
		int numberOfCrests = fileNameList.size();
		int randomIndex = 0;
		String filename = "";
		
		Log.i(TAG, "FileNameList size/numberofCrests is " + numberOfCrests);
		
		while (crestCounter <= 10) {
			
			 randomIndex = random.nextInt(numberOfCrests);
			 
			 // Get the random file name
			 filename = fileNameList.get(randomIndex);
			 
			 // If the league is enabled and it hasn't already been chosen
			 if (!quizClubsList.contains(filename)) {
				 
				 quizClubsList.add(filename);
				 ++crestCounter;
			 } // End if
		} // End while
		
		// Start the quiz by loading the first crest
		loadNextCrest();
	} // End resetQuiz method
	
	// After the user guesses a correct correct crest, load the next crest
	private void loadNextCrest() {

		// Get the filename of the next crest and remove it from the list
		String nextImageName = quizClubsList.remove(0);
		correctAnswer = nextImageName; // Update the correct answer
		
		answerTextView.setText(""); // Clear answerTextView
		
		// Display the number of the current question in the quiz
		questionNumberTextView.setText(""
				+ getResources().getString(R.string.question)
				+ " "
				+ (correctAnswers + 1)
				+ " "
				+ getResources().getString(R.string.of)
				+ " 10");
		
		// Extract the league from the next image's name
		String league = nextImageName.substring(0, nextImageName.indexOf("_"));
		
		// Use AssetManager to load next image from assets folder
		AssetManager assets = getAssets();
		InputStream stream; // Used to read in the crest's images
		
		try {
			
			// Get an InputStream to the asset representing the next crest
			stream = assets.open(league + "/" + nextImageName + ".png");
			
			// Load the asset as a Drawable and display on the crestImageView
			Drawable crest = Drawable.createFromStream(stream, nextImageName);
			crestImageView.setImageDrawable(crest);
			
		} catch (IOException e) {
			
			Log.e(TAG, "Error loading " + nextImageName, e);
		}
		
		// Clear prior answer Buttons from TableRows
		for (int row = 0; row < buttonTableLayout.getChildCount(); ++row)
			((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();
		
		Collections.shuffle(fileNameList); // Shuffle file names
		
		// Put the correct answer at the end of the fileNameList
		int correct = fileNameList.indexOf(correctAnswer);
		fileNameList.add(fileNameList.remove(correct));
		
		// Get a reference to the LayoutInflator service
		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// Add 3, 6 or 9 answer Buttons based on the values of guessRows
		for (int row = 0; row < guessRows; row++) {
			
			TableRow currentTableRow = getTableRow(row);
			
			// Place buttons in currentTableRow
			for (int column = 0; column < 3; column++) {
				
				// Inflate guess_button.xml to create new Button
				Button newGuessButton = (Button) inflator.inflate(R.layout.guess_button, null);
				
				// Get club name and set it as newGuessButton's text
				String fileName = fileNameList.get((row * 3) + column);
				newGuessButton.setText(getClubName(fileName));
				
				// Register answerButtonListener to respond to button clicks
				newGuessButton.setOnClickListener(guessButtonListener);
				currentTableRow.addView(newGuessButton);
			} // End for
		} // End for
		
		// Randomly replace one Button with the correct answer
		int row = random.nextInt(guessRows); // Pick Random Row
		int column = random.nextInt(3); // Pick Random Column
		TableRow randomTableRow = getTableRow(row); // Get the TableRow
		String clubName = getClubName(correctAnswer);
		((Button) randomTableRow.getChildAt(column)).setText(clubName);
	} // End method loadNextCrest
	
	// Called when the user selects an answer
	private void submitGuess(Button guessButton) {
		
		String guess = guessButton.getText().toString();
		String answer = getClubName(correctAnswer);
		++totalGuesses;
		
		// If the guess is correct
		if (guess.equals(answer)) {
			
			++correctAnswers;
			
			// Display "Correct!!!" in green text
			answerTextView.setText(answer + "!!!");
			answerTextView.setTextColor(getResources().getColor(R.color.correct_answer));
			
			disableButtons();
			
			// If the user has correctly identified ten crests
			if(correctAnswers == 10) {
				
				// Create a new AlertDialog Builder
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.reset_quiz);
				
				// Set the AlertDialog's messages to display game results
				builder.setMessage(
					String.format(
						"%d %s, %.02f%% %s",
						totalGuesses,
						getResources().getString(R.string.guesses),
						(1000/(double) totalGuesses),
						getResources().getString(R.string.correct)
					)
				);
				
				builder.setCancelable(false);
				
				// Add "Reset Quiz" Button
				builder.setPositiveButton(R.string.reset_quiz,
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							resetQuiz();
						}
					}
				);
				
				// Create AlertDialog from the Builder
				AlertDialog resetDialog = builder.create();
				resetDialog.show();
			}
			else {
				
				// Load the next crest after a 1-second delay
				handler.postDelayed(
					new Runnable() {
						
						@Override
						public void run() {
							loadNextCrest();
						}
					}, 1000);
			}
		}
		else {
			
			// Play the animation
			crestImageView.startAnimation(shakeAnimation);
			
			// Display "Incorrect!!!" in red
			answerTextView.setText("Incorrect!!!");
			answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
			guessButton.setEnabled(false);
		}
	} // End method submitGuess
	
	// Utility method that disables all answer Buttons
	private void disableButtons() {
		
		for(int row = 0; row < buttonTableLayout.getChildCount(); ++row) {
			
			TableRow tableRow = (TableRow) buttonTableLayout.getChildAt(row);
			
			for(int i = 0; i < tableRow.getChildCount(); ++i)
				tableRow.getChildAt(i).setEnabled(false);
		}
	} // End method diableButtons
	
	// Returns the specified TableRow 
	private TableRow getTableRow(int row) {

		return (TableRow) buttonTableLayout.getChildAt(row);
	}
	
	// Parses the club crest file name and returns the club name
	private String getClubName(String name) {

		return name.substring(name.indexOf('_') + 1).replace('_', ' ');
	}

	// Called when a guess button is touched
	private OnClickListener guessButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			submitGuess((Button) v);
		}
	};
	
	//---------------- Options menu methods --------------------------
	// Create constants for each menu item
	private final int CHOICES_MENU_ID = Menu.FIRST;
	private final int LEAGUES_MENU_ID = Menu.FIRST + 1;
	
	// Called when the user accesses the options menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		
		// Add two options to the menu - "Choices" and "Leagues"
		menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE, R.string.choices);
		menu.add(Menu.NONE, LEAGUES_MENU_ID, Menu.NONE, R.string.leagues);
		
		return true;
	} // End method onCreateOptionsMenu

	// Called when the user selects an option from the menu
	public boolean onOptionsItemSelect(MenuItem item) {
		
		// Switch the menu id of the user-selected options
		switch(item.getItemId()) {
			case CHOICES_MENU_ID:
				
				// Creates a list of the possible numbers of the answer choices
				final String[] possibleChoices = getResources().getStringArray(R.array.guessesList);
				
				// Creates a new AlertDialog Builder and set it's title
				AlertDialog.Builder choicesBuilder = new AlertDialog.Builder(this);
				choicesBuilder.setTitle(R.string.choices);
				
				// Add possibleChoices items to the Dialog and set the behavior when one of the items is clicked
				choicesBuilder.setItems(
					R.array.guessesList,
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							// Update guessRows to match the user's choice
							guessRows = Integer.parseInt(possibleChoices[which].toString()) / 3;
							resetQuiz();
						}
					}
				);
				
				// Create an AlertDialog from the Builder
				AlertDialog choicesDialog = choicesBuilder.create();
				choicesDialog.show();
				
				return true;
			
			case LEAGUES_MENU_ID:
				
				// Get array of world leagues
				final String[] leagueNames = leaguesMap.keySet().toArray(new String[leaguesMap.size()]);
				
				// Boolean array representing weather each league is enabled
				boolean[] leaguesEnabled = new boolean[leaguesMap.size()];
				
				for (int i = 0; i < leaguesEnabled.length; ++i)
					leaguesEnabled[i] = leaguesMap.get(leagueNames[i]);
				
				// Create an Alert Dialog Builder and set the dialog's title
				AlertDialog.Builder leaguesBuilder = new AlertDialog.Builder(this);
				leaguesBuilder.setTitle(R.string.leagues);
				
				// Replace '_' with Space in League names for display purposes
				String[] displayNames = new String[leagueNames.length];
				for (int i = 0; i < leagueNames.length; ++i)
					displayNames[i] = leagueNames[i].replace('_', ' ');
				
				// Add diaplayNames to the Dialog and set the behavior when one of the items is clicked
				leaguesBuilder.setMultiChoiceItems(
					displayNames,
					leaguesEnabled,
					new DialogInterface.OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {

							// Include or exclude the checked leagues depending on weather or not it's checked
							leaguesMap.put(leagueNames[which].toString(), isChecked);
						}
					}
				);
				
				// Resets quiz when user presses the "Reset Quiz" Button
				leaguesBuilder.setPositiveButton(
					R.string.reset_quiz,
					new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {

							resetQuiz();
						}
					}
				);
				
				// Create a dialog from the Builder
				AlertDialog leaguesDialog = leaguesBuilder.create();
				leaguesDialog.show();
				
				return true;
			} // End switch
		
		return super.onOptionsItemSelected(item);
	}
}
