package io.github.cdfn.skyblock.commons.module;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.hjson.HjsonConfigurer;
import java.nio.file.Path;

public class OkaeriConfigModule<T extends OkaeriConfig> extends AbstractModule {

  private final T config;
  private final Class<T> clazz;

  public OkaeriConfigModule(Path path, Class<T> clazz) {
    this.clazz = clazz;
    if(path == null) {
      this.config = ConfigManager.create(clazz, (it) -> {
        it.withConfigurer(new HjsonConfigurer());
      });
    } else {
      this.config = ConfigManager.create(clazz, (it) -> {
        it.withBindFile(path.toFile());
        it.withConfigurer(new HjsonConfigurer());
        it.saveDefaults();
        it.load(true);
      });
    }
  }

  @Override
  protected void configure() {
    bind(TypeLiteral.get(clazz)).toInstance(config);
  }
}
