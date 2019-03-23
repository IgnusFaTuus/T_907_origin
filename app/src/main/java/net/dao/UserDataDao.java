package net.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import net.kehui.www.t_907_origin.database.UserData;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER_DATA".
*/
public class UserDataDao extends AbstractDao<UserData, Long> {

    public static final String TABLENAME = "USER_DATA";

    /**
     * Properties of entity UserData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property InfoId = new Property(1, String.class, "InfoId", false, "INFO_ID");
        public final static Property TestTime = new Property(2, long.class, "testTime", false, "TEST_TIME");
        public final static Property TestMethod = new Property(3, String.class, "testMethod", false, "TEST_METHOD");
        public final static Property TestRange = new Property(4, String.class, "testRange", false, "TEST_RANGE");
        public final static Property WaveVelocity = new Property(5, String.class, "waveVelocity", false, "WAVE_VELOCITY");
        public final static Property TestPosition = new Property(6, String.class, "testPosition", false, "TEST_POSITION");
        public final static Property CableFullLength = new Property(7, String.class, "cableFullLength", false, "CABLE_FULL_LENGTH");
        public final static Property CablePhase = new Property(8, String.class, "cablePhase", false, "CABLE_PHASE");
        public final static Property CableType = new Property(9, String.class, "cableType", false, "CABLE_TYPE");
        public final static Property FaultType = new Property(10, String.class, "faultType", false, "FAULT_TYPE");
        public final static Property FaultLength = new Property(11, String.class, "faultLength", false, "FAULT_LENGTH");
        public final static Property Gain = new Property(12, int.class, "Gain", false, "GAIN");
        public final static Property Balance = new Property(13, int.class, "Balance", false, "BALANCE");
        public final static Property Language = new Property(14, String.class, "language", false, "LANGUAGE");
    }


    public UserDataDao(DaoConfig config) {
        super(config);
    }
    
    public UserDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER_DATA\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"INFO_ID\" TEXT," + // 1: InfoId
                "\"TEST_TIME\" INTEGER NOT NULL ," + // 2: testTime
                "\"TEST_METHOD\" TEXT," + // 3: testMethod
                "\"TEST_RANGE\" TEXT," + // 4: testRange
                "\"WAVE_VELOCITY\" TEXT," + // 5: waveVelocity
                "\"TEST_POSITION\" TEXT," + // 6: testPosition
                "\"CABLE_FULL_LENGTH\" TEXT," + // 7: cableFullLength
                "\"CABLE_PHASE\" TEXT," + // 8: cablePhase
                "\"CABLE_TYPE\" TEXT," + // 9: cableType
                "\"FAULT_TYPE\" TEXT," + // 10: faultType
                "\"FAULT_LENGTH\" TEXT," + // 11: faultLength
                "\"GAIN\" INTEGER NOT NULL ," + // 12: Gain
                "\"BALANCE\" INTEGER NOT NULL ," + // 13: Balance
                "\"LANGUAGE\" TEXT);"); // 14: language
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER_DATA\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, UserData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String InfoId = entity.getInfoId();
        if (InfoId != null) {
            stmt.bindString(2, InfoId);
        }
        stmt.bindLong(3, entity.getTestTime());
 
        String testMethod = entity.getTestMethod();
        if (testMethod != null) {
            stmt.bindString(4, testMethod);
        }
 
        String testRange = entity.getTestRange();
        if (testRange != null) {
            stmt.bindString(5, testRange);
        }
 
        String waveVelocity = entity.getWaveVelocity();
        if (waveVelocity != null) {
            stmt.bindString(6, waveVelocity);
        }
 
        String testPosition = entity.getTestPosition();
        if (testPosition != null) {
            stmt.bindString(7, testPosition);
        }
 
        String cableFullLength = entity.getCableFullLength();
        if (cableFullLength != null) {
            stmt.bindString(8, cableFullLength);
        }
 
        String cablePhase = entity.getCablePhase();
        if (cablePhase != null) {
            stmt.bindString(9, cablePhase);
        }
 
        String cableType = entity.getCableType();
        if (cableType != null) {
            stmt.bindString(10, cableType);
        }
 
        String faultType = entity.getFaultType();
        if (faultType != null) {
            stmt.bindString(11, faultType);
        }
 
        String faultLength = entity.getFaultLength();
        if (faultLength != null) {
            stmt.bindString(12, faultLength);
        }
        stmt.bindLong(13, entity.getGain());
        stmt.bindLong(14, entity.getBalance());
 
        String language = entity.getLanguage();
        if (language != null) {
            stmt.bindString(15, language);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, UserData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String InfoId = entity.getInfoId();
        if (InfoId != null) {
            stmt.bindString(2, InfoId);
        }
        stmt.bindLong(3, entity.getTestTime());
 
        String testMethod = entity.getTestMethod();
        if (testMethod != null) {
            stmt.bindString(4, testMethod);
        }
 
        String testRange = entity.getTestRange();
        if (testRange != null) {
            stmt.bindString(5, testRange);
        }
 
        String waveVelocity = entity.getWaveVelocity();
        if (waveVelocity != null) {
            stmt.bindString(6, waveVelocity);
        }
 
        String testPosition = entity.getTestPosition();
        if (testPosition != null) {
            stmt.bindString(7, testPosition);
        }
 
        String cableFullLength = entity.getCableFullLength();
        if (cableFullLength != null) {
            stmt.bindString(8, cableFullLength);
        }
 
        String cablePhase = entity.getCablePhase();
        if (cablePhase != null) {
            stmt.bindString(9, cablePhase);
        }
 
        String cableType = entity.getCableType();
        if (cableType != null) {
            stmt.bindString(10, cableType);
        }
 
        String faultType = entity.getFaultType();
        if (faultType != null) {
            stmt.bindString(11, faultType);
        }
 
        String faultLength = entity.getFaultLength();
        if (faultLength != null) {
            stmt.bindString(12, faultLength);
        }
        stmt.bindLong(13, entity.getGain());
        stmt.bindLong(14, entity.getBalance());
 
        String language = entity.getLanguage();
        if (language != null) {
            stmt.bindString(15, language);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public UserData readEntity(Cursor cursor, int offset) {
        UserData entity = new UserData( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // InfoId
            cursor.getLong(offset + 2), // testTime
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // testMethod
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // testRange
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // waveVelocity
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // testPosition
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // cableFullLength
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // cablePhase
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // cableType
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // faultType
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // faultLength
            cursor.getInt(offset + 12), // Gain
            cursor.getInt(offset + 13), // Balance
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14) // language
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, UserData entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setInfoId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setTestTime(cursor.getLong(offset + 2));
        entity.setTestMethod(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setTestRange(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setWaveVelocity(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setTestPosition(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setCableFullLength(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setCablePhase(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setCableType(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setFaultType(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setFaultLength(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setGain(cursor.getInt(offset + 12));
        entity.setBalance(cursor.getInt(offset + 13));
        entity.setLanguage(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(UserData entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(UserData entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(UserData entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
