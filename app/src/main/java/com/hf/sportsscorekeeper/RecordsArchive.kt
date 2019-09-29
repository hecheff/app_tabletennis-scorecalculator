package com.hf.sportsscorekeeper

import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_records_archive.*
import java.time.format.DateTimeFormatter
import java.util.*
import android.content.Intent
import android.os.Build
import android.annotation.TargetApi
import android.content.DialogInterface
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible


class RecordsArchive : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records_archive)

        populateRecords()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.records_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.clearRecords -> {
                showClearRecordsPrompt()
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    // Code for keeping state as-is when changing activity
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun getParentActivityIntent(): Intent? {
        return super.getParentActivityIntent()!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    private fun populateRecords() {
        // Get existing records off database
        var myDB = openOrCreateDatabase("my.db", MODE_PRIVATE, null)
        val result: Cursor = myDB.rawQuery("SELECT DISTINCT sessionID, date(match_dateTime), time(match_dateTime), " +
                "winCount_P1, winCount_P2 FROM gameRecords GROUP BY sessionID ORDER BY sessionID DESC", null)
        result.moveToFirst()

        // If list is empty, show "Empty List" text (Empty Collection compliance
        var db_sessionID = arrayOf<Int>()
        var db_dateTime = arrayOf<String>()
        var db_finalWinCount = arrayOf<Array<Int>>()
        var tempIndex = 0 // Index for identifying unique session ID. Used only for getting end scores (for now)
        var tempDateTime = ""

        val dbCheckCount = myDB.rawQuery("SELECT count(*) FROM gameRecords", null)
        dbCheckCount.moveToFirst()
        var checkEntriesCount = dbCheckCount.getInt(0)

        // Start by clearing all existing records in recycler_view
        if(checkEntriesCount > 0) {
            while (!result.isNull(0)) {
                tempIndex = result.getInt(0)
                db_sessionID += result.getInt(0)

                tempDateTime = result.getString(1) + " " + result.getString(2)
                db_dateTime += tempDateTime

                db_finalWinCount += arrayOf(result.getInt(3), result.getInt(4))

                if (!result.isLast) {
                    result.moveToNext()
                } else {
                    break
                }
            }
            recycler_view.layoutManager = LinearLayoutManager(this)
            recycler_view.adapter = RecordViewAdapter(db_sessionID, db_dateTime, db_finalWinCount)

            lbl_NoRecord.isInvisible = true
        } else {
            lbl_NoRecord.isInvisible = false
        }
    }

    fun showClearRecordsPrompt() {
        val clearRecordDialog = AlertDialog.Builder(this)

        clearRecordDialog.setMessage("Clear all records?")
            .setTitle("Confirm Clear Record")
            .setPositiveButton("Clear Data") { dialog, id ->
                clearRecords()
                finish()

                val intent = Intent(this, MainActivity::class.java).apply { }
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, id ->
                // Do nothing
            }

        val dialog = clearRecordDialog.create()
        dialog.show()
    }

    private fun clearRecords() {
        // Set database basics
        var myDB = openOrCreateDatabase("my.db", MODE_PRIVATE, null)

        // Drop table
        myDB.execSQL("DROP TABLE IF EXISTS gameRecords")


        // Recreate table
        myDB.execSQL(
            "CREATE TABLE IF NOT EXISTS gameRecords(" +
                    "sessionID INT, " +
                    "match_dateTime DATETIME, " +
                    "matchNumber INT, "  +
                    "matchFinalScore_P1 INT, " +
                    "matchFinalScore_P2 INT, " +
                    "winCount_P1 INT, " +
                    "winCount_P2 INT" +
                    ")")


    }
}
