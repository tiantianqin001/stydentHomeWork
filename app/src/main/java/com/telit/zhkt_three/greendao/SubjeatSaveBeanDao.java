package com.telit.zhkt_three.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.telit.zhkt_three.JavaBean.SubjeatSaveBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SUBJEAT_SAVE_BEAN".
*/
public class SubjeatSaveBeanDao extends AbstractDao<SubjeatSaveBean, String> {

    public static final String TABLENAME = "SUBJEAT_SAVE_BEAN";

    /**
     * Properties of entity SubjeatSaveBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "ID");
        public final static Property Images = new Property(1, String.class, "images", false, "IMAGES");
        public final static Property LayoutPosition = new Property(2, int.class, "layoutPosition", false, "LAYOUT_POSITION");
    }


    public SubjeatSaveBeanDao(DaoConfig config) {
        super(config);
    }
    
    public SubjeatSaveBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SUBJEAT_SAVE_BEAN\" (" + //
                "\"ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: id
                "\"IMAGES\" TEXT," + // 1: images
                "\"LAYOUT_POSITION\" INTEGER NOT NULL );"); // 2: layoutPosition
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SUBJEAT_SAVE_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SubjeatSaveBean entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String images = entity.getImages();
        if (images != null) {
            stmt.bindString(2, images);
        }
        stmt.bindLong(3, entity.getLayoutPosition());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SubjeatSaveBean entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String images = entity.getImages();
        if (images != null) {
            stmt.bindString(2, images);
        }
        stmt.bindLong(3, entity.getLayoutPosition());
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public SubjeatSaveBean readEntity(Cursor cursor, int offset) {
        SubjeatSaveBean entity = new SubjeatSaveBean( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // images
            cursor.getInt(offset + 2) // layoutPosition
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SubjeatSaveBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setImages(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setLayoutPosition(cursor.getInt(offset + 2));
     }
    
    @Override
    protected final String updateKeyAfterInsert(SubjeatSaveBean entity, long rowId) {
        return entity.getId();
    }
    
    @Override
    public String getKey(SubjeatSaveBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(SubjeatSaveBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
