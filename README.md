# scalajs-vite

scalajs-vite is a module bundler for Scala.js projects that use npm packages: it bundles the .js file emitted by the 
Scala.js compiler with its npm dependencies into a single .js file using Vite.

Setup and use of this plugin should feel quite familiar to users of well known 
[scalajs-bundler](https://scalacenter.github.io/scalajs-bundler), with the main difference being  that there is no special 
handling of `npmDependencies` - they must be provided through `package.json` placed within `vite` directory in project's
base.

## Getting started

### Basic setup

1. Setup project layout, following is a minimal example:

   ```
   src
     main
      scala
        example
          Main.scala # Scala.js entrypoint
   vite
     index.html # Vite entrypoint
     package.json # devDependencies must provide Vite package
   ```

1. Add plugin to sbt project:

   ```scala
   addSbtPlugin("me.ptrdom" % "sbt-scalajs-vite" % scalaJSViteVersion)
   ```

1. Enable plugin in `build.sbt`:

   ```
   enablePlugins(ScalaJSVitePlugin)
   ```
   
1. Specify application entrypoint:

   ```
   scalaJSModuleInitializers := Seq(
     ModuleInitializer
       .mainMethodWithArgs("example.Main", "main")
       .withModuleID("main")
   )
   ```
   
   Such configuration would allow `main.js` bundle to be used in Vite entrypoint:

   ```html
   <script type="module" src="/main.js"></script>
   ```

1. Use sbt tasks to compile Scala.js code and run Vite:
   - For development-like `vite dev`:
     - `fastLinkJS/startVite;~fastLinkJS/viteCompile;fastLinkJS/stopVite`
   - For production-like `vite preview`:
     - `fullLinkJS/startPreview;~fullLinkJS/viteBuild;fullLinkJS/stopPreview`

Running `fullLinkJS/viteBuild` produces a production-ready `vite build` in `/target/${scalaVersion}/vite/main/dist` 
directory.

All files in `vite` directory are copied to Vite working directory, so any other web resources and relevant configuration
files can be put there.

See `sbt-scalajs-vite/sbt-test/sbt-scalajs-vite/` directory for basic example projects.

### Integrating with sbt-web

1. Add plugin to sbt project:

   ```scala
   addSbtPlugin("me.ptrdom" % "sbt-web-scalajs-vite" % scalaJSViteVersion)
   ```

1. Enable plugin in `build.sbt`:

   ```scala
   lazy val server = project
     .settings(
       scalaJSProjects := Seq(client),
       pipelineStages := Seq(scalaJSPipeline)
     )
     .enablePlugins(WebScalaJSVitePlugin)
    
   lazy val client = project.enablePlugins(ScalaJSVitePlugin)
   ```

See `sbt-web-scalajs-vite/sbt-test/sbt-web-scalajs-vite/` directory for basic example projects.

### Package managers


Uses [npm](https://www.npmjs.com/) by default, but provided `PackageManager` abstraction allows configuration of other
package managers.

```scala
//for yarn
vitePackageManager := new scalajsvite.PackageManager {
  override def name = "yarn"
  override def lockFile = "yarn.lock"
  override def installCommand = "install"
}

// for pnpm
vitePackageManager := new scalajsvite.PackageManager {
  override def name = "pnpm"
  override def lockFile = "pnpm-lock.yaml"
  override def installCommand = "install"
}
```

## License

This software is licensed under the MIT license
