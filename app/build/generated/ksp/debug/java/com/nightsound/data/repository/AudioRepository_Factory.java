package com.nightsound.data.repository;

import com.nightsound.data.local.database.NightSoundDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AudioRepository_Factory implements Factory<AudioRepository> {
  private final Provider<NightSoundDatabase> databaseProvider;

  public AudioRepository_Factory(Provider<NightSoundDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AudioRepository get() {
    return newInstance(databaseProvider.get());
  }

  public static AudioRepository_Factory create(Provider<NightSoundDatabase> databaseProvider) {
    return new AudioRepository_Factory(databaseProvider);
  }

  public static AudioRepository newInstance(NightSoundDatabase database) {
    return new AudioRepository(database);
  }
}
