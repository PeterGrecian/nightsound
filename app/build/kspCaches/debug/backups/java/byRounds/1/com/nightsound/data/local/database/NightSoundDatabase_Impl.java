package com.nightsound.data.local.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.nightsound.data.local.dao.AudioSnippetDao;
import com.nightsound.data.local.dao.AudioSnippetDao_Impl;
import com.nightsound.data.local.dao.RecordingSessionDao;
import com.nightsound.data.local.dao.RecordingSessionDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class NightSoundDatabase_Impl extends NightSoundDatabase {
  private volatile AudioSnippetDao _audioSnippetDao;

  private volatile RecordingSessionDao _recordingSessionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `audio_snippets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileName` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `rmsValue` REAL NOT NULL, `sessionId` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `recording_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `snippetCount` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3658b1adfaa062f553ee0fc961151d12')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `audio_snippets`");
        db.execSQL("DROP TABLE IF EXISTS `recording_sessions`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsAudioSnippets = new HashMap<String, TableInfo.Column>(5);
        _columnsAudioSnippets.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAudioSnippets.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAudioSnippets.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAudioSnippets.put("rmsValue", new TableInfo.Column("rmsValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAudioSnippets.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAudioSnippets = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAudioSnippets = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAudioSnippets = new TableInfo("audio_snippets", _columnsAudioSnippets, _foreignKeysAudioSnippets, _indicesAudioSnippets);
        final TableInfo _existingAudioSnippets = TableInfo.read(db, "audio_snippets");
        if (!_infoAudioSnippets.equals(_existingAudioSnippets)) {
          return new RoomOpenHelper.ValidationResult(false, "audio_snippets(com.nightsound.data.local.entities.AudioSnippet).\n"
                  + " Expected:\n" + _infoAudioSnippets + "\n"
                  + " Found:\n" + _existingAudioSnippets);
        }
        final HashMap<String, TableInfo.Column> _columnsRecordingSessions = new HashMap<String, TableInfo.Column>(4);
        _columnsRecordingSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordingSessions.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordingSessions.put("endTime", new TableInfo.Column("endTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordingSessions.put("snippetCount", new TableInfo.Column("snippetCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecordingSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecordingSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRecordingSessions = new TableInfo("recording_sessions", _columnsRecordingSessions, _foreignKeysRecordingSessions, _indicesRecordingSessions);
        final TableInfo _existingRecordingSessions = TableInfo.read(db, "recording_sessions");
        if (!_infoRecordingSessions.equals(_existingRecordingSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "recording_sessions(com.nightsound.data.local.entities.RecordingSession).\n"
                  + " Expected:\n" + _infoRecordingSessions + "\n"
                  + " Found:\n" + _existingRecordingSessions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "3658b1adfaa062f553ee0fc961151d12", "e20364173da451f5d70d0222c8877f75");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "audio_snippets","recording_sessions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `audio_snippets`");
      _db.execSQL("DELETE FROM `recording_sessions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(AudioSnippetDao.class, AudioSnippetDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RecordingSessionDao.class, RecordingSessionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public AudioSnippetDao audioSnippetDao() {
    if (_audioSnippetDao != null) {
      return _audioSnippetDao;
    } else {
      synchronized(this) {
        if(_audioSnippetDao == null) {
          _audioSnippetDao = new AudioSnippetDao_Impl(this);
        }
        return _audioSnippetDao;
      }
    }
  }

  @Override
  public RecordingSessionDao recordingSessionDao() {
    if (_recordingSessionDao != null) {
      return _recordingSessionDao;
    } else {
      synchronized(this) {
        if(_recordingSessionDao == null) {
          _recordingSessionDao = new RecordingSessionDao_Impl(this);
        }
        return _recordingSessionDao;
      }
    }
  }
}
