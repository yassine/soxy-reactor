package com.github.yassine.soxychains;

import com.github.yassine.soxychains.cli.CommandGroup;
import com.github.yassine.soxychains.cli.ConfigurableCommand;
import com.google.common.collect.*;
import com.google.common.reflect.ClassPath;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.airlift.airline.Cli;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SoxyChainsCLI {

  private final Cli<Runnable> cli;
  private final Set<Module> modules;

  public void run(String... args){
    Runnable runnable = cli.parse(args);
    ArrayList<Module> ms = new ArrayList<>(this.modules);
    if(runnable instanceof ConfigurableCommand){
      String configPath = ((ConfigurableCommand) runnable).getConfigPath();
      ms.add(new SoxyChainsModule(configPath));
      Injector injector = Guice.createInjector(ms);
      injector.injectMembers(runnable);
    }
    runnable.run();
  }

  public static class Builder {

    private Package cliCommandsPackage;
    private Set<Module> modules = new HashSet<>();

    public Builder withCommandsPackage(Package p ){
      cliCommandsPackage = p;
      return this;
    }

    public Builder withModules(Module... modules){
      this.modules.addAll(Arrays.asList(modules));
      return this;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public SoxyChainsCLI build(){
      ClassPath classPath = ClassPath.from(SoxyChainsApplication.class.getClassLoader());

      Set<Package> packages = classPath.getTopLevelClassesRecursive(cliCommandsPackage.getName())
        .stream()
        .filter(c -> c.getSimpleName().equals("package-info"))
        .map(c -> c.load().getPackage())
        .filter(pkg -> pkg.isAnnotationPresent(CommandGroup.class))
        .collect(Collectors.toSet());
      ImmutableMap<String, Package> packageIndex = Maps.uniqueIndex(packages, Package::getName);

      ImmutableSet.Builder<Class> builder = ImmutableSet.builder();

      FastClasspathScanner scanner = new FastClasspathScanner(cliCommandsPackage.getName());
      scanner.matchClassesWithAnnotation(Command.class, builder::add);
      scanner.scan();

      Set<Class> commands = builder.build();

      ImmutableMultimap<String,Class> commandIndex = Multimaps.index(commands, command -> command.getPackage().getName());


      Cli.CliBuilder<Runnable> cliBuilder = new Cli.CliBuilder<Runnable>("tabakat")
        .withDescription("multi-layer anonymity application")
        .withDefaultCommand(Help.class)
        .withCommands(Help.class);

      commandIndex.keySet()
        .forEach(key -> {
          Package p = packageIndex.get(key);
          if(p != null){
            CommandGroup g = p.getAnnotation(CommandGroup.class);
            cliBuilder.withGroup(g.name())
              .withDescription(g.description())
              .withDefaultCommand(g.defaultCommand())
              .withCommands(commandIndex.get(key));
          }
        });

      return new SoxyChainsCLI(cliBuilder.build(), modules);
    }

  }
}
