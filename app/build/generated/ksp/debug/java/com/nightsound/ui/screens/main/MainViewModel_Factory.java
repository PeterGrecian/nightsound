package com.nightsound.ui.screens.main;

import android.app.Application;
import com.nightsound.data.repository.SettingsRepository;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public MainViewModel_Factory(Provider<Application> applicationProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.applicationProvider = applicationProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(applicationProvider.get(), settingsRepositoryProvider.get());
  }

  public static MainViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new MainViewModel_Factory(applicationProvider, settingsRepositoryProvider);
  }

  public static MainViewModel newInstance(Application application,
      SettingsRepository settingsRepository) {
    return new MainViewModel(application, settingsRepository);
  }
}
