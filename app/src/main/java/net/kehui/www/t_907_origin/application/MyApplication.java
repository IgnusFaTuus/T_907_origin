package net.kehui.www.t_907_origin.application;

import android.app.Application;

import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;

import net.dao.DaoMaster;
import net.dao.DaoSession;
import net.kehui.www.t_907_origin.util.MultiLanguageUtil;
import net.kehui.www.t_907_origin.util.PrefUtils;


import java.util.Locale;

public class MyApplication extends Application {
    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    public static MyApplication instances;

    //public final Locale Locale_Russia = new Locale("RU", "ru", "");
    //public final Locale Locale_French = new Locale("FR", "fr", "");
    //public final Locale Locale_Spanisch = new Locale("Es", "es", "");

    @Override
    public void onCreate() {
        super.onCreate();
        MultiLanguageUtil.init(getApplicationContext());
        instances = this;
        MultiLanguageUtil.getInstance().updateLanguage(PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys"));

        setDatabase();
        //_socket = null;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MultiLanguageUtil.getInstance().updateLanguage(PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys"));
//        switchLanguage(PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys"));
    }

    public static MyApplication getInstances() {
        return instances;
    }

    /**
     * 设置greenDao
     */
    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        mHelper = new DaoMaster.DevOpenHelper(this, "notes-db", null);
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

}
