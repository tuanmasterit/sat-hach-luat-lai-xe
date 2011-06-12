package com.tonnguyen.sathach;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tonnguyen.sathach.bean.UserResult;

public class QuestionDbAdapter {
    public static final String KEY_RESULT_ID = "QuestionName";
    public static final String KEY_RESULT_NUM_OF_APPEAR = "NumOfAppear";
    public static final String KEY_RESULT_CORRECT = "Correct";
    public static final String KEY_RESULT_INCORRECT = "Incorrect";

    private static final String TAG = "QuestionDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "LyThuyetLaiXe";
    private static final String DATABASE_TABLE = "UserResult";
    private static final int DATABASE_VERSION = 1;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE + " (" + KEY_RESULT_ID + " text not null primary key, "
        + KEY_RESULT_NUM_OF_APPEAR +" integer, " + 
        KEY_RESULT_CORRECT + " integer, " + 
        KEY_RESULT_INCORRECT + " integer);";

    private final Context mCtx;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public QuestionDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public QuestionDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    public long createUserResult(UserResult result) {
    	return createUserResult(result.getQuestionName(), result.getNumOfAppear(), result.getCorrect(), result.getIncorrect());
    }
    
    /**
     * Create a new user result. 
     * If the result is successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param questionName name of the question that user has tested
     * @param numOfAppear the number of this question has appear to user
     * @param correct the number that user has answered this question correctly
     * @param incorrect the number that user has answered this question incorrectly
     * @return rowId or -1 if failed
     */
    private long createUserResult(String questionName, int numOfAppear, int correct, int incorrect) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_RESULT_ID, questionName);
        initialValues.put(KEY_RESULT_NUM_OF_APPEAR, numOfAppear);
        initialValues.put(KEY_RESULT_CORRECT, correct);
        initialValues.put(KEY_RESULT_INCORRECT, incorrect);
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Return a Cursor over the list of all results in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchMostIncorrectResults() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_RESULT_ID, KEY_RESULT_NUM_OF_APPEAR,
        		KEY_RESULT_CORRECT, KEY_RESULT_INCORRECT}, 
        		//KEY_RESULT_INCORRECT + " > 0",
        		null,
        		null, null, null, KEY_RESULT_INCORRECT + " DESC, " + KEY_RESULT_NUM_OF_APPEAR + " DESC");
    }
    
    public boolean updateResult(UserResult result) {
    	return updateResult(result.getQuestionName(), result.getNumOfAppear(), result.getCorrect(), result.getIncorrect());
    }
    
    /**
     * Return a Cursor positioned at the result that matches the given questionName
     * 
     * @param questionName name of question to retrieve
     * @return Cursor positioned to matching result, if found
     * @throws SQLException if result could not be found/retrieved
     */
    public Cursor fetchResult(String questionName) throws SQLException {
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_RESULT_ID,
            		KEY_RESULT_NUM_OF_APPEAR, KEY_RESULT_CORRECT, KEY_RESULT_INCORRECT}, KEY_RESULT_ID + "='" + questionName + "'", null,
                    null, null, null, null);
        //if (mCursor != null) {
        //    mCursor.moveToFirst();
        //}
        return mCursor;
    }
    
    /**
     * Update the result using the details provided. The result to be updated is
     * specified using the questionName, where question name is unique, and it is altered to use the numOfAppear, correct and incorrect
     * values passed in
     * 
     * @param questionName name of the question that user has tested
     * @param numOfAppear the number of this question has appear to user
     * @param correct the number that user has answered this question correctly
     * @param incorrect the number that user has answered this question incorrectly
     * @return true if the result was successfully updated, false otherwise
     */
    private boolean updateResult(String questionName, int numOfAppear, int correct, int incorrect) {
        ContentValues args = new ContentValues();
        args.put(KEY_RESULT_NUM_OF_APPEAR, numOfAppear);
        args.put(KEY_RESULT_CORRECT, correct);
        args.put(KEY_RESULT_INCORRECT, incorrect);

        return mDb.update(DATABASE_TABLE, args, KEY_RESULT_ID + "='" + questionName + "'", null) > 0;
    }
    
    /**
     * Create new or update existing result, to increase numOfAppear, correct or incorrect
     * @param questionName name of the question that user has tested
     * @param correct flag to indicate whether user has answered this question correctly or incorrectly
     * @return true if the result was successfully created/updated, false otherwise
     */
    public boolean increaseResult(String questionName, boolean isCorrect) {
    	Cursor cursor = fetchResult(questionName);
    	if(cursor != null && cursor.moveToFirst()) { // result has been existed. lets update the existing result
    		int numOfAppear = cursor.getInt(1) + 1;
    		int correct = cursor.getInt(2);
    		int incorrect = cursor.getInt(3);
    		if(isCorrect) {
    			correct++;
    		} else {
    			incorrect++;
    		}
    		cursor.close();
    		return updateResult(questionName, numOfAppear, correct, incorrect);
    	} else { // create new result
    		if(cursor != null) {
    			cursor.close();
    		}
    		return createUserResult(questionName, 1, isCorrect ? 1 : 0, isCorrect ? 0 : 1) != -1;
    	}
    }
}
