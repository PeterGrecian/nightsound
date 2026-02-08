package com.nightsound.ui.screens.playback;

import android.app.Application;
import com.nightsound.data.repository.AudioRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class PlaybackViewModel_Factory implements Factory<PlaybackViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<AudioRepository> audioRepositoryProvider;

  public PlaybackViewModel_Factory(Provider<Application> applicationProvider,
      Provider<AudioRepository> audioRepositoryProvider) {
    this.applicationProvider = applicationProvider;
    this.audioRepositoryProvider = audioRepositoryProvider;
  }

  @Override
  public PlaybackViewModel get() {
    return newInstance(applicationProvider.get(), audioRepositoryProvider.get());
  }

  public static PlaybackViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<AudioRepository> audioRepositoryProvider) {
    return new PlaybackViewModel_Factory(applicationProvider, audioRepositoryProvider);
  }

  public static PlaybackViewModel newInstance(Application application,
      AudioRepository audioRepository) {
    return new PlaybackViewModel(application, audioRepository);
  }
}
