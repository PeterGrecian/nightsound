package com.nightsound.di;

import android.content.Context;
import com.nightsound.data.local.database.NightSoundDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideNightSoundDatabaseFactory implements Factory<NightSoundDatabase> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideNightSoundDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NightSoundDatabase get() {
    return provideNightSoundDatabase(contextProvider.get());
  }

  public static AppModule_ProvideNightSoundDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvideNightSoundDatabaseFactory(contextProvider);
  }

  public static NightSoundDatabase provideNightSoundDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideNightSoundDatabase(context));
  }
}
