package com.hf.sportsscorekeeper

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.preference.PreferenceManager

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.view.Gravity
import androidx.core.view.isInvisible
import kotlin.random.Random.Default.nextBoolean

class MainActivity : AppCompatActivity() {
    // SYSTEM USE
    val TAG = "StateChange"

    // Player variables (Index: 0 = P1, 1 = P2)
    var currScore = arrayOf(0,0)

    var sessionID = 0               // Session ID for database records. Override this with largest value + 1 at start of each session
    //var currRoundIndex = 0        // Current round

    // 2D arrays for game records
    var gameProgress_wins       = arrayOf<Array<Int>>()    // Get last for current win count of player
    var gameProgress_finalScore = arrayOf<Array<Int>>()

    // Environment Variables
    var isSwapped       = false         // Flag for if positions are swapped
    var currServerIsP1  = true          // Flag for if current server is Player 1

    /*
        Score Rules Differences:
        11:
            - Win:                  11
            - Deuce:                10-10
            - Rotate (Nor):         Per 2 points
            - Rotate (Deuce):       Per 1 point

        21:
            - Win:                  21
            - Deuce:                20-20
            - Rotate (Nor):         Per 5 points
            - Rotate (Deuce):       Per 1 point
    */
    var winScoreIs21    = true          // Flag for checking if using 11 or 21 score system

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settings= PreferenceManager.getDefaultSharedPreferences(this)
        winScoreIs21    = settings.getBoolean("scoreMode", true)

        if(winScoreIs21) {
            lbl_Rules.text = getString(R.string.scoreMode21_string)
        } else {
            lbl_Rules.text = getString(R.string.scoreMode11_string)
        }

        /*
        var decorView = window.decorView
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        setSupportActionBar(toolbar)
        */
        /*
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        */

        currServerIsP1 = nextBoolean()  // Set random start for first side to serve
        updateServer()
        setAdvantageLabel(-1)

        // Establish Database (if doesn't exist yet:
        /*
            Session ID (generate from 0 count up to max limit)
            Match LastSave Date/Time            DATETIME
            Match Number (starting with 0)      INT
            Match Final Score P1                INT
            Match Final Score P2                INT
            Win Count P1                        INT
            Win Count P2                        INT

            --------------------------
            EXAMPLE DATABASE
            --------------------------
            0   2019-06-30 17:30:15     0   21  23  0   1
            0   2019-06-30 17:38:54     1   12  21  0   2
            0   2019-06-30 17:51:30     2   21  6   1   2
            1   2019-07-04 12:11:01     0   23  21  1   0
            1   2019-07-04 12:18:33     1   8   21  1   1
            etc.


            Player 1 Name (Usage TBD)
            Player 2 Name (Usage TBD)
         */

        var myDB = openOrCreateDatabase("my.db", MODE_PRIVATE, null)
        //myDB.execSQL("DROP TABLE IF EXISTS gameRecords")
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

        val result:Cursor = myDB.rawQuery("SELECT MAX(sessionID) FROM gameRecords", null)
        result.moveToFirst()
        sessionID = result.getInt(0) + 1

        lbl_gameSession.text = "SessionID: " + sessionID.toString()

        // Show version number (SDKs < 28 can only show Version Name), include Version Code if SDK >= 28
        if(Build.VERSION.SDK_INT < 28) {
            lbl_version.text = "Version "+packageManager.getPackageInfo(packageName, 0).versionName.toString()
        } else {
            lbl_version.text = "Version "+packageManager.getPackageInfo(packageName, 0).versionName.toString()+" ("+packageManager.getPackageInfo(packageName, 0).longVersionCode.toString()+")"
        }

