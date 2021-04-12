package com.telit.zhkt_three.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.telit.zhkt_three.JavaBean.LineMatchBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "LINE_MATCH_BEAN".
*/
public class LineMatchBeanDao extends AbstractDao<LineMatchBean, Long> {

    public static final String TABLENAME = "LINE_MATCH_BEAN";

    /**
     * Properties of entity LineMatchBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Position = new Property(0, int.class, "position", false, "POSITION");
        public final static Property StartX = new Property(1, float.class, "startX", false, "START_X");
        public final static Property StartY = new Property(2, float.class, "startY", false, "START_Y");
        public final static Property EndX = new Property(3, float.class, "endX", false, "END_X");
        public final static Property EndY = new Property(4, float.class, "endY", false, "END_Y");
        public final static Property Id = new Property(5, Long.class, "id", true, "_id");
        public final static Property LeftId = new Property(6, String.class, "leftId", false, "LEFT_ID");
        public final static Property RightId = new Property(7, String.class, "rightId", false, "RIGHT_ID");
        public final static Property TypeId = new Property(8, String.class, "TypeId", false, "TYPE_ID");
    }


    public LineMatchBeanDao(DaoConfig config) {
        super(config);
    }
    
    public LineMatchBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"LINE_MATCH_BEAN\" (" + //
                "\"POSITION\" INTEGER NOT NULL ," + // 0: position
                "\"START_X\" REAL NOT NULL ," + // 1: startX
                "\"START_Y\" REAL NOT NULL ," + // 2: startY
                "\"END_X\" REAL NOT NULL ," + // 3: endX
                "\"END_Y\" REAL NOT NULL ," + // 4: endY
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 5: id
                "\"LEFT_ID\" TEXT," + // 6: leftId
                "\"RIGHT_ID\" TEXT," + // 7: rightId
                "\"TYPE_ID\" TEXT);"); // 8: TypeId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"LINE_MATCH_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, LineMatchBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getPosition());
        stmt.bindDouble(2, entity.getStartX());
        stmt.bindDouble(3, entity.getStartY());
        stmt.bindDouble(4, entity.getEndX());
        stmt.bindDouble(5, entity.getEndY());
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(6, id);
        }
 
        String leftId = entity.getLeftId();
        if (leftId != null) {
            stmt.bindString(7, leftId);
        }
 
        String rightId = entity.getRightId();
        if (rightId != null) {
            stmt.bindString(8, rightId);
        }
 
        String TypeId = entity.getTypeId();
        if (TypeId != null) {
            stmt.bindString(9, TypeId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, LineMatchBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getPosition());
        stmt.bindDouble(2, entity.getStartX());
        stmt.bindDouble(3, entity.getStartY());
        stmt.bindDouble(4, entity.getEndX());
        stmt.bindDouble(5, entity.getEndY());
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(6, id);
        }
 
        String leftId = entity.getLeftId();
        if (leftId != null) {
            stmt.bindString(7, leftId);
        }
 
        String rightId = entity.getRightId();
        if (rightId != null) {
            stmt.bindString(8, rightId);
        }
 
        String TypeId = entity.getTypeId();
        if (TypeId != null) {
            stmt.bindString(9, TypeId);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5);
    }    

    @Override
    public LineMatchBean readEntity(Cursor cursor, int offset) {
        LineMatchBean entity = new LineMatchBean( //
            cursor.getInt(offset + 0), // position
            cursor.getFloat(offset + 1), // startX
            cursor.getFloat(offset + 2), // startY
            cursor.getFloat(offset + 3), // endX
            cursor.getFloat(offset + 4), // endY
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // id
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // leftId
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // rightId
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8) // TypeId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, LineMatchBean entity, int offset) {
        entity.setPosition(cursor.getInt(offset + 0));
        entity.setStartX(cursor.getFloat(offset + 1));
        entity.setStartY(cursor.getFloat(offset + 2));
        entity.setEndX(cursor.getFloat(offset + 3));
        entity.setEndY(cursor.getFloat(offset + 4));
        entity.setId(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setLeftId(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setRightId(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setTypeId(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(LineMatchBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(LineMatchBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(LineMatchBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}