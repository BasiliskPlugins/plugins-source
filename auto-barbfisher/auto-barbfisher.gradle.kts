version = "0.0.2"

project.extra["PluginName"] = "Auto Barb Fisher"
project.extra["PluginDescription"] = "Automates 3T Barbarian Fishing"
project.extra["PluginProvider"] = "Basilisk"

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}