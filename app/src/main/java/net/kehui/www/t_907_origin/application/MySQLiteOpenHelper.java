package net.kehui.www.t_907_origin.application;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.dao.DaoMaster;
import net.dao.UserDataDao;
import net.kehui.www.t_907_origin.database.UserData;

import org.greenrobot.greendao.database.Database;

/**
 *
 * @author IF
 * @date 2019/4/4
 */
public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }
    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                DaoMaster.createAllTables(db, ifNotExists);
            }
            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                DaoMaster.dropAllTables(db, ifExists);
            }
        }, UserDataDao.class);
    }
}