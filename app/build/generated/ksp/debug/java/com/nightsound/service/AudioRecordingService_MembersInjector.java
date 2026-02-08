package com.nightsound.service;

import com.nightsound.data.local.database.NightSoundDatabase;
import com.nightsound.data.repository.SettingsRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AudioRecordingService_MembersInjector implements MembersInjector<AudioRecordingService> {
  private final Provider<NightSoundDatabase> databaseProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public AudioRecordingService_MembersInjector(Provider<NightSoundDatabase> databaseProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.databaseProvider = databaseProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public static MembersInjector<AudioRecordingService> create(
      Provider<NightSoundDatabase> databaseProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new AudioRecordingService_MembersInjector(databaseProvider, settingsRepositoryProvider);
  }

  @Override
  public void injectMembers(AudioRecordingService instance) {
    injectDatabase(instance, databaseProvider.get());
    injectSettingsRepository(instance, settingsRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.nightsound.service.AudioRecordingService.database")
  public static void injectDatabase(AudioRecordingService instance, NightSoundDatabase database) {
    instance.database = database;
  }

  @InjectedFieldSignature("com.nightsound.service.AudioRecordingService.settingsRepository")
  public static void injectSettingsRepository(AudioRecordingService instance,
      SettingsRepository settingsRepository) {
    instance.settingsRepository = settingsRepository;
  }
}
