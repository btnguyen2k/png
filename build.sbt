import com.typesafe.config._

val conf       = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
val appName    = conf.getString("app.name").toLowerCase().replaceAll("\\W+", "-")
val appVersion = conf.getString("app.version")

EclipseKeys.skipParents in ThisBuild := false
EclipseKeys.preTasks := Seq(compile in Compile)
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java
EclipseKeys.eclipseOutput := Some(".target")
EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18)
//EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
//EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
EclipseKeys.createSrc := EclipseCreateSrc.ManagedClasses + EclipseCreateSrc.ManagedResources + EclipseCreateSrc.Unmanaged + EclipseCreateSrc.Managed + EclipseCreateSrc.Source + EclipseCreateSrc.Resource

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(rjs, digest, gzip)

lazy val root = (project in file(".")).enablePlugins(PlayJava).settings(
    name    := appName,
    version := appVersion
)

scalaVersion := "2.11.7"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

resolvers += "Sonatype OSS Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots"

val _springVersion           = "4.2.5.RELEASE"
val _ddthCacheAdapterVersion = "0.4.1.4"
val _ddthCommons             = "0.4.0"
val _ddthDao                 = "0.5.0.5"
val _ddthQueueVersion        = "0.4.2"
val _akkaVersion             = "2.4.4"

libraryDependencies ++= Seq(
    "com.typesafe.akka"         %% "akka-cluster"               % _akkaVersion,
    "com.typesafe.akka"         %% "akka-cluster-metrics"       % _akkaVersion,
    "com.typesafe.akka"         %% "akka-cluster-tools"         % _akkaVersion,
    
    "org.slf4j"                 %  "log4j-over-slf4j"           % "1.7.21",

    // DB for metadata: MySQL or PostgreSQL
    // DB connection pool: HikariCP or DBCP2
    "com.zaxxer"                %  "HikariCP"                   % "2.4.5",
    // "org.apache.commons"        %  "commons-dbcp2"              % "2.1.1",
    "mysql"                     %  "mysql-connector-java"       % "5.1.38",
    "org.postgresql"            %  "postgresql"                 % "9.4.1208",
    "com.h2database"            %  "h2"                         % "1.4.191",

    "org.springframework"       %  "spring-beans"               % _springVersion,
    "org.springframework"       %  "spring-expression"          % _springVersion,
    "org.springframework"       %  "spring-jdbc"                % _springVersion,
    
    "redis.clients"             %  "jedis"                      % "2.8.1",
    "com.github.ddth"           %  "ddth-cache-adapter-core"    % _ddthCacheAdapterVersion,
    "com.github.ddth"           %  "ddth-cache-adapter-redis"   % _ddthCacheAdapterVersion,

    "com.github.ddth"           %  "ddth-commons-core"          % _ddthCommons,
    "com.github.ddth"           %  "ddth-commons-serialization" % _ddthCommons,

    "com.github.ddth"           %  "ddth-dao-core"              % _ddthDao,
    "com.github.ddth"           %  "ddth-dao-jdbc"              % _ddthDao,

    "com.notnoop.apns"          %  "apns"                       % "1.0.0.Beta6",

    // queue system: Kafka
    "com.github.ddth"           %  "ddth-kafka"                 % "1.2.1",
    "com.github.ddth"           %  "ddth-queue-core"            % _ddthQueueVersion,
    "com.github.ddth"           %  "ddth-queue-kafka"           % _ddthQueueVersion
)