        Log.i(TAG, "onCreate")
    }

    /*
    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        val state_currScore_p1 = currScore_p1
        val state_currScore_p2 = currScore_p2
        val state_sessionID = sessionID

        val state_gameProgress_wins = gameProgress_wins
        val state_gameProgress_finalScore = gameProgress_finalScore

        val state_isSwapped = isSwapped
        val state_currServerIsP1 = currServerIsP1

        savedInstanceState?.putInt("currScore_P1", state_currScore_p1)
        savedInstanceState?.putInt("currScore_P2", state_currScore_p2)
        savedInstanceState?.putInt("sessionID", state_sessionID)

        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i(TAG, "onRestoreInstanceState")

        currScore_p1    = savedInstanceState.getInt("currScore_P1")
        currScore_p2    = savedInstanceState.getInt("currScore_P2")
        sessionID       = savedInstanceState.getInt("sessionID")

        updateScoreDisplay()
        updateServer()
        updateGameStatus()
    }
    */


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.pastRecords -> {
                //Toast.makeText(this, "COMING SOON", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, RecordsArchive::class.java).apply { }
                startActivity(intent)
                true
            }
            R.id.settings -> {
                //Toast.makeText(this, "COMING SOON", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SettingsActivity::class.java).apply { }
                startActivity(intent)
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }


    // Update player scores
    fun setScore_PlayerLeft_Score_PlusOne(view:View) {
        updateScore("Left", true)
        checkServerChange()
    }
    fun setScore_PlayerLeft_Score_MinusOne(view:View) {
        updateScore("Left", false)
    }
    fun setScore_PlayerRight_Score_PlusOne(view:View) {
        updateScore("Right", true)
        checkServerChange()
    }
    fun setScore_PlayerRight_Score_MinusOne(view:View) {
        updateScore("Right", false)
    }

    fun setSwap(view:View) {
        isSwapped = !isSwapped
        updateScoreDisplay()
        updateServer()
        updateWinCountDisplay()

        // Swap
        if(!isSwapped) {
            bg_left.setImageResource(R.drawable.player_left)
            bg_right.setImageResource(R.drawable.player_right)
        } else {
            bg_left.setImageResource(R.drawable.player_left_swap)
            bg_right.setImageResource(R.drawable.player_right_swap)
        }

        showToast(resources.getString(R.string.sidesSwapped_string))
    }

    // Side: "Left" or "Right"
    // isAdd: true = +1, false = -1
    private fun updateScore(side:String, isAdd:Boolean) {
        if(!isSwapped) {
            when (side) {
                "Left" -> if (isAdd) {
                    currScore[0]++
                } else {
                    if (currScore[0] != 0) {
                        currScore[0]--
                    }
                }
                "Right" -> if (isAdd) {
                    currScore[1]++
                } else {
                    if (currScore[1] != 0) {
                        currScore[1]--
                    }
                }
            }
        } else {
            when (side) {
                "Left" -> if (isAdd) {
                    currScore[1]++
                } else {
                    if (currScore[1] != 0) {
                        currScore[1]--
                    }
                }
                "Right" -> if (isAdd) {
                    currScore[0]++
                } else {
                    if (currScore[0] != 0) {
                        currScore[0]--
                    }
                }
            }
        }
        updateScoreDisplay()
        updateServer()
        updateGameStatus()
    }

    private fun updateScoreDisplay() {
        if(!isSwapped) {
            txt_PlayerLeft_CurrScore.text   = currScore[0].toString()
            txt_PlayerRight_CurrScore.text  = currScore[1].toString()

            lbl_PlayerLeft.text     = resources.getString(R.string.player1_string)
            lbl_PlayerRight.text    = resources.getString(R.string.player2_string)
        } else {
            txt_PlayerLeft_CurrScore.text   = currScore[1].toString()
            txt_PlayerRight_CurrScore.text  = currScore[0].toString()

            lbl_PlayerLeft.text     = resources.getString(R.string.player2_string)
            lbl_PlayerRight.text    = resources.getString(R.string.player1_string)
        }
    }

    private fun checkServerChange() {
        var scoreDiv = 0

        when(winScoreIs21) {
            true    -> scoreDiv = 5
            false   -> scoreDiv = 2
        }
        // Override scoreDiv if deuce confirmed
        if((currScore[0] >= getDeuceScore()) && (currScore[1] >= getDeuceScore())) {
            scoreDiv = 1
        }

        if(getTotalScore()%scoreDiv == 0) {
            currServerIsP1 = !currServerIsP1
            updateServer()

            showToast(resources.getString(R.string.serverChange_string))
        }
    }

    private fun updateServer() {
        if(currServerIsP1) {
            if(!isSwapped) {
                lbl_PlayerLeft_Server.isInvisible = false
                lbl_PlayerRight_Server.isInvisible = true
            } else {
                lbl_PlayerLeft_Server.isInvisible = true
                lbl_PlayerRight_Server.isInvisible = false
            }
        } else {
            if(!isSwapped) {
                lbl_PlayerLeft_Server.isInvisible = true
                lbl_PlayerRight_Server.isInvisible = false
            } else {
                lbl_PlayerLeft_Server.isInvisible = false
                lbl_PlayerRight_Server.isInvisible = true
            }
        }
    }

    // Get current total score between both sides
    private fun getTotalScore():Int {
        return currScore[0] + currScore[1]
    }

    private fun showToast(content:String) {
        // Suspending Toast text for now during testing

        var toast:Toast = Toast.makeText(this, content, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 55, 75)
        toast.show()
    }

    private fun getDeuceScore():Int {
        if(!winScoreIs21) {
            return 10
        }
        return 20
    }

    private fun getWinScore():Int {
        if(!winScoreIs21) {
            return 11
        }
        return 21
    }

    private fun updateGameStatus() {
        if((currScore[0] >= getDeuceScore()) && (currScore[1] >= getDeuceScore())) {
            // When Deuce has already been called once
            if(currScore[0] >= currScore[1]+2) {
                // P1 win
                updateWinCount(0)
            } else if(currScore[1] >= currScore[0]+2) {
                // P2 win
                updateWinCount(1)
            } else {
                // Check if Advantage
                if(currScore[0] == currScore[1]+1) {
                    // Player 1 Advantage
                    setAdvantageLabel(0)
                    showToast(resources.getString(R.string.advantage_player1_string))
                } else if(currScore[1] == currScore[0]+1) {
                    // Player 2 Advantage
                    setAdvantageLabel(1)
                    showToast(resources.getString(R.string.advantage_player2_string))
                } else if(currScore[0] == currScore[1]) {
                    // Deuce
                    setAdvantageLabel(-1)
                    showToast(resources.getString(R.string.deuce_string))
                }
            }
        } else {
            // Non-Deuce
            if(currScore[0] == getWinScore()) {
                // Player 1 Win
                updateWinCount(0)
            } else if(currScore[1] == getWinScore()) {
                // Player 2 Win
                updateWinCount(1)
            } else {
                // No conclusion
            }
        }
    }

    private fun updateWinCount(winPlayerIndex:Int) {
        // Update Win Count array
        if(gameProgress_wins.isNotEmpty()) {
            when(winPlayerIndex) {
                0 -> gameProgress_wins += arrayOf(gameProgress_wins[gameProgress_wins.lastIndex][0] + 1,gameProgress_wins[gameProgress_wins.lastIndex][1])  // P1 Wins
                1 -> gameProgress_wins += arrayOf(gameProgress_wins[gameProgress_wins.lastIndex][0],gameProgress_wins[gameProgress_wins.lastIndex][1] + 1)  // P2 Wins
            }
        } else {
            when(winPlayerIndex) {
                0 -> gameProgress_wins += arrayOf(1, 0)  // P1 Wins
                1 -> gameProgress_wins += arrayOf(0, 1)  // P2 Wins
            }
        }

        // Update Last Score array
        gameProgress_finalScore += arrayOf(currScore[0], currScore[1])

        // Update display
        updateWinCountDisplay()
        setAdvantageLabel(-1)

        // Reset score
        currScore = arrayOf(0,0)
        updateScoreDisplay()

        // Write result to database
        addResultToDatabase()
        updateScoreDisplaySimple()
    }

    private fun updateScoreDisplaySimple() {
        var output = ""

        // Call database to recall all instances of current current session
        var myDB = openOrCreateDatabase("my.db", MODE_PRIVATE, null)
        val result: Cursor = myDB.rawQuery("SELECT * FROM gameRecords WHERE sessionID=$sessionID ORDER BY sessionID DESC", null)
        result.moveToFirst()

        val indexLimit = (result.count - 1)

        for(i in 0..indexLimit) {
            if(!isSwapped) {
                output += result.getString(3) + " - " + result.getString(4) + "\n"
            } else {
                output += result.getString(4) + " - " + result.getString(3) + "\n"
            }

            if(i < indexLimit) {
                result.moveToNext()
            }
        }

        lbl_scoreRecordSimple.text = output
    }

    private fun updateWinCountDisplay() {
        if(gameProgress_wins.isNotEmpty()) {
            val markerLimit = 7 // Display Win Count Markers limit
            val p1_winCount = gameProgress_wins[gameProgress_wins.lastIndex][0]
            val p2_winCount = gameProgress_wins[gameProgress_wins.lastIndex][1]

            var placeholder_p1Wins = lbl_PlayerLeft_WinsIcons
            var placeholder_p2Wins = lbl_PlayerRight_WinsIcons
            if(isSwapped) {
                placeholder_p1Wins = lbl_PlayerRight_WinsIcons
                placeholder_p2Wins = lbl_PlayerLeft_WinsIcons
            }
            var outputText_p1 = ""
            var outputText_p2 = ""

            // Set P1 Wins
            if(p1_winCount <= markerLimit) {
                if(!isSwapped) {
                    for(i in 1..gameProgress_wins[gameProgress_wins.lastIndex][0]) {
                        outputText_p1 += "●"
                    }
                    for(i in (gameProgress_wins[gameProgress_wins.lastIndex][0]+1)..markerLimit) {
                        outputText_p1 += "〇"
                    }
                } else {
                    for(i in (gameProgress_wins[gameProgress_wins.lastIndex][0]+1)..markerLimit) {
                        outputText_p1 += "〇"
                    }
                    for(i in 1..gameProgress_wins[gameProgress_wins.lastIndex][0]) {
                        outputText_p1 += "●"
                    }
                }
            } else {
                outputText_p1 += "● x $p1_winCount"
            }
            placeholder_p1Wins.text = outputText_p1

            // Set P2 Wins
            if(p2_winCount <= markerLimit) {
                if(!isSwapped) {
                    for (i in (gameProgress_wins[gameProgress_wins.lastIndex][1] + 1)..markerLimit) {
                        outputText_p2 += "〇"
                    }
                    for (i in 1..gameProgress_wins[gameProgress_wins.lastIndex][1]) {
                        outputText_p2 += "●"
                    }
                } else {
                    for (i in 1..gameProgress_wins[gameProgress_wins.lastIndex][1]) {
                        outputText_p2 += "●"
                    }
                    for (i in (gameProgress_wins[gameProgress_wins.lastIndex][1] + 1)..markerLimit) {
                        outputText_p2 += "〇"
                    }
                }
            } else {
                outputText_p2 += "● x $p2_winCount"
            }
            placeholder_p2Wins.text = outputText_p2
        }
        updateScoreDisplaySimple()
    }

    private fun addResultToDatabase() {
        /*
            Match LastSave Date/Time            DATETIME
            Match Number (starting with 0)      INT
            Match Final Score P1                INT
            Match Final Score P2                INT
            Win Count P1                        INT
            Win Count P2                        INT
        */

        val currMatchNumber         = gameProgress_wins.lastIndex
        val currMatchFinalScoreP1   = gameProgress_finalScore[currMatchNumber][0]
        val currMatchFinalScoreP2   = gameProgress_finalScore[currMatchNumber][1]
        val currMatchWinCountP1     = gameProgress_wins[currMatchNumber][0]
        val currMatchWinCountP2     = gameProgress_wins[currMatchNumber][1]

        var myDB = openOrCreateDatabase("my.db", MODE_PRIVATE, null)
        myDB.execSQL("INSERT INTO gameRecords VALUES(" +
                sessionID + ", " +
                "datetime('now'), " +
                currMatchNumber + ", " +
                currMatchFinalScoreP1 + ", " +
                currMatchFinalScoreP2 + ", " +
                currMatchWinCountP1 + ", " +
                currMatchWinCountP2 + ")")
        showToast("Result Saved.")
    }

    private fun setAdvantageLabel(playerIndex:Int) {
        if(playerIndex == 0) {
            // Advantage to Player 1
            if(!isSwapped) {
                lbl_PlayerLeft_Advantage.isInvisible = false
                lbl_PlayerRight_Advantage.isInvisible = true
            } else {
                lbl_PlayerLeft_Advantage.isInvisible = true
                lbl_PlayerRight_Advantage.isInvisible = false
            }
        } else if(playerIndex == 1) {
            // Advantage to Player 2
            if(!isSwapped) {
                lbl_PlayerLeft_Advantage.isInvisible = true
                lbl_PlayerRight_Advantage.isInvisible = false
            } else {
                lbl_PlayerLeft_Advantage.isInvisible = false
                lbl_PlayerRight_Advantage.isInvisible = true
            }
        } else {
            // No advantage
            lbl_PlayerLeft_Advantage.isInvisible = true
            lbl_PlayerRight_Advantage.isInvisible = true
        }
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    */
}
