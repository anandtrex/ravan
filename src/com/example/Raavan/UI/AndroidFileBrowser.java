package com.example.Raavan.UI;

import java.io.File;

import java.util.ArrayList;

import com.example.Raavan.R;

import android.app.AlertDialog;

import android.app.ListActivity;

import android.content.DialogInterface;

import android.content.Intent;

import android.content.DialogInterface.OnClickListener;

import android.net.Uri;

import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.widget.ArrayAdapter;

import android.widget.ListView;

public class AndroidFileBrowser extends ListActivity

{

	private enum DISPLAYMODE {
		ABSOLUTE, RELATIVE
	};

	private final DISPLAYMODE mDisplayMode = DISPLAYMODE.ABSOLUTE;

	private ArrayList<String> mDirectoryEntries = new ArrayList<String>();

	private File mCurrentDirectory = new File("/");

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState)

	{

		super.onCreate(savedInstanceState);

		// setContentView() gets called within the next line,

		// so we do not need it here.

		browseToRoot();

	}

	/**
	 * 
	 * This method browses to the root-directory of the file-system.
	 */

	private void browseToRoot()

	{

		browseTo(new File("/"));

	}

	/**
	 * 
	 * Method browses to the given directory and either lists its files or asks
	 * the
	 * 
	 * user to open the file if it is not a directory but a file instead.
	 * 
	 * 
	 * 
	 * @param dir
	 *            the file or directory to browse to
	 */

	private void browseTo(final File dir)

	{

		if (dir.isDirectory())

		{

			mCurrentDirectory = dir;

			fill(mCurrentDirectory.listFiles());

		}

		else

		{

			OnClickListener okButtonListener = new OnClickListener()

			{

				@Override
				public void onClick(DialogInterface arg0, int arg1)

				{

					// Starts an intent to view the file that was clicked...

					openFile(dir);

				}

			};

			OnClickListener cancelButtonListener = new OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1)

				{

					// Do nothing

				}

			};

			new AlertDialog.Builder(this)

			.setTitle("Question")

			.setMessage("Do you want to open that file?\n" + dir.getName())

			.setPositiveButton("OK", okButtonListener)

			.setNegativeButton("Cancel", cancelButtonListener)

			.show();

		}

	}

	/**
	 * 
	 * Fills the list view with the file path (relative or absolut).
	 * 
	 * 
	 * 
	 * @param files
	 *            the files to be shown
	 */

	private void fill(File[] files)

	{

		this.mDirectoryEntries.clear();

		// Add the "." == "current directory"

		// and the ".." == 'Up one level'

		this.mDirectoryEntries.add(getString(R.string.current_dir));

		if (this.mCurrentDirectory.getParent() != null)

			this.mDirectoryEntries.add(getString(R.string.up_one_level));

		switch (this.mDisplayMode)

		{

		case ABSOLUTE:

			for (File file : files)

			{

				this.mDirectoryEntries.add(file.getPath());

			}

			break;

		case RELATIVE: // On relative Mode, we have to substract the
			// current-path from the beginning

			int currentPathStringLenght = this.mCurrentDirectory
			.getAbsolutePath().length();

			for (File file : files)

			{

				this.mDirectoryEntries.add(file.getAbsolutePath().substring(
						currentPathStringLenght));

			}

			break;

		}

		// create an array list adapter and set it as the view

		ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,

				R.layout.raavanui, this.mDirectoryEntries);

		this.setListAdapter(directoryList);

	}

	/**
	 * 
	 * Sends an intent to open the given file.
	 * 
	 * 
	 * 
	 * @param f
	 *            the file to be opened
	 */

	private void openFile(File f)

	{

		// Create an Intent

		Intent intent = new Intent();

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.setAction(android.content.Intent.ACTION_VIEW);

		// Category where the App should be searched

		// String category = new String("android.intent.category.DEFAULT");

		// Setting up the data and the type for the intent

		String type = getMIMEType(f);

		intent.setDataAndType(Uri.fromFile(f), type);

		// will start the activtiy found by android or show a dialog to select
		// one

		startActivity(intent);

	}

	/**
	 * 
	 * Returns the MIME type for the given file.
	 * 
	 * 
	 * 
	 * @param f
	 *            the file for which you want to determine the MIME type
	 * 
	 * @return the detected MIME type
	 */

	private String getMIMEType(File f)

	{

		String end = f.getName().substring(f.getName().lastIndexOf(".") + 1,
				f.getName().length()).toLowerCase();

		String type = "";

		if (end.equals("mp3") || end.equals("aac") || end.equals("aac")
				|| end.equals("amr") || end.equals("mpeg") || end.equals("mp4"))
			type = "audio";

		else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg"))
			type = "image";

		type += "/*";

		return type;

	}

	/**
	 * 
	 * This function browses up one level according to the field:
	 * mCurrentDirectory.
	 */

	private void upOneLevel()

	{

		if (this.mCurrentDirectory.getParent() != null)

			this.browseTo(this.mCurrentDirectory.getParentFile());

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		int selectionRowID = position;

		String selectedFileString = this.mDirectoryEntries.get(selectionRowID);

		if (selectedFileString.equals(getString(R.string.current_dir)))

		{

			// Refresh

			this.browseTo(this.mCurrentDirectory);

		} else if (selectedFileString.equals(getString(R.string.up_one_level)))

		{

			this.upOneLevel();

		}

		else

		{

			File clickedFile = null;

			switch (this.mDisplayMode) {

			case RELATIVE:

				clickedFile = new File(this.mCurrentDirectory.getAbsolutePath()

						+ this.mDirectoryEntries.get(selectionRowID));

				break;

			case ABSOLUTE:

				clickedFile = new File(selectedFileString);

				break;

			}

			if (clickedFile != null)

			{

				Log.d("AndroidFileBrowser", "File " + clickedFile + " exists? "
						+

						clickedFile.exists());

				this.browseTo(clickedFile);

			}

		}

	}

}
