import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.codaliscia.data.AppDB
import com.codaliscia.data.MIGRATION_1_2
import com.codaliscia.data.Subscription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 02.05.2020
 */
@RunWith(AndroidJUnit4::class)
class RoomMigrationTest {
    private val TEST_DB = "migration-test"
    private val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDB::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, *ALL_MIGRATIONS);
    }

    private fun insertSubscription(subscription: Subscription, db: SupportSQLiteDatabase) {
        db.insert("subscriptions", CONFLICT_REPLACE, ContentValues().apply {
            put("shopName", subscription.shopName)
            put("shopAddress", subscription.shopAddress)
            put("shopBrand", subscription.shopBrand)
            put("lat", subscription.lat)
            put("lng", subscription.lng)
            put("isActive", subscription.isActive)
            put("timestamp", subscription.timestamp)
        })
    }
}